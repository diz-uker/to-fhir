"""Scans a FHIR package directory and classifies its resources into an IgPackageModel."""

from __future__ import annotations

import json
from pathlib import Path

from .ig_package_model import (
    ConceptConstant,
    ExtensionValueType,
    IgPackageModel,
    NamingSystemEntry,
)
from .name_utils import to_constant_name, to_enum_member_name


def resolve_package_content_dir(
    fhir_packages_dir: Path, package_name: str, package_version: str
) -> Path:
    """Returns the package content directory.

    Tries the Firely Terminal cache layout (<dir>/<name>#<version>/package) first,
    then falls back to the npm layout (<dir>/<name>).
    """
    firely = fhir_packages_dir / f"{package_name}#{package_version}" / "package"
    if firely.is_dir():
        return firely
    return fhir_packages_dir / package_name


def scan(package_content_dir: Path, package_name: str, package_version: str) -> IgPackageModel:
    """Reads all JSON files in the package directory and classifies them."""
    resources = _read_resources(package_content_dir)

    code_systems: dict[str, str] = {}
    code_system_concepts: dict[str, list[ConceptConstant]] = {}
    profiles: dict[str, str] = {}
    extensions: dict[str, str] = {}
    extension_value_types: dict[str, ExtensionValueType] = {}

    for resource in resources:
        if resource.get("resourceType") != "CodeSystem":
            continue
        rid = resource.get("id")
        url = resource.get("url")
        if not rid or not url:
            continue
        _classify_code_system(
            resource, to_constant_name(rid), url, code_systems, code_system_concepts
        )

    cs_url_by_valueset_url = _index_single_codesystem_valuesets(resources)

    for resource in resources:
        if resource.get("resourceType") != "StructureDefinition":
            continue
        rid = resource.get("id")
        url = resource.get("url")
        if not rid or not url:
            continue
        _classify_structure_definition(
            resource,
            to_constant_name(rid),
            url,
            profiles,
            extensions,
            extension_value_types,
            cs_url_by_valueset_url,
        )

    naming_systems: dict[str, NamingSystemEntry] = {}
    for resource in resources:
        if resource.get("resourceType") != "NamingSystem":
            continue
        rid = resource.get("id")
        unique_ids = resource.get("uniqueId")
        if not rid or not unique_ids:
            continue
        by_type: dict[str, list[str]] = {}
        for uid in unique_ids:
            uid_type = uid.get("type")
            uid_value = uid.get("value")
            if not uid_type or not uid_value:
                continue
            by_type.setdefault(uid_type, []).append(uid_value)
        if not by_type:
            continue
        naming_systems[to_constant_name(rid)] = NamingSystemEntry(
            description=resource.get("description"),
            by_type=dict(sorted(by_type.items())),
        )

    return IgPackageModel(
        package_name=package_name,
        package_version=package_version,
        code_systems=dict(sorted(code_systems.items())),
        profiles=dict(sorted(profiles.items())),
        extensions=dict(sorted(extensions.items())),
        code_system_concepts=code_system_concepts,
        extension_value_types=extension_value_types,
        naming_systems=dict(sorted(naming_systems.items())),
    )


def _read_resources(package_content_dir: Path) -> list[dict]:
    results = []
    for f in package_content_dir.glob("*.json"):
        try:
            with f.open(encoding="utf-8") as fp:
                data = json.load(fp)
            if isinstance(data, dict):
                results.append(data)
        except (json.JSONDecodeError, OSError):
            pass
    return results


def _classify_code_system(
    resource: dict,
    constant_name: str,
    url: str,
    code_systems: dict[str, str],
    code_system_concepts: dict[str, list[ConceptConstant]],
) -> None:
    code_systems[constant_name] = url
    if resource.get("content") != "complete":
        return
    raw_concepts = resource.get("concept")
    if not raw_concepts:
        return
    used_names: set[str] = set()
    concepts = _flatten_concepts(raw_concepts, used_names)
    if concepts:
        code_system_concepts[constant_name] = concepts


def _flatten_concepts(raw: list[dict], used_names: set[str]) -> list[ConceptConstant]:
    result = []
    for item in raw:
        code = item.get("code")
        if code:
            result.append(
                ConceptConstant(
                    constant_name=_unique_member_name(code, used_names),
                    code=code,
                    display=item.get("display"),
                )
            )
        children = item.get("concept")
        if children:
            result.extend(_flatten_concepts(children, used_names))
    return result


def _unique_member_name(code: str, used_names: set[str]) -> str:
    base = to_enum_member_name(code)
    name = base
    suffix = 2
    while name in used_names:
        name = f"{base}_{suffix}"
        suffix += 1
    used_names.add(name)
    return name


def _classify_structure_definition(
    resource: dict,
    constant_name: str,
    url: str,
    profiles: dict[str, str],
    extensions: dict[str, str],
    extension_value_types: dict[str, ExtensionValueType],
    cs_url_by_valueset_url: dict[str, str],
) -> None:
    kind = resource.get("kind")
    derivation = resource.get("derivation")
    if kind == "logical" or derivation == "specialization":
        return
    is_extension = kind == "complex-type" and derivation == "constraint"
    if is_extension and resource.get("type") == "Extension":
        extensions[constant_name] = url
        extension_value_types[constant_name] = _extension_value_type_for(
            resource, cs_url_by_valueset_url
        )
        return
    if kind == "resource" and derivation == "constraint":
        version = resource.get("version")
        profiles[constant_name] = f"{url}|{version}" if version else url


def _extension_value_type_for(
    resource: dict, cs_url_by_valueset_url: dict[str, str]
) -> ExtensionValueType:
    elements: list[dict] = (resource.get("snapshot") or {}).get("element") or []
    for element in elements:
        if element.get("path") != "Extension.value[x]":
            continue
        types: list[dict] = element.get("type") or []
        if element.get("max") == "0" or not types:
            return ExtensionValueType.NONE
        if len(types) > 1:
            return ExtensionValueType.CHOICE
        code = types[0].get("code")
        if not code:
            return ExtensionValueType.NONE
        bound_url = _bound_code_system_url(elements, element, code, cs_url_by_valueset_url)
        if bound_url:
            return ExtensionValueType.bound_coding(code, bound_url)
        return ExtensionValueType.fixed(code)
    return ExtensionValueType.NONE


def _bound_code_system_url(
    elements: list[dict],
    value_element: dict,
    value_type_code: str,
    cs_url_by_valueset_url: dict[str, str],
) -> str | None:
    if value_type_code not in ("CodeableConcept", "Coding"):
        return None
    system_path = (
        "Extension.value[x].coding.system"
        if value_type_code == "CodeableConcept"
        else "Extension.value[x].system"
    )
    for element in elements:
        if element.get("path") == system_path:
            fixed_uri = element.get("fixedUri")
            if fixed_uri:
                return fixed_uri
    binding: dict = value_element.get("binding") or {}
    if binding.get("strength") != "required":
        return None
    value_set: str | None = binding.get("valueSet")
    if not value_set:
        return None
    bare_url = value_set.split("|")[0]
    return cs_url_by_valueset_url.get(bare_url)


def _index_single_codesystem_valuesets(resources: list[dict]) -> dict[str, str]:
    result: dict[str, str] = {}
    for resource in resources:
        if resource.get("resourceType") != "ValueSet":
            continue
        url = resource.get("url")
        if not url:
            continue
        cs_url = _single_code_system_url(resource.get("compose") or {})
        if cs_url:
            result[url] = cs_url
    return result


def _single_code_system_url(compose: dict) -> str | None:
    if compose.get("exclude"):
        return None
    includes: list[dict] = compose.get("include") or []
    if len(includes) != 1:
        return None
    only = includes[0]
    if only.get("valueSet"):
        return None
    return only.get("system")

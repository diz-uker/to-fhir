"""Scans a restored FHIR package directory (``~/.fhir/packages/<name>#<version>/package/``, or
``node_modules/<name>/`` if installed via npm) and classifies its resources into
CodeSystem/Profile/Extension canonical URL constants.
"""

from __future__ import annotations

import json
from pathlib import Path

from ig_codegen import name_utils
from ig_codegen.concept_constant import ConceptConstant
from ig_codegen.fhir_resource_summary import Concept, FhirResourceSummary
from ig_codegen.ig_package_model import IgPackageModel


class IgPackageScanner:
    def resolve_package_content_dir(
        self, fhir_packages_dir: Path, package_name: str, package_version: str
    ) -> Path:
        """Resolves the directory holding a package's resource JSON files. Tries the Firely
        Terminal cache layout (``<fhir_packages_dir>/<package_name>#<package_version>/package``)
        first, then falls back to the flat npm layout (``<fhir_packages_dir>/<package_name>``, no
        version segment, no nested ``package`` directory) used when FHIR packages are installed via
        ``npm install`` instead of Firely Terminal's ``fhir restore``.
        """
        firely_layout = fhir_packages_dir / f"{package_name}#{package_version}" / "package"
        if firely_layout.is_dir():
            return firely_layout
        return fhir_packages_dir / package_name

    def scan(
        self, package_content_dir: Path, package_name: str, package_version: str
    ) -> IgPackageModel:
        code_systems: dict[str, str] = {}
        profiles: dict[str, str] = {}
        extensions: dict[str, str] = {}
        code_system_concepts: dict[str, list[ConceptConstant]] = {}

        for file in sorted(package_content_dir.glob("*.json")):
            self._classify(file, code_systems, profiles, extensions, code_system_concepts)

        return IgPackageModel(
            package_name=package_name,
            package_version=package_version,
            code_systems=dict(sorted(code_systems.items())),
            profiles=dict(sorted(profiles.items())),
            extensions=dict(sorted(extensions.items())),
            code_system_concepts=code_system_concepts,
        )

    def _classify(
        self,
        file: Path,
        code_systems: dict[str, str],
        profiles: dict[str, str],
        extensions: dict[str, str],
        code_system_concepts: dict[str, list[ConceptConstant]],
    ) -> None:
        resource = FhirResourceSummary.from_json(json.loads(file.read_text()))

        resource_type = resource.resource_type
        fhir_id = resource.id
        url = resource.url
        if resource_type is None or fhir_id is None or url is None:
            return

        constant_name = name_utils.to_constant_name(fhir_id)

        if resource_type == "CodeSystem":
            self._classify_code_system(
                resource, constant_name, url, code_systems, code_system_concepts
            )
            return

        if resource_type != "StructureDefinition":
            return

        kind = resource.kind
        derivation = resource.derivation
        if kind == "logical" or derivation == "specialization":
            return

        if kind == "complex-type" and derivation == "constraint" and resource.type == "Extension":
            extensions[constant_name] = url
            return

        if kind == "resource" and derivation == "constraint":
            version = resource.version
            profiles[constant_name] = url if version is None else f"{url}|{version}"

    @staticmethod
    def _classify_code_system(
        resource: FhirResourceSummary,
        constant_name: str,
        url: str,
        code_systems: dict[str, str],
        code_system_concepts: dict[str, list[ConceptConstant]],
    ) -> None:
        code_systems[constant_name] = url
        if resource.content != "complete" or not resource.concept:
            return
        concepts = _flatten_concepts(resource.concept)
        if concepts:
            code_system_concepts[constant_name] = concepts


def _flatten_concepts(concepts: list[Concept]) -> list[ConceptConstant]:
    """Flattens a CodeSystem's concept hierarchy (including both group/parent and leaf concepts)
    into a flat, name-collision-free list, in document order.
    """
    result: list[ConceptConstant] = []
    used_names: set[str] = set()
    _flatten_concepts_into(concepts, result, used_names)
    return result


def _flatten_concepts_into(
    concepts: list[Concept], result: list[ConceptConstant], used_names: set[str]
) -> None:
    for concept in concepts:
        if concept.code is not None:
            result.append(
                ConceptConstant(
                    constant_name=_unique_enum_constant_name(concept.code, used_names),
                    code=concept.code,
                    display=concept.display,
                )
            )
        if concept.concept:
            _flatten_concepts_into(concept.concept, result, used_names)


def _unique_enum_constant_name(code: str, used_names: set[str]) -> str:
    base_name = name_utils.to_enum_constant_name(code)
    name = base_name
    suffix = 2
    while name in used_names:
        name = f"{base_name}_{suffix}"
        suffix += 1
    used_names.add(name)
    return name

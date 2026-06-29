"""Renders an :class:`~ig_codegen.ig_package_model.IgPackageModel` as a Python source file: one
nested class per non-empty category (CodeSystems/Profiles/Extensions), each holding one static
no-arg accessor method per canonical URL, named in snake_case (e.g. the FHIR id
``mii-pr-diagnose-condition`` becomes the accessor ``mii_pr_diagnose_condition()``, called as
``Onkologie.Profiles.mii_pr_diagnose_condition()``).

A CodeSystem that ships its own concepts inline (``content == "complete"``) additionally gets a
nested ``enum.Enum``, named after the CodeSystem itself, with one member per concept, a
``coding()`` accessor returning a ``fhir.resources.R4B.coding.Coding``, and a ``from_value(code)``
class method lookup, e.g. ``Onkologie.CodeSystems.MiiCsOnkoIntention.K.coding()`` and
``Onkologie.CodeSystems.MiiCsOnkoIntention.from_value("K")``.
"""

from __future__ import annotations

import subprocess
from pathlib import Path

from jinja2 import Environment, PackageLoader

from ig_codegen import name_utils
from ig_codegen.ig_package_model import IgPackageModel

_ENV = Environment(
    loader=PackageLoader("ig_codegen", "templates"),
    trim_blocks=True,
    lstrip_blocks=True,
)
_ENV.filters["pyrepr"] = repr
_TEMPLATE = _ENV.get_template("module.py.jinja2")


def generate(model: IgPackageModel, class_name: str) -> str:
    categories = [
        _build_category("CodeSystems", model.code_systems, model.code_system_concepts),
        _build_category("Profiles", model.profiles, {}),
        _build_category("Extensions", model.extensions, {}),
    ]
    categories = [category for category in categories if category["entries"]]
    has_any_enum = any(entry["enum"] for category in categories for entry in category["entries"])

    source = _TEMPLATE.render(
        package_name=model.package_name,
        package_version=model.package_version,
        class_name=class_name,
        categories=categories,
        has_any_enum=has_any_enum,
    )
    return _format(source)


def write_to(model: IgPackageModel, class_name: str, module_file: Path) -> Path:
    module_file.parent.mkdir(parents=True, exist_ok=True)
    module_file.write_text(generate(model, class_name))
    return module_file


def _build_category(name: str, constants: dict[str, str], concepts_by_name: dict) -> dict:
    entries = []
    for constant_name, url in constants.items():
        concepts = concepts_by_name.get(constant_name)
        enum = None
        if concepts:
            enum = {
                "class_name": name_utils.to_pascal_case(constant_name),
                "system": url,
                "concepts": concepts,
            }
        entries.append(
            {
                "accessor_name": name_utils.to_snake_case(constant_name),
                "url": url,
                "enum": enum,
            }
        )
    return {"name": name, "entries": entries}


def _format(source: str) -> str:
    """Pretty-prints generated source with ``ruff format``, falling back to the raw, already
    syntactically-valid template output if ``ruff`` isn't on PATH.
    """
    try:
        result = subprocess.run(
            ["ruff", "format", "-"],
            input=source,
            capture_output=True,
            text=True,
            check=True,
        )
    except (FileNotFoundError, subprocess.CalledProcessError):
        return source
    return result.stdout

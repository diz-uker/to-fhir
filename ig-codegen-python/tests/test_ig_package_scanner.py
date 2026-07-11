"""Exercises the scanner against FHIR packages actually restored to the local Firely Terminal
cache (``~/.fhir/packages``), as listed in ``tests/resources/package.json``. Skips if the packages
aren't present (e.g. on a machine that hasn't run ``fhir restore``).
"""

from pathlib import Path

import pytest

from ig_codegen.ig_package_scanner import IgPackageScanner

PACKAGE_NAME = "de.medizininformatikinitiative.kerndatensatz.onkologie"
PACKAGE_VERSION = "2026.0.3"


@pytest.fixture
def model():
    scanner = IgPackageScanner()
    fhir_packages_dir = Path.home() / ".fhir" / "packages"
    package_content_dir = scanner.resolve_package_content_dir(
        fhir_packages_dir, PACKAGE_NAME, PACKAGE_VERSION
    )
    if not package_content_dir.is_dir():
        pytest.skip(f"FHIR package not restored locally: {package_content_dir}")
    return scanner.scan(package_content_dir, PACKAGE_NAME, PACKAGE_VERSION)


def test_classifies_code_systems_by_plain_url(model):
    assert "MII_CS_ONKO_INTENTION" in model.code_systems
    assert model.code_systems["MII_CS_ONKO_INTENTION"] == (
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/CodeSystem/"
        "mii-cs-onko-intention"
    )


def test_classifies_profiles_by_versioned_url(model):
    value = model.profiles.get("MII_PR_ONKO_ALLGEMEINER_LEISTUNGSZUSTAND_ECOG")
    assert value is not None
    assert value.endswith(f"|{PACKAGE_VERSION}")
    assert "StructureDefinition/mii-pr-onko-allgemeiner-leistungszustand-ecog" in value


def test_classifies_extensions_by_plain_url(model):
    value = model.extensions.get("MII_EX_ONKO_STRAHLENTHERAPIE_BESTRAHLUNG_EINZELDOSIS")
    assert value == (
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/StructureDefinition/"
        "mii-ex-onko-strahlentherapie-bestrahlung-einzeldosis"
    )


def test_excludes_logical_models_and_base_type_specializations(model):
    assert model.profiles
    assert all("|" in value for value in model.profiles.values())


def test_expands_complete_code_system_concepts_into_concept_constants(model):
    intention = model.code_system_concepts.get("MII_CS_ONKO_INTENTION")
    assert intention is not None
    assert len(intention) == 7
    assert any(
        c.constant_name == "K" and c.code == "K" and c.display == "kurativ" for c in intention
    )


def test_does_not_expand_non_complete_code_systems(model):
    # mii-cs-onko-krk-operationstyp has content == "fragment" in this package, not "complete".
    assert "MII_CS_ONKO_KRK_OPERATIONSTYP" not in model.code_system_concepts

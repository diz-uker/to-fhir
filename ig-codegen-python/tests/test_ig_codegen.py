"""End-to-end: drives ``tests/resources/package.json`` (this module's own manifest, listing the
packages restored for development/testing) against the local Firely Terminal cache.
"""

from pathlib import Path

import pytest

from ig_codegen.ig_codegen import IgCodegen
from ig_codegen.ig_package_scanner import IgPackageScanner

_RESOURCES_DIR = Path(__file__).parent / "resources"


def test_generates_one_module_per_non_skipped_dependency(tmp_path: Path) -> None:
    package_json_file = _RESOURCES_DIR / "package.json"
    fhir_packages_dir = Path.home() / ".fhir" / "packages"
    onkologie_content_dir = IgPackageScanner().resolve_package_content_dir(
        fhir_packages_dir,
        "de.medizininformatikinitiative.kerndatensatz.onkologie",
        "2026.0.3",
    )
    if not onkologie_content_dir.is_dir():
        pytest.skip(f"FHIR package not restored locally: {onkologie_content_dir}")

    generated_files = IgCodegen().generate(package_json_file, fhir_packages_dir, tmp_path)

    assert generated_files
    onkologie = tmp_path.joinpath(
        "de", "medizininformatikinitiative", "kerndatensatz", "onkologie", "__init__.py"
    )
    assert onkologie in generated_files
    assert onkologie.exists()
    # hl7.fhir.r4.core has no IG-specific canonical prefix and must be skipped.
    assert not any("hl7" in str(f) for f in generated_files)


def test_default_fhir_packages_dir_prefers_home_dir_when_it_exists(tmp_path: Path) -> None:
    fake_home = tmp_path / "home"
    home_fhir_packages = fake_home / ".fhir" / "packages"
    home_fhir_packages.mkdir(parents=True)

    assert IgCodegen.default_fhir_packages_dir(str(fake_home), Path()) == home_fhir_packages


def test_default_fhir_packages_dir_falls_back_to_cwd_when_home_dir_is_missing(
    tmp_path: Path,
) -> None:
    fake_home = tmp_path / "home"
    fake_cwd = tmp_path / "cwd"
    cwd_fhir_packages = fake_cwd / ".fhir" / "packages"
    cwd_fhir_packages.mkdir(parents=True)

    assert IgCodegen.default_fhir_packages_dir(str(fake_home), fake_cwd) == cwd_fhir_packages


def test_default_fhir_packages_dir_falls_back_to_node_modules_when_neither_fhir_dir_exists(
    tmp_path: Path,
) -> None:
    fake_home = tmp_path / "home"
    fake_cwd = tmp_path / "cwd"
    fake_cwd.mkdir(parents=True)

    assert (
        IgCodegen.default_fhir_packages_dir(str(fake_home), fake_cwd) == fake_cwd / "node_modules"
    )

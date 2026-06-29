"""Exercises :meth:`IgPackageScanner.resolve_package_content_dir` with synthetic directory trees,
to cover the Firely Terminal vs. npm layout fallback without depending on real restored packages.
"""

import json
from pathlib import Path

from ig_codegen.ig_package_scanner import IgPackageScanner

PACKAGE_NAME = "de.example.onkologie"
PACKAGE_VERSION = "1.0.0"


def test_prefers_firely_terminal_layout_when_both_exist(tmp_path: Path) -> None:
    scanner = IgPackageScanner()
    firely_layout = tmp_path / f"{PACKAGE_NAME}#{PACKAGE_VERSION}" / "package"
    firely_layout.mkdir(parents=True)
    (tmp_path / PACKAGE_NAME).mkdir()  # npm-style, also present

    assert (
        scanner.resolve_package_content_dir(tmp_path, PACKAGE_NAME, PACKAGE_VERSION)
        == firely_layout
    )


def test_falls_back_to_flat_npm_layout_when_firely_layout_is_missing(tmp_path: Path) -> None:
    scanner = IgPackageScanner()
    npm_layout = tmp_path / PACKAGE_NAME
    npm_layout.mkdir()

    assert (
        scanner.resolve_package_content_dir(tmp_path, PACKAGE_NAME, PACKAGE_VERSION) == npm_layout
    )


def test_scans_resources_from_npm_layout(tmp_path: Path) -> None:
    scanner = IgPackageScanner()
    npm_layout = tmp_path / PACKAGE_NAME
    npm_layout.mkdir()
    (npm_layout / "CodeSystem-mii-cs-onko-intention.json").write_text(
        json.dumps(
            {
                "resourceType": "CodeSystem",
                "id": "mii-cs-onko-intention",
                "url": "https://example.org/CodeSystem/mii-cs-onko-intention",
                "version": "1.0.0",
                "content": "complete",
                "concept": [{"code": "K", "display": "kurativ"}],
            }
        )
    )

    package_content_dir = scanner.resolve_package_content_dir(
        tmp_path, PACKAGE_NAME, PACKAGE_VERSION
    )
    model = scanner.scan(package_content_dir, PACKAGE_NAME, PACKAGE_VERSION)

    assert model.code_systems["MII_CS_ONKO_INTENTION"] == (
        "https://example.org/CodeSystem/mii-cs-onko-intention"
    )

"""Tests for ig_package_scanner — resource reading and classification."""

from __future__ import annotations

import os
import re
from pathlib import Path

import pytest

from ig_codegen.ig_package_scanner import resolve_package_content_dir, scan

PACKAGE_NAME = "de.example.onkologie"
PACKAGE_VERSION = "1.0.0"

CODE_SYSTEM = """
{
  "resourceType": "CodeSystem",
  "id": "mii-cs-onko-intention",
  "url": "https://example.org/CodeSystem/mii-cs-onko-intention",
  "version": "1.0.0",
  "content": "complete",
  "concept": [{"code": "K", "display": "kurativ"}]
}
"""


def _package_content_dir(tmp_path: Path) -> Path:
    content_dir = tmp_path / PACKAGE_NAME
    content_dir.mkdir()
    return content_dir


def _scan(tmp_path: Path):
    return scan(
        resolve_package_content_dir(tmp_path, PACKAGE_NAME, PACKAGE_VERSION),
        PACKAGE_NAME,
        PACKAGE_VERSION,
    )


class TestReadResources:
    def test_raises_on_a_malformed_resource_file(self, tmp_path: Path) -> None:
        content_dir = _package_content_dir(tmp_path)
        (content_dir / "CodeSystem-broken.json").write_text("{not json", encoding="utf-8")

        with pytest.raises(ValueError, match=re.escape("CodeSystem-broken.json")):
            _scan(tmp_path)

    @pytest.mark.skipif(
        hasattr(os, "geteuid") and os.geteuid() == 0,
        reason="root bypasses file permissions",
    )
    def test_raises_on_an_unreadable_resource_file(self, tmp_path: Path) -> None:
        content_dir = _package_content_dir(tmp_path)
        unreadable = content_dir / "CodeSystem-unreadable.json"
        unreadable.write_text(CODE_SYSTEM, encoding="utf-8")
        unreadable.chmod(0o000)

        try:
            with pytest.raises(OSError):
                _scan(tmp_path)
        finally:
            unreadable.chmod(0o644)

    def test_ignores_resource_types_it_generates_nothing_from(self, tmp_path: Path) -> None:
        content_dir = _package_content_dir(tmp_path)
        (content_dir / "CodeSystem-mii-cs-onko-intention.json").write_text(
            CODE_SYSTEM, encoding="utf-8"
        )
        # Library.type is a CodeableConcept, where StructureDefinition.type is a plain code.
        (content_dir / "Library-mii-lib-onko-synthesize-tnm.json").write_text(
            """
            {
              "resourceType": "Library",
              "id": "mii-lib-onko-synthesize-tnm",
              "url": "https://example.org/Library/mii-lib-onko-synthesize-tnm",
              "type": {"coding": [{"code": "logic-library"}]}
            }
            """,
            encoding="utf-8",
        )

        model = _scan(tmp_path)

        assert model.code_systems["MII_CS_ONKO_INTENTION"] == (
            "https://example.org/CodeSystem/mii-cs-onko-intention"
        )
        assert model.profiles == {}
        assert model.extensions == {}

    def test_reads_a_naming_system_whose_type_is_a_codeable_concept(self, tmp_path: Path) -> None:
        content_dir = _package_content_dir(tmp_path)
        (content_dir / "NamingSystem-mii-ns-onko-beispiel.json").write_text(
            """
            {
              "resourceType": "NamingSystem",
              "id": "mii-ns-onko-beispiel",
              "kind": "identifier",
              "description": "Beispiel-Namenssystem",
              "type": {"coding": [{"code": "NI"}]},
              "uniqueId": [{"type": "uri", "value": "https://example.org/ns/beispiel"}]
            }
            """,
            encoding="utf-8",
        )

        model = _scan(tmp_path)

        naming_system = model.naming_systems["MII_NS_ONKO_BEISPIEL"]
        assert naming_system.description == "Beispiel-Namenssystem"
        assert naming_system.by_type["uri"] == ["https://example.org/ns/beispiel"]

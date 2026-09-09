"""Tests for fhir_codings — the default Coding templates."""

from __future__ import annotations

from collections.abc import Callable

import pytest
from fhir.resources.R4B.coding import Coding

from to_fhir import fhir_codings, fhir_systems

TEMPLATES: list[tuple[Callable[[], Coding], str]] = [
    (fhir_codings.loinc, fhir_systems.LOINC),
    (fhir_codings.snomed, fhir_systems.SNOMED),
    (fhir_codings.ops, fhir_systems.OPS),
    (fhir_codings.atc, fhir_systems.ATC),
    (fhir_codings.icd10gm, fhir_systems.ICD10GM),
    (fhir_codings.pzn, fhir_systems.PZN),
]


class TestTemplates:
    @pytest.mark.parametrize(("template", "expected_system"), TEMPLATES)
    def test_has_system_but_no_code(
        self, template: Callable[[], Coding], expected_system: str
    ) -> None:
        coding = template()

        assert coding.system == expected_system
        assert coding.code is None
        assert coding.display is None

    @pytest.mark.parametrize(("template", "expected_system"), TEMPLATES)
    def test_returns_fresh_instance(
        self, template: Callable[[], Coding], expected_system: str
    ) -> None:
        first = template()
        second = template()

        assert first is not second
        assert second.system == expected_system

    def test_loinc_carries_version(self) -> None:
        assert fhir_codings.loinc().version == "2.82"

    def test_pzn_has_no_version(self) -> None:
        assert fhir_codings.pzn().version is None

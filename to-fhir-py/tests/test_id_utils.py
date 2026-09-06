"""Tests for id_utils — deterministic FHIR resource ids."""

from __future__ import annotations

import pytest
from fhir.resources.R4B.identifier import Identifier

from to_fhir import id_utils

EXPECTED_ID = "ab0ecec0f0b7f2e7d0034eb57fceee58120d2ecb95d4e05c2613143ae439f652"


def patient_identifier(
    value: str = "12345", system: str = "http://example.com/patient"
) -> Identifier:
    return Identifier(system=system, value=value)


class TestFromIdentifier:
    def test_computes_id(self) -> None:
        assert id_utils.from_identifier(patient_identifier()) == EXPECTED_ID

    def test_is_consistent(self) -> None:
        assert id_utils.from_identifier(patient_identifier()) == id_utils.from_identifier(
            patient_identifier()
        )

    def test_different_values_differ(self) -> None:
        assert id_utils.from_identifier(patient_identifier(value="12345")) != (
            id_utils.from_identifier(patient_identifier(value="54321"))
        )

    def test_different_systems_differ(self) -> None:
        assert id_utils.from_identifier(
            patient_identifier(system="http://example.com/patient")
        ) != id_utils.from_identifier(patient_identifier(system="http://other.com/patient"))

    # An empty Identifier.value is already rejected by fhir.resources itself, so only the
    # cases it does let through are covered here.
    @pytest.mark.parametrize(
        ("system", "value"),
        [
            (None, "12345"),
            ("", "12345"),
            (" ", "12345"),
            ("http://example.com/patient", None),
            ("http://example.com/patient", " "),
        ],
    )
    def test_blank_identifier_raises(self, system: str | None, value: str | None) -> None:
        identifier = Identifier(system=system, value=value)

        with pytest.raises(ValueError, match="must not be blank"):
            id_utils.from_identifier(identifier)

    def test_with_resource_type(self) -> None:
        assert id_utils.from_identifier(patient_identifier(), "Patient") == f"Patient/{EXPECTED_ID}"

    def test_with_explicit_algorithm(self) -> None:
        resource_id = id_utils.from_identifier(patient_identifier(), algorithm="sha512")

        assert len(resource_id) == 128
        assert resource_id != EXPECTED_ID

    def test_with_resource_type_and_explicit_algorithm(self) -> None:
        resource_id = id_utils.from_identifier(
            patient_identifier(), "Observation", algorithm="sha512"
        )

        assert resource_id.startswith("Observation/")
        assert resource_id.endswith(
            id_utils.from_identifier(patient_identifier(), algorithm="sha512")
        )

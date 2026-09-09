"""Tests for reference_utils — creating references to FHIR resources."""

from __future__ import annotations

import pytest
from fhir.resources.R4B.identifier import Identifier
from fhir.resources.R4B.observation import Observation
from fhir.resources.R4B.patient import Patient

from to_fhir import create_reference_to, create_reference_to_identifier


class TestCreateReferenceTo:
    def test_patient(self) -> None:
        reference = create_reference_to(Patient(id="patient-123"))

        assert reference.reference == "Patient/patient-123"

    def test_observation(self) -> None:
        reference = create_reference_to(Observation(id="obs-456", status="final", code=None))

        assert reference.reference == "Observation/obs-456"

    # Blank and whitespace-only ids are already rejected by fhir.resources itself, so an
    # unset id is the only case that reaches our own check.
    def test_without_id_raises(self) -> None:
        with pytest.raises(ValueError, match="non-blank id"):
            create_reference_to(Patient())


class TestCreateReferenceToIdentifier:
    def test_hashes_the_identifier(self) -> None:
        identifier = Identifier(system="http://example.com/patient", value="12345")

        reference = create_reference_to_identifier(identifier, "Patient")

        assert reference.reference == (
            "Patient/ab0ecec0f0b7f2e7d0034eb57fceee58120d2ecb95d4e05c2613143ae439f652"
        )

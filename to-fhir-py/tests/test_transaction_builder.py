"""Tests for transaction_builder — building FHIR transaction bundles."""

from __future__ import annotations

from collections.abc import Callable

import pytest
from fhir.resources.R4B.bundle import Bundle
from fhir.resources.R4B.device import Device
from fhir.resources.R4B.identifier import Identifier
from fhir.resources.R4B.observation import Observation
from fhir.resources.R4B.patient import Patient
from fhir.resources.R4B.provenance import Provenance
from fhir.resources.R4B.reference import Reference
from fhir.resources.R4B.resource import Resource
from syrupy.assertion import SnapshotAssertion

from to_fhir import TransactionBuilder

BUNDLE_TYPES = [
    "document",
    "message",
    "transaction",
    "transaction-response",
    "batch",
    "batch-response",
    "history",
    "searchset",
    "collection",
]

FhirJson = Callable[[Resource], str]


def make_patient() -> Patient:
    return Patient(id="test-patient", extension=[{"url": "test", "valueCode": "test"}])


def make_observation() -> Observation:
    return Observation(id="test-observation", status="final", code=None)


def provenance_of(bundle: Bundle) -> Provenance:
    provenances = [
        entry.resource for entry in bundle.entry or [] if isinstance(entry.resource, Provenance)
    ]
    assert len(provenances) == 1
    return provenances[0]


class TestBuild:
    def test_single_entry(self, fhir_json: FhirJson, snapshot_json: SnapshotAssertion) -> None:
        bundle = TransactionBuilder().add_entry(make_patient()).build()

        assert fhir_json(bundle) == snapshot_json

    def test_multiple_entries(self, fhir_json: FhirJson, snapshot_json: SnapshotAssertion) -> None:
        bundle = (
            TransactionBuilder()
            .with_id("test-patient")
            .add_entries(make_patient(), make_observation())
            .build()
        )

        assert fhir_json(bundle) == snapshot_json

    def test_multiple_entries_and_provenance(
        self, fhir_json: FhirJson, snapshot_json: SnapshotAssertion
    ) -> None:
        bundle = (
            TransactionBuilder()
            .with_id("test-patient")
            .with_provenance(
                Reference(
                    reference="Device/the-etl-job",
                    display="The test etl job in version 1.2.3",
                ),
                Reference(display="The source system"),
            )
            .add_entries(make_patient(), make_observation())
            .add_delete_entries(
                Reference(reference="Observation/test-observation-to-delete"),
                Reference(reference="Observation/test-observation-to-delte-as-well"),
            )
            .build()
        )

        assert fhir_json(bundle) == snapshot_json

    def test_default_bundle_type(self) -> None:
        assert TransactionBuilder().build().type == "transaction"

    @pytest.mark.parametrize("bundle_type", BUNDLE_TYPES)
    def test_different_bundle_types(self, bundle_type: str) -> None:
        assert TransactionBuilder().with_type(bundle_type).build().type == bundle_type

    def test_add_single_entry(self) -> None:
        patient = make_patient()

        bundle = TransactionBuilder().add_entry(patient).build()

        assert len(bundle.entry) == 1
        assert bundle.entry[0].resource is patient

    def test_add_multiple_entries_individually(self) -> None:
        patient1 = Patient(id="patient-1")
        patient2 = Patient(id="patient-2")

        bundle = TransactionBuilder().add_entry(patient1).add_entry(patient2).build()

        assert [entry.resource for entry in bundle.entry] == [patient1, patient2]

    def test_add_entries_from_iterable(self) -> None:
        patients = [Patient(id="patient-1"), Patient(id="patient-2")]

        bundle = TransactionBuilder().add_entries(*patients).build()

        assert len(bundle.entry) == 2

    def test_empty_bundle_has_no_entries(self) -> None:
        assert TransactionBuilder().build().entry is None

    def test_full_url_is_relative_reference_by_default(self) -> None:
        bundle = TransactionBuilder().add_entry(make_patient()).build()

        assert bundle.entry[0].fullUrl == "Patient/test-patient"

    def test_full_url_base_builds_absolute_full_url(self) -> None:
        bundle = (
            TransactionBuilder()
            .with_full_url_base("https://example.org/fhir")
            .add_entry(make_patient())
            .build()
        )

        assert bundle.entry[0].fullUrl == "https://example.org/fhir/Patient/test-patient"
        assert bundle.entry[0].request.url == "Patient/test-patient"

    def test_full_url_base_handles_trailing_slash(self) -> None:
        bundle = (
            TransactionBuilder()
            .with_full_url_base("https://example.org/fhir/")
            .add_entry(make_patient())
            .build()
        )

        assert bundle.entry[0].fullUrl == "https://example.org/fhir/Patient/test-patient"

    def test_chained_configuration(self) -> None:
        patient = make_patient()

        bundle = (
            TransactionBuilder().with_type("batch").with_id(patient.id).add_entry(patient).build()
        )

        assert bundle.type == "batch"
        assert bundle.entry[0].request.method == "PUT"
        assert bundle.id == "test-patient"

    @pytest.mark.parametrize("bundle_id", ["bundle-1", "123", "my-bundle-id"])
    def test_with_id(self, bundle_id: str) -> None:
        assert TransactionBuilder().with_id(bundle_id).build().id == bundle_id


class TestDuplicateEntries:
    def test_raises_on_duplicate_resource_ids(self) -> None:
        builder = (
            TransactionBuilder()
            .fail_on_duplicate_entries()
            .add_entry(Patient(id="patient-123"))
            .add_entry(Patient(id="patient-123"))
        )

        with pytest.raises(ValueError, match="Patient/patient-123"):
            builder.build()

    def test_no_error_when_flag_not_enabled(self) -> None:
        bundle = (
            TransactionBuilder()
            .add_entry(Patient(id="patient-123"))
            .add_entry(Patient(id="patient-123"))
            .build()
        )

        assert len(bundle.entry) == 2

    def test_different_resource_types_with_same_id_allowed(self) -> None:
        bundle = (
            TransactionBuilder()
            .fail_on_duplicate_entries()
            .add_entry(Patient(id="resource-123"))
            .add_entry(Observation(id="resource-123", status="final", code=None))
            .build()
        )

        assert len(bundle.entry) == 2

    def test_raises_with_multiple_duplicates(self) -> None:
        builder = (
            TransactionBuilder()
            .fail_on_duplicate_entries()
            .add_entries(
                Patient(id="patient-1"),
                Patient(id="patient-1"),
                Patient(id="patient-2"),
                Patient(id="patient-2"),
            )
        )

        with pytest.raises(ValueError, match="Duplicate resource added"):
            builder.build()


class TestBuildWithSeparateProvenance:
    def test_with_device(self, fhir_json: FhirJson, snapshot_json: SnapshotAssertion) -> None:
        result = (
            TransactionBuilder()
            .with_id("test-patient")
            .with_provenance(Device(id="the-etl-job"), Reference(display="The source system"))
            .add_entries(make_patient(), make_observation())
            .add_delete_entries(
                Reference(reference="Observation/test-observation-to-delete"),
                Reference(reference="Observation/test-observation-to-delete-as-well"),
            )
            .build_with_separate_provenance()
        )

        assert fhir_json(result.data_bundle) == snapshot_json(name="data")
        assert fhir_json(result.provenance_bundle) == snapshot_json(name="provenance")

    def test_without_device(self, fhir_json: FhirJson, snapshot_json: SnapshotAssertion) -> None:
        data_bundle, provenance_bundle = (
            TransactionBuilder()
            .with_provenance(
                Reference(
                    reference="Device/the-etl-job",
                    display="The test etl job in version 1.2.3",
                ),
                Reference(display="The source system"),
            )
            .add_entry(Patient(id="test-patient"))
            .build_with_separate_provenance()
        )

        assert fhir_json(data_bundle) == snapshot_json(name="data")
        assert fhir_json(provenance_bundle) == snapshot_json(name="provenance")

    def test_raises_without_provenance(self) -> None:
        with pytest.raises(RuntimeError, match="with_provenance"):
            TransactionBuilder().build_with_separate_provenance()

    def test_provenance_bundle_is_always_transaction(self) -> None:
        result = (
            TransactionBuilder()
            .with_type("batch")
            .with_provenance(Reference(reference="Device/the-etl-job"), Reference(display="source"))
            .add_entry(Patient(id="test-patient"))
            .build_with_separate_provenance()
        )

        assert result.data_bundle.type == "batch"
        assert result.provenance_bundle.type == "transaction"


class TestProvenance:
    def test_id_is_deterministic(self) -> None:
        def build() -> Bundle:
            return (
                TransactionBuilder()
                .with_provenance(
                    Reference(reference="Device/the-etl-job"),
                    Reference(display="The source system"),
                )
                .add_entry(Patient(id="test-patient"))
                .build()
            )

        assert provenance_of(build()).id == provenance_of(build()).id

    def test_uses_identifier_when_reference_is_absent(self) -> None:
        bundle = (
            TransactionBuilder()
            .with_provenance(
                Reference(identifier=Identifier(system="http://example.com/etl", value="the-job")),
                Reference(display="The source system"),
            )
            .add_entry(Patient(id="test-patient"))
            .build()
        )

        assert provenance_of(bundle).id is not None

    def test_without_any_identifying_information_raises(self) -> None:
        builder = (
            TransactionBuilder()
            .with_provenance(Reference(display=None), Reference(display="source"))
            .add_entry(Patient(id="test-patient"))
        )

        with pytest.raises(ValueError, match="Invalid provenance who reference"):
            builder.build()

    def test_device_is_added_to_bundle_and_targets(self) -> None:
        device = Device(id="the-etl-job")

        bundle = (
            TransactionBuilder()
            .with_provenance(device, Reference(display="The source system"))
            .add_entry(Patient(id="test-patient"))
            .build()
        )

        assert any(entry.resource is device for entry in bundle.entry)
        assert any(
            target.reference == "Device/the-etl-job" for target in provenance_of(bundle).target
        )

    def test_device_is_not_duplicated_when_already_added_as_entry(self) -> None:
        device = Device(id="the-etl-job")

        bundle = (
            TransactionBuilder()
            .with_provenance(device, Reference(display="The source system"))
            .add_entry(device)
            .build()
        )

        assert sum(entry.resource is device for entry in bundle.entry) == 1

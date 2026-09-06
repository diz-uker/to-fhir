"""Builder for creating FHIR transaction bundles."""

from __future__ import annotations

import hashlib
from datetime import UTC, datetime
from typing import NamedTuple, Self, cast

from fhir.resources.R4B.bundle import Bundle, BundleEntry, BundleEntryRequest
from fhir.resources.R4B.codeableconcept import CodeableConcept
from fhir.resources.R4B.coding import Coding
from fhir.resources.R4B.device import Device
from fhir.resources.R4B.provenance import Provenance, ProvenanceAgent, ProvenanceEntity
from fhir.resources.R4B.reference import Reference
from fhir.resources.R4B.resource import Resource

from to_fhir.reference_utils import create_reference_to

_DATA_OPERATION_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-DataOperation"
_PARTICIPANT_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/provenance-participant-type"
_PARTICIPATION_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType"


class DataAndProvenanceBundles(NamedTuple):
    """A pair of FHIR bundles: one holding data resources, one holding provenance resources."""

    #: The bundle containing data resources (e.g. Patient, Observation).
    data_bundle: Bundle

    #: The bundle containing Provenance and optionally Device resources.
    provenance_bundle: Bundle


class TransactionBuilder:
    """Builder for FHIR transaction bundles, using the update-as-create approach by default."""

    def __init__(self) -> None:
        self._resources: list[Resource] = []
        self._resources_to_delete: list[Reference] = []
        self._bundle_type = "transaction"
        self._bundle_id: str | None = None
        self._full_url_base: str | None = None
        self._fail_on_duplicate_entries = False
        self._provenance_who: Reference | None = None
        self._provenance_what: Reference | None = None
        self._provenance_device: Device | None = None

    def with_type(self, bundle_type: str) -> Self:
        """Set the bundle type, e.g. ``"transaction"`` or ``"batch"``."""
        self._bundle_type = bundle_type
        return self

    def fail_on_duplicate_entries(self) -> Self:
        """Raise when multiple resources with the same id are added, once the bundle is built."""
        self._fail_on_duplicate_entries = True
        return self

    def add_entry(self, resource: Resource) -> Self:
        """Add a FHIR resource to the transaction bundle."""
        self._resources.append(resource)
        return self

    def add_entries(self, *resources: Resource) -> Self:
        """Add FHIR resources to the transaction bundle."""
        self._resources.extend(resources)
        return self

    def add_delete_entry(self, resource: Reference) -> Self:
        """Add a reference to a resource that should be deleted as part of the transaction."""
        self._resources_to_delete.append(resource)
        return self

    def add_delete_entries(self, *resources: Reference) -> Self:
        """Add references to resources that should be deleted as part of the transaction."""
        self._resources_to_delete.extend(resources)
        return self

    def with_id(self, bundle_id: str) -> Self:
        """Set the id of the bundle."""
        self._bundle_id = bundle_id
        return self

    def with_full_url_base(self, base_url: str) -> Self:
        """Set an absolute base URL used to build each entry's ``fullUrl``.

        Each entry's ``fullUrl`` becomes ``<base_url>/<ResourceType>/<id>``, making it absolute as
        FHIR requires. Plain ``ResourceType/id`` references used elsewhere in the bundle (e.g. in a
        ``Provenance.target``) still resolve correctly against it, since those match by the tail of
        a hierarchical ``fullUrl``. The base URL does not need to be a real, dereferenceable server
        endpoint.

        Args:
            base_url: An absolute base URL, e.g. ``https://example.org/fhir``. A trailing slash is
                optional.
        """
        self._full_url_base = base_url.rstrip("/")
        return self

    def with_provenance(self, who: Reference | Device, what: Reference) -> Self:
        """Include a Provenance resource in the bundle.

        If the bundle contains both delete and update/create entries, two Provenance resources are
        included. The ``Provenance.id`` is derived from the hash of ``who`` and ``what``.

        Args:
            who: The agent responsible for the transformation or deletion, as ``Provenance
                .agent.who``. This is typically the transformation service itself. Passing a
                ``Device`` adds that resource to the bundle and references it instead.
            what: A reference to the resource that is the source of the transformation, as
                ``Provenance.entity.what``.
        """
        if isinstance(who, Device):
            self._provenance_device = who
            self._provenance_who = create_reference_to(who)
        else:
            self._provenance_who = who

        self._provenance_what = what
        return self

    def build(self) -> Bundle:
        """Build a FHIR Bundle with the configured type.

        Raises:
            ValueError: :meth:`fail_on_duplicate_entries` is enabled and duplicate resource ids
                were found.
        """
        entries = self._data_entries()

        if self._provenance_enabled:
            device = self._provenance_device
            if device is not None and not any(resource is device for resource in self._resources):
                entries.append(self._put_entry(device))

            entries.extend(self._provenance_entries())

        return _bundle(self._bundle_type, self._bundle_id, entries)

    def build_with_separate_provenance(self) -> DataAndProvenanceBundles:
        """Build the data resources and the provenance resources as two separate bundles.

        The data bundle holds only the data resources and delete entries; the provenance bundle
        holds the Provenance resource(s) and, if configured via :meth:`with_provenance`, the
        Device. The provenance bundle is always of type ``transaction``.

        Raises:
            RuntimeError: Provenance has not been enabled via :meth:`with_provenance`.
            ValueError: :meth:`fail_on_duplicate_entries` is enabled and duplicate resource ids
                were found.
        """
        if not self._provenance_enabled:
            raise RuntimeError(
                "Provenance must be enabled via with_provenance() before calling "
                "build_with_separate_provenance()"
            )

        data_bundle = _bundle(self._bundle_type, self._bundle_id, self._data_entries())

        # Use a SHA-256 hash of "provenance-" + the data bundle id for the provenance bundle id.
        # This keeps the id unique while ensuring it stays within FHIR id length/character
        # constraints.
        # We could also just use the data bundle id, but if both bundles are written to the same
        # topic in Kafka and that id is used as the key, it would cause issues on compaction.
        provenance_bundle_id = (
            None if self._bundle_id is None else _sha256_hex(f"provenance-{self._bundle_id}")
        )

        entries: list[BundleEntry] = []
        if self._provenance_device is not None:
            entries.append(self._put_entry(self._provenance_device))

        entries.extend(self._provenance_entries())

        return DataAndProvenanceBundles(
            data_bundle, _bundle("transaction", provenance_bundle_id, entries)
        )

    @property
    def _provenance_enabled(self) -> bool:
        return self._provenance_who is not None and self._provenance_what is not None

    def _full_url(self, reference: str) -> str:
        """Build an entry's ``fullUrl`` from the resource's relative reference."""
        if self._full_url_base is None:
            return reference
        return f"{self._full_url_base}/{reference}"

    def _put_entry(self, resource: Resource, url: str | None = None) -> BundleEntry:
        url = url if url is not None else cast("str", create_reference_to(resource).reference)
        return BundleEntry(
            fullUrl=self._full_url(url),
            resource=resource,
            request=BundleEntryRequest(method="PUT", url=url),
        )

    def _data_entries(self) -> list[BundleEntry]:
        entries: list[BundleEntry] = []
        seen: set[str] = set()

        for resource in self._resources:
            url = cast("str", create_reference_to(resource).reference)

            if self._fail_on_duplicate_entries and url in seen:
                raise ValueError(f"Duplicate resource added: {url}")

            seen.add(url)
            entries.append(self._put_entry(resource, url))

        entries.extend(
            BundleEntry(request=BundleEntryRequest(method="DELETE", url=reference.reference))
            for reference in self._resources_to_delete
        )

        return entries

    def _provenance_entries(self) -> list[BundleEntry]:
        entries: list[BundleEntry] = []

        if self._resources:
            entries.append(self._put_entry(self._build_create_provenance()))

        if self._resources_to_delete:
            entries.append(self._put_entry(self._build_delete_provenance()))

        return entries

    def _require_provenance(self) -> tuple[Reference, Reference]:
        if self._provenance_who is None or self._provenance_what is None:
            raise RuntimeError(
                "Provenance must be enabled via with_provenance() before building a "
                "Provenance resource"
            )
        return self._provenance_who, self._provenance_what

    def _build_create_provenance(self) -> Provenance:
        who, what = self._require_provenance()
        now = datetime.now(UTC)

        targets = [create_reference_to(resource) for resource in self._resources]

        if self._provenance_device is not None:
            # If a device is configured as the provenance agent, we also want to include a
            # reference to it in the targets list.
            device_reference = create_reference_to(self._provenance_device)
            if all(target.reference != device_reference.reference for target in targets):
                targets.append(device_reference)

        return Provenance(
            id=_sha256_hex(f"create-{_provenance_id_string(who, what)}"),
            occurredDateTime=now,
            recorded=now,
            target=targets,
            activity=_concept(_DATA_OPERATION_SYSTEM, "CREATE", "create"),
            agent=[
                ProvenanceAgent(
                    type=_concept(_PARTICIPANT_TYPE_SYSTEM, "assembler", "Assembler"),
                    role=[_concept(_PARTICIPATION_TYPE_SYSTEM, "AUT", "author")],
                    who=who,
                )
            ],
            entity=[ProvenanceEntity(role="source", what=what)],
        )

    def _build_delete_provenance(self) -> Provenance:
        who, what = self._require_provenance()
        now = datetime.now(UTC)

        return Provenance(
            id=_sha256_hex(f"delete-{_provenance_id_string(who, what)}"),
            occurredDateTime=now,
            recorded=now,
            target=list(self._resources_to_delete),
            activity=_concept(_DATA_OPERATION_SYSTEM, "DELETE", "delete"),
            agent=[
                ProvenanceAgent(
                    type=_concept(_PARTICIPANT_TYPE_SYSTEM, "performer", "Performer"),
                    who=who,
                )
            ],
            entity=[
                ProvenanceEntity(role="removal", what=to_delete)
                for to_delete in self._resources_to_delete
            ],
        )


def _bundle(bundle_type: str, bundle_id: str | None, entries: list[BundleEntry]) -> Bundle:
    return Bundle(type=bundle_type, id=bundle_id, entry=entries or None)


def _concept(system: str, code: str, display: str) -> CodeableConcept:
    return CodeableConcept(coding=[Coding(system=system, code=code, display=display)])


def _sha256_hex(value: str) -> str:
    return hashlib.sha256(value.encode()).hexdigest()


def _provenance_id_string(who: Reference, what: Reference) -> str:
    return f"{_describe(who, 'who')}-{_describe(what, 'what')}"


def _describe(reference: Reference, name: str) -> str:
    """Reduce a reference to the most specific stable string available.

    That is its literal reference, else its identifier value, else its display - for use in a
    deterministic Provenance id.
    """
    literal = reference.reference
    if literal and literal.strip():
        return literal

    if reference.identifier is not None and reference.identifier.value is not None:
        return reference.identifier.value

    if reference.display is not None:
        return reference.display

    raise ValueError(
        f"Invalid provenance {name} reference. "
        f"Either reference, identifier or display must be provided."
    )

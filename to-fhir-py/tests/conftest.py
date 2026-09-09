"""Shared fixtures for the to-fhir tests."""

from __future__ import annotations

import re
from collections.abc import Callable

import pytest
from fhir.resources.R4B.resource import Resource
from syrupy.assertion import SnapshotAssertion
from syrupy.extensions.single_file import SingleFileSnapshotExtension, WriteMode

# Provenance.occurred and Provenance.recorded are wall-clock timestamps, so they would differ on
# every run.
_PROVENANCE_TIMESTAMPS = re.compile(r'"(occurredDateTime|recorded)": "[^"]*"')


class JsonSnapshotExtension(SingleFileSnapshotExtension):
    """Store each snapshot as its own .json file, mirroring the Java and C# snapshot layout."""

    file_extension = "json"
    _write_mode = WriteMode.TEXT


@pytest.fixture
def snapshot_json(snapshot: SnapshotAssertion) -> SnapshotAssertion:
    """A snapshot assertion that writes plain .json files."""
    return snapshot.use_extension(JsonSnapshotExtension)


@pytest.fixture
def fhir_json() -> Callable[[Resource], str]:
    """Serialize a resource as pretty-printed FHIR JSON, with timestamps scrubbed."""

    def serialize(resource: Resource) -> str:
        return _PROVENANCE_TIMESTAMPS.sub(
            r'"\1": "2000-01-01T11:11:11Z"', resource.model_dump_json(indent=2)
        )

    return serialize

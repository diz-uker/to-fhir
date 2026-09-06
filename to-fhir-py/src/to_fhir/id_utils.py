"""Utilities for deterministic FHIR resource ids."""

from __future__ import annotations

import hashlib

from fhir.resources.R4B.identifier import Identifier

#: The hash algorithm used unless a different :mod:`hashlib` algorithm is requested.
DEFAULT_ALGORITHM = "sha256"


def from_identifier(
    identifier: Identifier,
    resource_type: str | None = None,
    *,
    algorithm: str = DEFAULT_ALGORITHM,
) -> str:
    """Compute a deterministic id from a FHIR identifier by hashing its system and value.

    Args:
        identifier: The FHIR identifier to compute the id from.
        resource_type: When given, the id is returned as the relative reference
            ``<resource_type>/<id>`` instead of the bare id.
        algorithm: The :mod:`hashlib` algorithm to hash with, e.g. ``"sha256"``.

    Returns:
        The deterministic id, optionally qualified with ``resource_type``.

    Raises:
        ValueError: The identifier's system or value is missing or blank.
    """
    if not (identifier.system or "").strip():
        raise ValueError("Identifier system must not be blank.")

    if not (identifier.value or "").strip():
        raise ValueError(f"Identifier value must not be blank. System: {identifier.system}")

    digest = hashlib.new(algorithm, f"{identifier.system}|{identifier.value}".encode())
    resource_id = digest.hexdigest()

    if resource_type is None:
        return resource_id

    return f"{resource_type}/{resource_id}"

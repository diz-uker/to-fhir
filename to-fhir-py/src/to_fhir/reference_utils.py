"""Utilities for creating FHIR references to resources."""

from __future__ import annotations

from fhir.resources.R4B.identifier import Identifier
from fhir.resources.R4B.reference import Reference
from fhir.resources.R4B.resource import Resource

from to_fhir import id_utils


def create_reference_to(resource: Resource) -> Reference:
    """Create a reference to ``resource``, in the form ``ResourceType/id``.

    Args:
        resource: The FHIR resource to create a reference to. It must have a non-blank id.

    Raises:
        ValueError: The resource has no id, or a blank one.
    """
    if not (resource.id or "").strip():
        raise ValueError(
            f"Resource of type {resource.get_resource_type()} must have a "
            f"non-blank id to be referenced."
        )

    return Reference(reference=f"{resource.get_resource_type()}/{resource.id}")


def create_reference_to_identifier(
    identifier: Identifier,
    resource_type: str,
    *,
    algorithm: str = id_utils.DEFAULT_ALGORITHM,
) -> Reference:
    """Create a reference to the resource identified by ``identifier``.

    The reference is of the form ``<resource_type>/sha256(system|value)``.

    Args:
        identifier: An identifier of the FHIR resource to create a reference to.
        resource_type: The resource type to include in the reference.
        algorithm: The :mod:`hashlib` algorithm to hash with.

    Raises:
        ValueError: The identifier's system or value is missing or blank.
    """
    return Reference(
        reference=id_utils.from_identifier(identifier, resource_type, algorithm=algorithm)
    )

"""The subset of a FHIR resource JSON file's fields needed to classify it."""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(frozen=True)
class Concept:
    """A CodeSystem.concept entry; ``concept`` holds nested child concepts, if any."""

    code: str | None = None
    display: str | None = None
    concept: list[Concept] = field(default_factory=list)

    @staticmethod
    def from_json(data: dict) -> Concept:
        return Concept(
            code=data.get("code"),
            display=data.get("display"),
            concept=[Concept.from_json(c) for c in data.get("concept") or []],
        )


@dataclass(frozen=True)
class FhirResourceSummary:
    resource_type: str | None = None
    id: str | None = None
    url: str | None = None
    version: str | None = None
    kind: str | None = None
    derivation: str | None = None
    type: str | None = None
    content: str | None = None
    concept: list[Concept] = field(default_factory=list)

    @staticmethod
    def from_json(data: dict) -> FhirResourceSummary:
        return FhirResourceSummary(
            resource_type=data.get("resourceType"),
            id=data.get("id"),
            url=data.get("url"),
            version=data.get("version"),
            kind=data.get("kind"),
            derivation=data.get("derivation"),
            type=data.get("type"),
            content=data.get("content"),
            concept=[Concept.from_json(c) for c in data.get("concept") or []],
        )

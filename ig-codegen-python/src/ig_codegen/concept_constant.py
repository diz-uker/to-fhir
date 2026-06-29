"""A single, already name-sanitized CodeSystem concept, ready to render as an enum member."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class ConceptConstant:
    constant_name: str
    code: str
    display: str | None = None

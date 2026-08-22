"""Converts FHIR resource ids (kebab-case or PascalCase) into Python identifier names."""

from __future__ import annotations

import re

_LOWER_OR_DIGIT_FOLLOWED_BY_UPPER = re.compile(r"(?<=[a-z0-9])(?=[A-Z])")
_NON_ALPHANUMERIC_RUN = re.compile(r"[^A-Za-z0-9]+")


def to_constant_name(s: str) -> str:
    """Converts a FHIR id to UPPER_SNAKE_CASE."""
    with_boundaries = _LOWER_OR_DIGIT_FOLLOWED_BY_UPPER.sub("_", s)
    parts = [p.upper() for p in _NON_ALPHANUMERIC_RUN.split(with_boundaries) if p]
    name = "_".join(parts)
    if not name:
        return "_"
    if name[0].isdigit():
        return "_" + name
    return name


def to_pascal_case(segment: str) -> str:
    """Converts a FHIR id or dot-separated package name segment to PascalCase."""
    with_boundaries = _LOWER_OR_DIGIT_FOLLOWED_BY_UPPER.sub("_", segment)
    parts = _NON_ALPHANUMERIC_RUN.split(with_boundaries)
    name = "".join((w[0].upper() + w[1:].lower()) for w in parts if w)
    if not name:
        return "_"
    if name[0].isdigit():
        return "_" + name
    return name


def to_snake_case(s: str) -> str:
    """Converts a FHIR id (kebab-case or camelCase) to snake_case."""
    with_boundaries = _LOWER_OR_DIGIT_FOLLOWED_BY_UPPER.sub("_", s)
    parts = [p.lower() for p in _NON_ALPHANUMERIC_RUN.split(with_boundaries) if p]
    name = "_".join(parts)
    if not name:
        return "_"
    if name[0].isdigit():
        return "_" + name
    return name


def to_enum_member_name(code: str) -> str:
    """Converts a CodeSystem concept code to a Python enum member name (UPPER_SNAKE_CASE).

    Codes ending with '+' or '-' get a '_POS' or '_NEG' suffix.
    """
    if code.endswith("+"):
        base, extra = code[:-1], "_POS"
    elif code.endswith("-"):
        base, extra = code[:-1], "_NEG"
    else:
        base, extra = code, ""
    name = to_constant_name(base)
    if name == "_":
        return "_"
    return name + extra

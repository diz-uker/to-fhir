"""Converts FHIR resource ids (kebab-case or, occasionally, PascalCase) into Python constant names."""

import re

_LOWER_OR_DIGIT_FOLLOWED_BY_UPPER = re.compile(r"(?<=[a-z0-9])(?=[A-Z])")
_NON_ALPHANUMERIC_RUN = re.compile(r"[^A-Za-z0-9]+")


def to_constant_name(fhir_id: str) -> str:
    with_word_boundaries = _LOWER_OR_DIGIT_FOLLOWED_BY_UPPER.sub("_", fhir_id)
    return _NON_ALPHANUMERIC_RUN.sub("_", with_word_boundaries).upper()


def to_pascal_case(segment: str) -> str:
    """Converts a dot-separated FHIR package name segment into a PascalCase Python class name."""
    with_word_boundaries = _LOWER_OR_DIGIT_FOLLOWED_BY_UPPER.sub("_", segment)
    words = [word for word in _NON_ALPHANUMERIC_RUN.split(with_word_boundaries) if word]
    return "".join(word[0].upper() + word[1:].lower() for word in words)


def to_snake_case(constant_name: str) -> str:
    """Converts a ``SCREAMING_SNAKE_CASE`` constant name (as produced by :func:`to_constant_name`)
    into a lowercase snake_case Python identifier, e.g. ``MII_PR_DIAGNOSE_CONDITION ->
    mii_pr_diagnose_condition``.
    """
    return constant_name.lower()


def to_enum_constant_name(code: str) -> str:
    """Converts a CodeSystem concept code into a valid Python enum member name. Real FHIR code
    systems use codes that aren't valid Python identifiers as-is - e.g. purely numeric codes
    (``"2"``), or receptor-status codes ending in ``+``/``-`` (``"mol+"``, ``"i-"``), which would
    otherwise collide once the sign is stripped by :func:`to_constant_name`.

    This does not guarantee uniqueness across a whole CodeSystem by itself - callers must still
    disambiguate any remaining collisions (e.g. two codes differing only in characters this
    function treats as equivalent).
    """
    if code.endswith("+"):
        base, suffix = code[:-1], "_POS"
    elif code.endswith("-"):
        base, suffix = code[:-1], "_NEG"
    else:
        base, suffix = code, ""

    constant_name = to_constant_name(base) + suffix
    if not constant_name:
        return "_"
    if constant_name[0].isdigit():
        constant_name = f"_{constant_name}"
    return f"{constant_name}_" if _is_sunder(constant_name) else constant_name


def _is_sunder(name: str) -> bool:
    """``enum.Enum`` reserves single-leading/single-trailing-underscore names (``_sunder_``,
    e.g. ``_100_``) for its own use, unlike Java enum constants which have no such restriction.
    A code like ``"100%"`` produces exactly this shape once the leading-digit prefix is added, so
    it needs an extra trailing underscore to stay a plain, unreserved identifier.
    """
    return (
        len(name) > 2
        and name.startswith("_")
        and name.endswith("_")
        and not name.startswith("__")
        and not name.endswith("__")
    )

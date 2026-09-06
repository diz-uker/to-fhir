"""Factory functions for default :class:`Coding` templates used across to-FHIR®.

Each function returns a fresh instance, since ``Coding`` is mutable.
"""

from __future__ import annotations

from fhir.resources.R4B.coding import Coding

from to_fhir import fhir_systems

_SNOMED_VERSION = "http://snomed.info/sct/900000000000207008/version/20250701"


def loinc() -> Coding:
    """Return a fresh LOINC coding template."""
    return Coding(system=fhir_systems.LOINC, version="2.82")


def snomed() -> Coding:
    """Return a fresh SNOMED CT coding template."""
    return Coding(system=fhir_systems.SNOMED, version=_SNOMED_VERSION)


def ops() -> Coding:
    """Return a fresh OPS coding template."""
    return Coding(system=fhir_systems.OPS, version="2026")


def atc() -> Coding:
    """Return a fresh ATC coding template."""
    return Coding(system=fhir_systems.ATC, version="2026")


def icd10gm() -> Coding:
    """Return a fresh ICD-10-GM coding template."""
    return Coding(system=fhir_systems.ICD10GM, version="2026")


def pzn() -> Coding:
    """Return a fresh PZN coding template."""
    return Coding(system=fhir_systems.PZN)

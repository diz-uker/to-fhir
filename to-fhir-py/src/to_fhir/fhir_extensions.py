"""Factory functions for default :class:`Extension` templates used across to-FHIR®."""

from __future__ import annotations

import enum

from fhir.resources.R4B.coding import Coding
from fhir.resources.R4B.extension import Extension

#: The data-absent-reason extension URL.
DATA_ABSENT_REASON_URL = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"

#: The code system backing :class:`DataAbsentReason`.
DATA_ABSENT_REASON_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/data-absent-reason"


class DataAbsentReason(enum.StrEnum):
    """Codes from the ``data-absent-reason`` code system.

    See `CodeSystem: DataAbsentReason
    <https://hl7.org/fhir/R4B/codesystem-data-absent-reason.html>`_. Members are their own
    FHIR code, so ``DataAbsentReason.NOT_ASKED == "not-asked"``.
    """

    #: The value is expected to exist but is not known.
    UNKNOWN = "unknown"

    #: The source was asked but does not know the value.
    ASKED_UNKNOWN = "asked-unknown"

    #: There is reason to expect (from the workflow) that the value may become known.
    TEMP_UNKNOWN = "temp-unknown"

    #: The workflow didn't lead to this value being known.
    NOT_ASKED = "not-asked"

    #: The source was asked but declined to answer.
    ASKED_DECLINED = "asked-declined"

    #: The information is not available due to security, privacy or related reasons.
    MASKED = "masked"

    #: There is no proper value for this element (e.g. last menstrual period for a male).
    NOT_APPLICABLE = "not-applicable"

    #: The source system wasn't capable of supporting this element.
    UNSUPPORTED = "unsupported"

    #: The content of the data is represented in the resource narrative.
    AS_TEXT = "as-text"

    #: Some system or workflow process error means that the information is not available.
    ERROR = "error"

    #: The numeric value is undefined or unrepresentable due to a floating point error.
    NOT_A_NUMBER = "not-a-number"

    #: The numeric value is excessively low and unrepresentable due to a floating point error.
    NEGATIVE_INFINITY = "negative-infinity"

    #: The numeric value is excessively high and unrepresentable due to a floating point error.
    POSITIVE_INFINITY = "positive-infinity"

    #: The value is not available because the observation procedure was not performed.
    NOT_PERFORMED = "not-performed"

    #: The value is not permitted in this context (e.g. due to profiles, or the base data types).
    NOT_PERMITTED = "not-permitted"

    @property
    def code(self) -> str:
        """The FHIR code for this concept, e.g. ``not-asked``."""
        return self.value

    def coding(self) -> Coding:
        """Return a fresh :class:`Coding` for this concept."""
        return Coding(system=DATA_ABSENT_REASON_CODE_SYSTEM, code=self.value)

    def extension(self) -> Extension:
        """Return a fresh data-absent-reason extension carrying this concept."""
        return data_absent_reason(self)


def data_absent_reason(reason: DataAbsentReason | None = None) -> Extension:
    """Return a fresh data-absent-reason extension.

    Args:
        reason: The reason the value is absent. When omitted, the returned extension is a bare
            template without a value.
    """
    if reason is None:
        return Extension(url=DATA_ABSENT_REASON_URL)
    return Extension(url=DATA_ABSENT_REASON_URL, valueCode=reason.value)

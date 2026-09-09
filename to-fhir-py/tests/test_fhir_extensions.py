"""Tests for fhir_extensions — the default Extension templates."""

from __future__ import annotations

import pytest

from to_fhir import fhir_extensions
from to_fhir.fhir_extensions import DataAbsentReason, data_absent_reason


class TestDataAbsentReasonExtension:
    def test_not_asked(self) -> None:
        extension = data_absent_reason(DataAbsentReason.NOT_ASKED)

        assert extension.url == fhir_extensions.DATA_ABSENT_REASON_URL
        assert extension.valueCode == "not-asked"

    def test_returns_fresh_instance(self) -> None:
        first = data_absent_reason(DataAbsentReason.NOT_ASKED)
        second = data_absent_reason(DataAbsentReason.NOT_ASKED)

        assert first is not second

    def test_template_has_no_value(self) -> None:
        extension = data_absent_reason()

        assert extension.url == fhir_extensions.DATA_ABSENT_REASON_URL
        assert extension.valueCode is None

    def test_enum_extension_matches_factory(self) -> None:
        extension = DataAbsentReason.MASKED.extension()

        assert extension.url == fhir_extensions.DATA_ABSENT_REASON_URL
        assert extension.valueCode == "masked"


class TestDataAbsentReason:
    @pytest.mark.parametrize(
        ("reason", "expected_code"),
        [
            (DataAbsentReason.UNKNOWN, "unknown"),
            (DataAbsentReason.ASKED_UNKNOWN, "asked-unknown"),
            (DataAbsentReason.TEMP_UNKNOWN, "temp-unknown"),
            (DataAbsentReason.NOT_ASKED, "not-asked"),
            (DataAbsentReason.ASKED_DECLINED, "asked-declined"),
            (DataAbsentReason.MASKED, "masked"),
            (DataAbsentReason.NOT_APPLICABLE, "not-applicable"),
            (DataAbsentReason.UNSUPPORTED, "unsupported"),
            (DataAbsentReason.AS_TEXT, "as-text"),
            (DataAbsentReason.ERROR, "error"),
            (DataAbsentReason.NOT_A_NUMBER, "not-a-number"),
            (DataAbsentReason.NEGATIVE_INFINITY, "negative-infinity"),
            (DataAbsentReason.POSITIVE_INFINITY, "positive-infinity"),
            (DataAbsentReason.NOT_PERFORMED, "not-performed"),
            (DataAbsentReason.NOT_PERMITTED, "not-permitted"),
        ],
    )
    def test_code_maps_every_concept(self, reason: DataAbsentReason, expected_code: str) -> None:
        assert reason.code == expected_code
        assert reason == expected_code

    def test_coding_carries_code_system(self) -> None:
        coding = DataAbsentReason.ERROR.coding()

        assert coding.system == fhir_extensions.DATA_ABSENT_REASON_CODE_SYSTEM
        assert coding.code == "error"

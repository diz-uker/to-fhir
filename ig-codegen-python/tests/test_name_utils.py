import pytest

from ig_codegen import name_utils


@pytest.mark.parametrize(
    ("fhir_id", "expected"),
    [
        ("mii-cs-onko-intention", "MII_CS_ONKO_INTENTION"),
        ("mii-pr-onko-operation", "MII_PR_ONKO_OPERATION"),
        ("PatientPseudonymisiert", "PATIENT_PSEUDONYMISIERT"),
        ("mii-ex-onko-zahnstatus", "MII_EX_ONKO_ZAHNSTATUS"),
        ("Vitalstatus", "VITALSTATUS"),
        ("a", "A"),
    ],
)
def test_to_constant_name(fhir_id: str, expected: str) -> None:
    assert name_utils.to_constant_name(fhir_id) == expected


@pytest.mark.parametrize(
    ("segment", "expected"),
    [
        ("onkologie", "Onkologie"),
        ("base", "Base"),
        ("kerndatensatz", "Kerndatensatz"),
        ("r4", "R4"),
    ],
)
def test_to_pascal_case(segment: str, expected: str) -> None:
    assert name_utils.to_pascal_case(segment) == expected


@pytest.mark.parametrize(
    ("constant_name", "expected"),
    [
        ("MII_PR_DIAGNOSE_CONDITION", "mii_pr_diagnose_condition"),
        ("MII_CS_ONKO_INTENTION", "mii_cs_onko_intention"),
        ("PATIENT_PSEUDONYMISIERT", "patient_pseudonymisiert"),
        ("A", "a"),
    ],
)
def test_to_snake_case(constant_name: str, expected: str) -> None:
    assert name_utils.to_snake_case(constant_name) == expected


@pytest.mark.parametrize(
    ("code", "expected"),
    [
        ("K", "K"),
        ("T1a1", "T1A1"),
        ("Tis(LAMN)", "TIS_LAMN_"),
        ("2", "_2"),
        ("10", "_10"),
        ("mol+", "MOL_POS"),
        ("i-", "I_NEG"),
        # enum.Enum reserves single-leading/trailing-underscore "_sunder_" names for its own use.
        ("100%", "_100__"),
    ],
)
def test_to_enum_constant_name(code: str, expected: str) -> None:
    assert name_utils.to_enum_constant_name(code) == expected

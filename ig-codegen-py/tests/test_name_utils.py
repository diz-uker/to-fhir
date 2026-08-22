"""Tests for name_utils — the FHIR-id-to-Python-name conversion utilities."""
from __future__ import annotations

from ig_codegen.name_utils import (
    to_constant_name,
    to_enum_member_name,
    to_pascal_case,
    to_snake_case,
)


class TestToConstantName:
    def test_kebab_case(self) -> None:
        assert to_constant_name("mii-cs-onko-intention") == "MII_CS_ONKO_INTENTION"

    def test_pascal_case(self) -> None:
        assert to_constant_name("MiiCsOnkoIntention") == "MII_CS_ONKO_INTENTION"

    def test_single_word(self) -> None:
        assert to_constant_name("kurativ") == "KURATIV"

    def test_already_upper_snake(self) -> None:
        assert to_constant_name("MII_CS_ONKO") == "MII_CS_ONKO"

    def test_digit_prefix(self) -> None:
        assert to_constant_name("0100").startswith("_")

    def test_empty(self) -> None:
        assert to_constant_name("") == "_"

    def test_mixed_separators(self) -> None:
        assert to_constant_name("research-subject-id") == "RESEARCH_SUBJECT_ID"


class TestToPascalCase:
    def test_kebab_case(self) -> None:
        assert to_pascal_case("mii-cs-onko-intention") == "MiiCsOnkoIntention"

    def test_already_pascal(self) -> None:
        assert to_pascal_case("MiiCsOnkoIntention") == "MiiCsOnkoIntention"

    def test_single_word_lower(self) -> None:
        assert to_pascal_case("onkologie") == "Onkologie"

    def test_single_word_upper(self) -> None:
        assert to_pascal_case("ONKOLOGIE") == "Onkologie"

    def test_last_package_segment(self) -> None:
        assert to_pascal_case("onkologie") == "Onkologie"
        assert to_pascal_case("kerndatensatz") == "Kerndatensatz"

    def test_empty(self) -> None:
        assert to_pascal_case("") == "_"


class TestToSnakeCase:
    def test_kebab_case(self) -> None:
        assert to_snake_case("mii-cs-onko-intention") == "mii_cs_onko_intention"

    def test_pascal_case(self) -> None:
        assert to_snake_case("MiiCsOnkoIntention") == "mii_cs_onko_intention"

    def test_single_word(self) -> None:
        assert to_snake_case("uri") == "uri"

    def test_already_snake(self) -> None:
        assert to_snake_case("mii_cs_onko") == "mii_cs_onko"

    def test_digit_prefix(self) -> None:
        name = to_snake_case("0100")
        assert not name[0].isdigit(), f"Expected no leading digit, got {name!r}"

    def test_empty(self) -> None:
        assert to_snake_case("") == "_"


class TestToEnumMemberName:
    def test_simple_code(self) -> None:
        assert to_enum_member_name("kurativ") == "KURATIV"

    def test_upper_code(self) -> None:
        assert to_enum_member_name("K") == "K"

    def test_kebab_code(self) -> None:
        assert to_enum_member_name("abc-def") == "ABC_DEF"

    def test_plus_suffix(self) -> None:
        assert to_enum_member_name("abc+") == "ABC_POS"

    def test_minus_suffix(self) -> None:
        assert to_enum_member_name("abc-") == "ABC_NEG"

    def test_numeric_code(self) -> None:
        name = to_enum_member_name("100")
        assert not name[0].isdigit(), f"Expected no leading digit, got {name!r}"
        assert name.endswith("100") or name == "_100"

    def test_empty(self) -> None:
        assert to_enum_member_name("") == "_"

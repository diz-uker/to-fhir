from pathlib import Path

from ig_codegen.concept_constant import ConceptConstant
from ig_codegen.ig_package_model import IgPackageModel
from ig_codegen.python_constants_generator import generate, write_to


def _model(code_systems=None, profiles=None, extensions=None, code_system_concepts=None):
    return IgPackageModel(
        package_name="de.example.onkologie",
        package_version="1.0.0",
        code_systems=code_systems or {},
        profiles=profiles or {},
        extensions=extensions or {},
        code_system_concepts=code_system_concepts or {},
    )


def test_generates_accessor_classes_only_for_non_empty_categories():
    model = _model(
        code_systems={
            "MII_CS_ONKO_INTENTION": "https://example.org/CodeSystem/mii-cs-onko-intention"
        }
    )

    source = generate(model, "Onkologie")

    assert "class CodeSystems" in source
    assert "class Profiles" not in source
    assert "class Extensions" not in source
    assert "def mii_cs_onko_intention() -> str" in source
    assert "https://example.org/CodeSystem/mii-cs-onko-intention" in source


def test_accessor_method_has_docstring_with_the_url_value():
    model = _model(
        code_systems={
            "MII_CS_ONKO_INTENTION": "https://example.org/CodeSystem/mii-cs-onko-intention"
        }
    )

    source = generate(model, "Onkologie")

    method_index = source.index("def mii_cs_onko_intention() -> str")
    docstring_index = source.index('"""The canonical URL', method_index)
    return_index = source.index("return", docstring_index)
    assert method_index < docstring_index < return_index
    assert "``https://example.org/CodeSystem/mii-cs-onko-intention``" in source


def test_profile_values_carry_version_suffix():
    model = _model(
        profiles={
            "MII_PR_ONKO_OPERATION": (
                "https://example.org/StructureDefinition/mii-pr-onko-operation|1.0.0"
            )
        }
    )

    source = generate(model, "Onkologie")

    assert "def mii_pr_onko_operation() -> str" in source
    assert "mii-pr-onko-operation|1.0.0" in source


def test_write_to_produces_file_at_expected_python_package_path(tmp_path: Path):
    model = _model(code_systems={"FOO": "https://example.org/CodeSystem/foo"})

    written = write_to(model, "Onkologie", tmp_path / "__init__.py")

    assert written == tmp_path / "__init__.py"
    assert written.exists()
    source = written.read_text()
    assert "def foo() -> str" in source


def test_generates_enum_with_coding_accessor_for_code_system_concepts():
    model = _model(
        code_systems={
            "MII_CS_ONKO_INTENTION": "https://example.org/CodeSystem/mii-cs-onko-intention"
        },
        code_system_concepts={
            "MII_CS_ONKO_INTENTION": [
                ConceptConstant("K", "K", "kurativ"),
                ConceptConstant("P", "P", "palliativ"),
            ]
        },
    )

    source = generate(model, "Onkologie")

    assert "from fhir.resources.R4B.coding import Coding" in source
    assert "class MiiCsOnkoIntention(Enum)" in source
    assert 'K = ("K", "kurativ")' in source
    assert 'P = ("P", "palliativ")' in source
    assert "def coding(self) -> Coding" in source
    assert "Coding(" in source
    assert 'system="https://example.org/CodeSystem/mii-cs-onko-intention"' in source
    assert "code=self.code" in source
    assert "display=self.display" in source


def test_generates_from_value_lookup_on_concept_enum():
    model = _model(
        code_systems={
            "MII_CS_ONKO_INTENTION": "https://example.org/CodeSystem/mii-cs-onko-intention"
        },
        code_system_concepts={
            "MII_CS_ONKO_INTENTION": [
                ConceptConstant("K", "K", "kurativ"),
                ConceptConstant("P", "P", "palliativ"),
            ]
        },
    )

    source = generate(model, "Onkologie")

    assert "def from_value(cls, code: str) -> Self" in source
    assert "for member in cls:" in source
    assert "if member.code == code:" in source
    assert 'raise ValueError(f"Unknown code: {code}")' in source


def test_does_not_generate_enum_when_code_system_has_no_concepts():
    model = _model(
        code_systems={
            "MII_CS_ONKO_INTENTION": "https://example.org/CodeSystem/mii-cs-onko-intention"
        }
    )

    source = generate(model, "Onkologie")

    assert "Enum" not in source

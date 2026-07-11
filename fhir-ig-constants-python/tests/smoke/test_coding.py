import pytest

from fhir_ig_constants.de.medizininformatikinitiative.kerndatensatz.onkologie import Onkologie


def test_coding_accessor_returns_expected_system_code_and_display():
    coding = Onkologie.CodeSystems.MiiCsOnkoIntention.K.coding()
    assert coding.system == (
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/CodeSystem/"
        "mii-cs-onko-intention"
    )
    assert coding.code == "K"
    assert coding.display == "kurativ"


def test_disambiguated_constants_round_trip_their_original_code():
    pos = Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_POS.coding()
    neg = Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_NEG.coding()
    assert pos.code == "i+"
    assert neg.code == "i-"


def test_from_value_looks_up_constant_by_code():
    assert (
        Onkologie.CodeSystems.MiiCsOnkoIntention.from_value("K")
        == Onkologie.CodeSystems.MiiCsOnkoIntention.K
    )
    assert (
        Onkologie.CodeSystems.MiiCsOnkoTnmUicc.from_value("i+")
        == Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_POS
    )
    assert (
        Onkologie.CodeSystems.MiiCsOnkoTnmUicc.from_value("i-")
        == Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_NEG
    )


def test_from_value_raises_for_unknown_code():
    with pytest.raises(ValueError, match="Unknown code"):
        Onkologie.CodeSystems.MiiCsOnkoIntention.from_value("not-a-real-code")

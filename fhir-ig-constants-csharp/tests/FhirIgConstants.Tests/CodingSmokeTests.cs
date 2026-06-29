using De.Medizininformatikinitiative.Kerndatensatz.Onkologie;

namespace FhirIgConstants.Tests;

public class CodingSmokeTests
{
    [Fact]
    public void CodingPropertyReturnsExpectedSystemCodeAndDisplay()
    {
        var coding = Onkologie.CodeSystems.MiiCsOnkoIntention.K;
        Assert.Equal(
            "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/CodeSystem/mii-cs-onko-intention",
            coding.System);
        Assert.Equal("K", coding.Code);
        Assert.Equal("kurativ", coding.Display);
    }

    [Fact]
    public void DisambiguatedConstantsRoundTripTheirOriginalCode()
    {
        var pos = Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_POS;
        var neg = Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_NEG;
        Assert.Equal("i+", pos.Code);
        Assert.Equal("i-", neg.Code);
    }

    [Fact]
    public void FromValueLooksUpConstantByCode()
    {
        Assert.Equal(
            Onkologie.CodeSystems.MiiCsOnkoIntention.K.Code,
            Onkologie.CodeSystems.MiiCsOnkoIntention.FromValue("K").Code);
        Assert.Equal(
            Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_POS.Code,
            Onkologie.CodeSystems.MiiCsOnkoTnmUicc.FromValue("i+").Code);
        Assert.Equal(
            Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_NEG.Code,
            Onkologie.CodeSystems.MiiCsOnkoTnmUicc.FromValue("i-").Code);
    }

    [Fact]
    public void FromValueThrowsForUnknownCode()
    {
        Assert.Throws<ArgumentException>(() => Onkologie.CodeSystems.MiiCsOnkoIntention.FromValue("not-a-real-code"));
    }
}

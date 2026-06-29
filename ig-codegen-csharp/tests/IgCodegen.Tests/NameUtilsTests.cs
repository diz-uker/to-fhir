using Dizuker.IgCodegen;

namespace IgCodegen.Tests;

public class NameUtilsTests
{
    [Theory]
    [InlineData("mii-cs-onko-intention", "MII_CS_ONKO_INTENTION")]
    [InlineData("mii-pr-onko-operation", "MII_PR_ONKO_OPERATION")]
    [InlineData("PatientPseudonymisiert", "PATIENT_PSEUDONYMISIERT")]
    [InlineData("mii-ex-onko-zahnstatus", "MII_EX_ONKO_ZAHNSTATUS")]
    [InlineData("Vitalstatus", "VITALSTATUS")]
    [InlineData("a", "A")]
    public void ToConstantName(string id, string expected) =>
        Assert.Equal(expected, NameUtils.ToConstantName(id));

    [Theory]
    [InlineData("onkologie", "Onkologie")]
    [InlineData("base", "Base")]
    [InlineData("kerndatensatz", "Kerndatensatz")]
    [InlineData("r4", "R4")]
    public void ToPascalCase(string segment, string expected) =>
        Assert.Equal(expected, NameUtils.ToPascalCase(segment));

    [Theory]
    [InlineData("MII_PR_DIAGNOSE_CONDITION", "MiiPrDiagnoseCondition")]
    [InlineData("MII_CS_ONKO_INTENTION", "MiiCsOnkoIntention")]
    [InlineData("PATIENT_PSEUDONYMISIERT", "PatientPseudonymisiert")]
    [InlineData("A", "A")]
    public void ToPropertyCase(string constantName, string expected) =>
        Assert.Equal(expected, NameUtils.ToPropertyCase(constantName));

    [Theory]
    [InlineData("K", "K")]
    [InlineData("T1a1", "T1A1")]
    [InlineData("Tis(LAMN)", "TIS_LAMN_")]
    [InlineData("2", "_2")]
    [InlineData("10", "_10")]
    [InlineData("mol+", "MOL_POS")]
    [InlineData("i-", "I_NEG")]
    public void ToIdentifierName(string code, string expected) =>
        Assert.Equal(expected, NameUtils.ToIdentifierName(code));
}

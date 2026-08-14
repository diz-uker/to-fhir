using IgCodegen;
using Xunit;

namespace IgCodegen.Tests;

public class NameUtilsTests
{
    [Theory]
    [InlineData("mii-cs-onko-intention", "MII_CS_ONKO_INTENTION")]
    [InlineData("mii-pr-diagnose-condition", "MII_PR_DIAGNOSE_CONDITION")]
    [InlineData("MiiCsOnkoIntention", "MII_CS_ONKO_INTENTION")]
    public void ToConstantName_ConvertsKebabAndPascalCase(string input, string expected) =>
        Assert.Equal(expected, NameUtils.ToConstantName(input));

    [Theory]
    [InlineData("MII_CS_ONKO_INTENTION", "MiiCsOnkoIntention")]
    [InlineData("ONKOLOGIE", "Onkologie")]
    [InlineData("onkologie", "Onkologie")]
    public void ToPascalCase_ConvertsConstantName(string input, string expected) =>
        Assert.Equal(expected, NameUtils.ToPascalCase(input));

    [Theory]
    [InlineData("K", "K")]
    [InlineData("i+", "I_POS")]
    [InlineData("i-", "I_NEG")]
    [InlineData("2", "_2")]
    [InlineData("mol+", "MOL_POS")]
    public void ToEnumMemberName_SanitizesFhirCode(string code, string expected) =>
        Assert.Equal(expected, NameUtils.ToEnumMemberName(code));
}

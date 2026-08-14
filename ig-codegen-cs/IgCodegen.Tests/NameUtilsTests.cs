using IgCodegen;
using Xunit;

namespace IgCodegen.Tests;

public class NameUtilsTests
{
    [Theory]
    [InlineData("K", "K")]
    [InlineData("kurativ", "Kurativ")]
    [InlineData("lost-to-follow-up", "LostToFollowUp")]
    [InlineData("miiCsOnkoIntention", "MiiCsOnkoIntention")]
    [InlineData("0100", "_0100")]
    [InlineData("active+", "ActivePos")]
    [InlineData("active-", "ActiveNeg")]
    public void ToEnumMemberName_ProducesPascalCase(string code, string expected)
    {
        Assert.Equal(expected, NameUtils.ToEnumMemberName(code));
    }
}

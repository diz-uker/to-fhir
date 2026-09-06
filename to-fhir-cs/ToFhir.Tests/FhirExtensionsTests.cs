using Hl7.Fhir.Model;
using Xunit;

namespace ToFhir.Tests;

public class FhirExtensionsTests
{
    [Fact]
    public void DataAbsentReasonNotAsked()
    {
        var extension = FhirExtensions.DataAbsentReason(DataAbsentReasonCode.NotAsked);

        Assert.Equal(FhirExtensions.DataAbsentReasonUrl, extension.Url);
        Assert.Equal("not-asked", Assert.IsType<Code>(extension.Value).Value);
    }

    [Fact]
    public void DataAbsentReasonReturnsFreshInstance()
    {
        var first = FhirExtensions.DataAbsentReason(DataAbsentReasonCode.NotAsked);
        var second = FhirExtensions.DataAbsentReason(DataAbsentReasonCode.NotAsked);

        Assert.NotSame(first, second);
    }

    [Fact]
    public void DataAbsentReasonTemplateHasNoValue()
    {
        var extension = FhirExtensions.DataAbsentReason();

        Assert.Equal(FhirExtensions.DataAbsentReasonUrl, extension.Url);
        Assert.Null(extension.Value);
    }

    [Fact]
    public void ToExtensionMatchesFactory()
    {
        var extension = DataAbsentReasonCode.Masked.ToExtension();

        Assert.Equal(FhirExtensions.DataAbsentReasonUrl, extension.Url);
        Assert.Equal("masked", Assert.IsType<Code>(extension.Value).Value);
    }

    [Theory]
    [InlineData(DataAbsentReasonCode.Unknown, "unknown")]
    [InlineData(DataAbsentReasonCode.AskedUnknown, "asked-unknown")]
    [InlineData(DataAbsentReasonCode.TempUnknown, "temp-unknown")]
    [InlineData(DataAbsentReasonCode.NotAsked, "not-asked")]
    [InlineData(DataAbsentReasonCode.AskedDeclined, "asked-declined")]
    [InlineData(DataAbsentReasonCode.Masked, "masked")]
    [InlineData(DataAbsentReasonCode.NotApplicable, "not-applicable")]
    [InlineData(DataAbsentReasonCode.Unsupported, "unsupported")]
    [InlineData(DataAbsentReasonCode.AsText, "as-text")]
    [InlineData(DataAbsentReasonCode.Error, "error")]
    [InlineData(DataAbsentReasonCode.NotANumber, "not-a-number")]
    [InlineData(DataAbsentReasonCode.NegativeInfinity, "negative-infinity")]
    [InlineData(DataAbsentReasonCode.PositiveInfinity, "positive-infinity")]
    [InlineData(DataAbsentReasonCode.NotPerformed, "not-performed")]
    [InlineData(DataAbsentReasonCode.NotPermitted, "not-permitted")]
    public void CodeMapsEveryConcept(DataAbsentReasonCode reason, string expectedCode)
    {
        Assert.Equal(expectedCode, reason.Code());
    }

    [Fact]
    public void CodingCarriesCodeSystem()
    {
        var coding = DataAbsentReasonCode.Error.Coding();

        Assert.Equal(DataAbsentReasonCodeExtensions.CodeSystemUrl, coding.System);
        Assert.Equal("error", coding.Code);
    }
}

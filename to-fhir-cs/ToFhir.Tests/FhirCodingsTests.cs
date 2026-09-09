using Hl7.Fhir.Model;
using Xunit;

namespace ToFhir.Tests;

public class FhirCodingsTests
{
    public static TheoryData<Func<Coding>, string> Templates =>
        new()
        {
            { FhirCodings.Loinc, FhirSystems.Loinc },
            { FhirCodings.Snomed, FhirSystems.Snomed },
            { FhirCodings.Ops, FhirSystems.Ops },
            { FhirCodings.Atc, FhirSystems.Atc },
            { FhirCodings.Icd10Gm, FhirSystems.Icd10Gm },
            { FhirCodings.Pzn, FhirSystems.Pzn },
        };

    [Theory]
    [MemberData(nameof(Templates))]
    public void TemplateHasSystemButNoCode(Func<Coding> template, string expectedSystem)
    {
        var coding = template();

        Assert.Equal(expectedSystem, coding.System);
        Assert.Null(coding.Code);
        Assert.Null(coding.Display);
    }

    [Theory]
    [MemberData(nameof(Templates))]
    public void TemplateReturnsFreshInstance(Func<Coding> template, string expectedSystem)
    {
        var first = template();
        var second = template();

        Assert.NotSame(first, second);
        Assert.Equal(expectedSystem, second.System);
    }

    [Fact]
    public void LoincTemplateCarriesVersion()
    {
        Assert.Equal("2.82", FhirCodings.Loinc().Version);
    }

    [Fact]
    public void PznTemplateHasNoVersion()
    {
        Assert.Null(FhirCodings.Pzn().Version);
    }
}

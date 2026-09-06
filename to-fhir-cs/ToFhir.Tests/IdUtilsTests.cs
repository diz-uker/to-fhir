using System.Security.Cryptography;
using Hl7.Fhir.Model;
using Xunit;

namespace ToFhir.Tests;

public class IdUtilsTests
{
    private static Identifier PatientIdentifier(
        string value = "12345",
        string system = "http://example.com/patient"
    ) => new(system, value);

    [Fact]
    public void ComputeIdFromIdentifier()
    {
        var id = IdUtils.FromIdentifier(PatientIdentifier());

        Assert.Equal("ab0ecec0f0b7f2e7d0034eb57fceee58120d2ecb95d4e05c2613143ae439f652", id);
    }

    [Fact]
    public void ComputeIdFromIdentifierIsConsistent()
    {
        Assert.Equal(
            IdUtils.FromIdentifier(PatientIdentifier()),
            IdUtils.FromIdentifier(PatientIdentifier())
        );
    }

    [Fact]
    public void ComputeIdFromIdentifierDifferentValues()
    {
        Assert.NotEqual(
            IdUtils.FromIdentifier(PatientIdentifier(value: "12345")),
            IdUtils.FromIdentifier(PatientIdentifier(value: "54321"))
        );
    }

    [Fact]
    public void ComputeIdFromIdentifierDifferentSystems()
    {
        Assert.NotEqual(
            IdUtils.FromIdentifier(PatientIdentifier(system: "http://example.com/patient")),
            IdUtils.FromIdentifier(PatientIdentifier(system: "http://other.com/patient"))
        );
    }

    [Theory]
    [InlineData("", "12345")]
    [InlineData(null, "12345")]
    [InlineData(" ", "12345")]
    [InlineData("http://example.com/patient", "")]
    [InlineData("http://example.com/patient", null)]
    [InlineData("http://example.com/patient", " ")]
    public void ComputeIdFromBlankIdentifierThrows(string? system, string? value)
    {
        var identifier = new Identifier { System = system, Value = value };

        Assert.Throws<ArgumentException>(() => IdUtils.FromIdentifier(identifier));
    }

    [Fact]
    public void ComputeIdFromIdentifierWithResourceType()
    {
        var id = IdUtils.FromIdentifier(PatientIdentifier(), ResourceType.Patient);

        Assert.Equal(
            "Patient/ab0ecec0f0b7f2e7d0034eb57fceee58120d2ecb95d4e05c2613143ae439f652",
            id
        );
    }

    [Fact]
    public void ComputeIdWithExplicitHashAlgorithm()
    {
        var id = IdUtils.FromIdentifier(PatientIdentifier(), HashAlgorithmName.SHA512);

        Assert.Equal(128, id.Length);
        Assert.NotEqual(IdUtils.FromIdentifier(PatientIdentifier()), id);
    }

    [Fact]
    public void ComputeIdWithResourceTypeAndExplicitHashAlgorithm()
    {
        var id = IdUtils.FromIdentifier(
            PatientIdentifier(),
            ResourceType.Observation,
            HashAlgorithmName.SHA512
        );

        Assert.StartsWith("Observation/", id, StringComparison.Ordinal);
        Assert.EndsWith(
            IdUtils.FromIdentifier(PatientIdentifier(), HashAlgorithmName.SHA512),
            id,
            StringComparison.Ordinal
        );
    }
}

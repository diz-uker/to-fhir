using Hl7.Fhir.Model;
using Xunit;

namespace ToFhir.Tests;

public class ReferenceUtilsTests
{
    [Fact]
    public void CreateReferenceToPatient()
    {
        var patient = new Patient { Id = "patient-123" };

        var reference = ReferenceUtils.CreateReferenceTo(patient);

        Assert.Equal("Patient/patient-123", reference.Reference);
    }

    [Fact]
    public void CreateReferenceToObservation()
    {
        var observation = new Observation { Id = "obs-456" };

        var reference = ReferenceUtils.CreateReferenceTo(observation);

        Assert.Equal("Observation/obs-456", reference.Reference);
    }

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData(" ")]
    public void CreateReferenceWithoutIdThrows(string? id)
    {
        var patient = new Patient { Id = id };

        Assert.Throws<ArgumentException>(() => ReferenceUtils.CreateReferenceTo(patient));
    }

    [Fact]
    public void CreateReferenceToIdentifier()
    {
        var identifier = new Identifier("http://example.com/patient", "12345");

        var reference = ReferenceUtils.CreateReferenceTo(identifier, ResourceType.Patient);

        Assert.Equal(
            "Patient/ab0ecec0f0b7f2e7d0034eb57fceee58120d2ecb95d4e05c2613143ae439f652",
            reference.Reference
        );
    }
}

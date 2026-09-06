using System.Runtime.CompilerServices;
using Hl7.Fhir.Model;
using Xunit;
// Hl7.Fhir.Model.Task (the FHIR resource) otherwise shadows the async return type.
using Task = System.Threading.Tasks.Task;

namespace ToFhir.Tests;

public class TransactionBuilderTests
{
    public static TheoryData<Bundle.BundleType> BundleTypes =>
        [.. Enum.GetValues<Bundle.BundleType>()];

    private static Patient TestPatient()
    {
        var patient = new Patient { Id = "test-patient" };
        patient.AddExtension("test", new Code("test"));
        return patient;
    }

    private static Observation TestObservation() =>
        new() { Id = "test-observation", Status = ObservationStatus.Final };

    /// <summary>Snapshots a bundle as pretty-printed FHIR JSON.</summary>
    private static SettingsTask VerifyFhir(
        Bundle bundle,
        string? name = null,
        [CallerFilePath] string sourceFile = ""
    )
    {
        var task = Verify(FhirJson.Serialize(bundle), "json", sourceFile: sourceFile);
        return name is null ? task : task.UseTextForParameters(name);
    }

    [Fact]
    public Task BuildWithSingleEntry()
    {
        var bundle = new TransactionBuilder().AddEntry(TestPatient()).Build();

        return VerifyFhir(bundle);
    }

    [Fact]
    public Task BuildWithMultipleEntries()
    {
        var bundle = new TransactionBuilder()
            .WithId("test-patient")
            .AddEntries(TestPatient(), TestObservation())
            .Build();

        return VerifyFhir(bundle);
    }

    [Fact]
    public Task BuildWithMultipleEntriesAndProvenance()
    {
        var bundle = new TransactionBuilder()
            .WithId("test-patient")
            .WithProvenance(
                new ResourceReference("Device/the-etl-job")
                {
                    Display = "The test etl job in version 1.2.3",
                },
                new ResourceReference { Display = "The source system" }
            )
            .AddEntries(TestPatient(), TestObservation())
            .AddDeleteEntries(
                new ResourceReference("Observation/test-observation-to-delete"),
                new ResourceReference("Observation/test-observation-to-delte-as-well")
            )
            .Build();

        return VerifyFhir(bundle);
    }

    [Fact]
    public void DefaultBundleType()
    {
        Assert.Equal(Bundle.BundleType.Transaction, new TransactionBuilder().Build().Type);
    }

    [Theory]
    [MemberData(nameof(BundleTypes))]
    public void WithDifferentBundleTypes(Bundle.BundleType bundleType)
    {
        Assert.Equal(bundleType, new TransactionBuilder().WithType(bundleType).Build().Type);
    }

    [Fact]
    public void AddSingleEntry()
    {
        var patient = TestPatient();

        var bundle = new TransactionBuilder().AddEntry(patient).Build();

        Assert.Same(patient, Assert.Single(bundle.Entry).Resource);
    }

    [Fact]
    public void AddMultipleEntriesIndividually()
    {
        var patient1 = new Patient { Id = "patient-1" };
        var patient2 = new Patient { Id = "patient-2" };

        var bundle = new TransactionBuilder().AddEntry(patient1).AddEntry(patient2).Build();

        Assert.Collection(
            bundle.Entry,
            entry => Assert.Same(patient1, entry.Resource),
            entry => Assert.Same(patient2, entry.Resource)
        );
    }

    [Fact]
    public void AddEntriesFromSequence()
    {
        var patients = new List<Resource>
        {
            new Patient { Id = "patient-1" },
            new Patient { Id = "patient-2" },
        };

        var bundle = new TransactionBuilder().AddEntries(patients).Build();

        Assert.Equal(2, bundle.Entry.Count);
    }

    [Fact]
    public void FullUrlIsRelativeReferenceByDefault()
    {
        var bundle = new TransactionBuilder().AddEntry(TestPatient()).Build();

        Assert.Equal("Patient/test-patient", Assert.Single(bundle.Entry).FullUrl);
    }

    [Fact]
    public void WithFullUrlBaseBuildsAbsoluteFullUrl()
    {
        var bundle = new TransactionBuilder()
            .WithFullUrlBase("https://example.org/fhir")
            .AddEntry(TestPatient())
            .Build();

        var entry = Assert.Single(bundle.Entry);
        Assert.Equal("https://example.org/fhir/Patient/test-patient", entry.FullUrl);
        Assert.Equal("Patient/test-patient", entry.Request!.Url);
    }

    [Fact]
    public void WithFullUrlBaseHandlesTrailingSlash()
    {
        var bundle = new TransactionBuilder()
            .WithFullUrlBase("https://example.org/fhir/")
            .AddEntry(TestPatient())
            .Build();

        Assert.Equal(
            "https://example.org/fhir/Patient/test-patient",
            Assert.Single(bundle.Entry).FullUrl
        );
    }

    [Fact]
    public void ChainedConfiguration()
    {
        var patient = TestPatient();

        var bundle = new TransactionBuilder()
            .WithType(Bundle.BundleType.Batch)
            .WithId(patient.Id!)
            .AddEntry(patient)
            .Build();

        Assert.Equal(Bundle.BundleType.Batch, bundle.Type);
        Assert.Equal(Bundle.HTTPVerb.PUT, Assert.Single(bundle.Entry).Request!.Method);
        Assert.Equal("test-patient", bundle.Id);
    }

    [Theory]
    [InlineData("bundle-1")]
    [InlineData("123")]
    [InlineData("my-bundle-id")]
    public void WithIdVariousValues(string id)
    {
        Assert.Equal(id, new TransactionBuilder().WithId(id).Build().Id);
    }

    [Fact]
    public void ThrowsOnDuplicateResourceIds()
    {
        var builder = new TransactionBuilder()
            .FailOnDuplicateEntries()
            .AddEntry(new Patient { Id = "patient-123" })
            .AddEntry(new Patient { Id = "patient-123" });

        var exception = Assert.Throws<InvalidOperationException>(builder.Build);
        Assert.Contains("Patient/patient-123", exception.Message, StringComparison.Ordinal);
    }

    [Fact]
    public void NoDuplicateExceptionWhenFlagNotEnabled()
    {
        var bundle = new TransactionBuilder()
            .AddEntry(new Patient { Id = "patient-123" })
            .AddEntry(new Patient { Id = "patient-123" })
            .Build();

        Assert.Equal(2, bundle.Entry.Count);
    }

    [Fact]
    public void DifferentResourceTypesWithSameIdAllowed()
    {
        var bundle = new TransactionBuilder()
            .FailOnDuplicateEntries()
            .AddEntry(new Patient { Id = "resource-123" })
            .AddEntry(new Observation { Id = "resource-123", Status = ObservationStatus.Final })
            .Build();

        Assert.Equal(2, bundle.Entry.Count);
    }

    [Fact]
    public void ThrowsOnDuplicateResourceIdsWithMultipleDuplicates()
    {
        var builder = new TransactionBuilder()
            .FailOnDuplicateEntries()
            .AddEntry(new Patient { Id = "patient-1" })
            .AddEntry(new Patient { Id = "patient-1" })
            .AddEntry(new Patient { Id = "patient-2" })
            .AddEntry(new Patient { Id = "patient-2" });

        Assert.Throws<InvalidOperationException>(builder.Build);
    }

    [Fact]
    public async Task BuildWithSeparateProvenanceAndDevice()
    {
        var result = new TransactionBuilder()
            .WithId("test-patient")
            .WithProvenance(
                new Device { Id = "the-etl-job" },
                new ResourceReference { Display = "The source system" }
            )
            .AddEntries(TestPatient(), TestObservation())
            .AddDeleteEntries(
                new ResourceReference("Observation/test-observation-to-delete"),
                new ResourceReference("Observation/test-observation-to-delete-as-well")
            )
            .BuildWithSeparateProvenance();

        await VerifyFhir(result.DataBundle, "data");
        await VerifyFhir(result.ProvenanceBundle, "provenance");
    }

    [Fact]
    public async Task BuildWithSeparateProvenanceWithoutDevice()
    {
        var (dataBundle, provenanceBundle) = new TransactionBuilder()
            .WithProvenance(
                new ResourceReference("Device/the-etl-job")
                {
                    Display = "The test etl job in version 1.2.3",
                },
                new ResourceReference { Display = "The source system" }
            )
            .AddEntry(new Patient { Id = "test-patient" })
            .BuildWithSeparateProvenance();

        await VerifyFhir(dataBundle, "data");
        await VerifyFhir(provenanceBundle, "provenance");
    }

    [Fact]
    public void BuildWithSeparateProvenanceThrowsWithoutProvenance()
    {
        Assert.Throws<InvalidOperationException>(
            new TransactionBuilder().BuildWithSeparateProvenance
        );
    }

    [Fact]
    public void BuildWithSeparateProvenanceBundleIsAlwaysTransaction()
    {
        var result = new TransactionBuilder()
            .WithType(Bundle.BundleType.Batch)
            .WithProvenance(
                new ResourceReference("Device/the-etl-job"),
                new ResourceReference { Display = "source" }
            )
            .AddEntry(new Patient { Id = "test-patient" })
            .BuildWithSeparateProvenance();

        Assert.Equal(Bundle.BundleType.Batch, result.DataBundle.Type);
        Assert.Equal(Bundle.BundleType.Transaction, result.ProvenanceBundle.Type);
    }

    [Fact]
    public void ProvenanceIdIsDeterministic()
    {
        static Bundle Build() =>
            new TransactionBuilder()
                .WithProvenance(
                    new ResourceReference("Device/the-etl-job"),
                    new ResourceReference { Display = "The source system" }
                )
                .AddEntry(new Patient { Id = "test-patient" })
                .Build();

        Assert.Equal(ProvenanceIdOf(Build()), ProvenanceIdOf(Build()));

        static string? ProvenanceIdOf(Bundle bundle) =>
            bundle.Entry.Select(entry => entry.Resource).OfType<Provenance>().Single().Id;
    }

    [Fact]
    public void ProvenanceUsesIdentifierWhenReferenceIsAbsent()
    {
        var bundle = new TransactionBuilder()
            .WithProvenance(
                new ResourceReference
                {
                    Identifier = new Identifier("http://example.com/etl", "the-etl-job"),
                },
                new ResourceReference { Display = "The source system" }
            )
            .AddEntry(new Patient { Id = "test-patient" })
            .Build();

        var provenance = bundle.Entry.Select(entry => entry.Resource).OfType<Provenance>().Single();

        Assert.NotNull(provenance.Id);
    }

    [Fact]
    public void ProvenanceWithoutAnyIdentifyingInformationThrows()
    {
        var builder = new TransactionBuilder()
            .WithProvenance(new ResourceReference(), new ResourceReference { Display = "source" })
            .AddEntry(new Patient { Id = "test-patient" });

        Assert.Throws<InvalidOperationException>(builder.Build);
    }

    [Fact]
    public void DeviceIsAddedToBundleAndProvenanceTargets()
    {
        var device = new Device { Id = "the-etl-job" };

        var bundle = new TransactionBuilder()
            .WithProvenance(device, new ResourceReference { Display = "The source system" })
            .AddEntry(new Patient { Id = "test-patient" })
            .Build();

        Assert.Contains(bundle.Entry, entry => ReferenceEquals(entry.Resource, device));

        var provenance = bundle.Entry.Select(entry => entry.Resource).OfType<Provenance>().Single();

        Assert.Contains(provenance.Target, target => target.Reference == "Device/the-etl-job");
    }

    [Fact]
    public void DeviceIsNotDuplicatedWhenAlreadyAddedAsEntry()
    {
        var device = new Device { Id = "the-etl-job" };

        var bundle = new TransactionBuilder()
            .WithProvenance(device, new ResourceReference { Display = "The source system" })
            .AddEntry(device)
            .Build();

        Assert.Single(bundle.Entry, entry => ReferenceEquals(entry.Resource, device));
    }
}

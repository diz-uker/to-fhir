using Dizuker.IgCodegen;

namespace IgCodegen.Tests;

public class CSharpConstantsGeneratorTests
{
    private static IgPackageModel Model(
        Dictionary<string, string>? codeSystems = null,
        Dictionary<string, string>? profiles = null,
        Dictionary<string, string>? extensions = null,
        Dictionary<string, List<ConceptConstant>>? codeSystemConcepts = null) =>
        new(
            "de.example.onkologie",
            "1.0.0",
            codeSystems ?? new(),
            profiles ?? new(),
            extensions ?? new(),
            codeSystemConcepts ?? new());

    [Fact]
    public void GeneratesAccessorClassesOnlyForNonEmptyCategories()
    {
        var model = Model(codeSystems: new()
        {
            ["MII_CS_ONKO_INTENTION"] = "https://example.org/CodeSystem/mii-cs-onko-intention",
        });

        string source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("class CodeSystems", source);
        Assert.DoesNotContain("class Profiles", source);
        Assert.DoesNotContain("class Extensions", source);
        Assert.Contains("MiiCsOnkoIntention", source);
        Assert.Contains("https://example.org/CodeSystem/mii-cs-onko-intention", source);
    }

    [Fact]
    public void ProfileValuesCarryVersionSuffix()
    {
        var model = Model(profiles: new()
        {
            ["MII_PR_ONKO_OPERATION"] = "https://example.org/StructureDefinition/mii-pr-onko-operation|1.0.0",
        });

        string source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("public static string MiiPrOnkoOperation", source);
        Assert.Contains("mii-pr-onko-operation|1.0.0", source);
    }

    [Fact]
    public void WriteToProducesFileAtExpectedPath()
    {
        string tempDir = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
        try
        {
            var model = Model(codeSystems: new() { ["FOO"] = "https://example.org/CodeSystem/foo" });

            string written = CSharpConstantsGenerator.WriteTo(
                model, "De.Example.Onkologie", "Onkologie", Path.Combine(tempDir, "Onkologie.cs"));

            Assert.Equal(Path.Combine(tempDir, "Onkologie.cs"), written);
            Assert.True(File.Exists(written));
            string source = File.ReadAllText(written);
            Assert.Contains("namespace De.Example.Onkologie;", source);
            Assert.Contains("public static string Foo", source);
        }
        finally
        {
            Directory.Delete(tempDir, recursive: true);
        }
    }

    [Fact]
    public void GeneratesConceptClassWithCodingPropertiesInsteadOfPlainUrlProperty()
    {
        var model = Model(
            codeSystems: new()
            {
                ["MII_CS_ONKO_INTENTION"] = "https://example.org/CodeSystem/mii-cs-onko-intention",
            },
            codeSystemConcepts: new()
            {
                ["MII_CS_ONKO_INTENTION"] = new()
                {
                    new ConceptConstant("K", "K", "kurativ"),
                    new ConceptConstant("P", "P", "palliativ"),
                },
            });

        string source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("using Hl7.Fhir.Model;", source);
        Assert.Contains("public static class MiiCsOnkoIntention", source);
        // No plain string accessor when a concepts class is generated - the name would collide.
        Assert.DoesNotContain("public static string MiiCsOnkoIntention", source);
        Assert.Contains("public const string Url = \"https://example.org/CodeSystem/mii-cs-onko-intention\";", source);
        Assert.Contains("public static Coding K => new(Url, \"K\", \"kurativ\");", source);
        Assert.Contains("public static Coding P => new(Url, \"P\", \"palliativ\");", source);
    }

    [Fact]
    public void GeneratesFromValueLookupOnConceptClass()
    {
        var model = Model(
            codeSystems: new()
            {
                ["MII_CS_ONKO_INTENTION"] = "https://example.org/CodeSystem/mii-cs-onko-intention",
            },
            codeSystemConcepts: new()
            {
                ["MII_CS_ONKO_INTENTION"] = new()
                {
                    new ConceptConstant("K", "K", "kurativ"),
                    new ConceptConstant("P", "P", "palliativ"),
                },
            });

        string source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("public static Coding FromValue(string code)", source);
        Assert.Contains("All.FirstOrDefault(c => c.Code == code)", source);
        Assert.Contains("throw new ArgumentException($\"Unknown code: {code}\")", source);
    }

    [Fact]
    public void DoesNotEmitFhirUsingWhenNoCodeSystemHasConcepts()
    {
        var model = Model(codeSystems: new()
        {
            ["MII_CS_ONKO_INTENTION"] = "https://example.org/CodeSystem/mii-cs-onko-intention",
        });

        string source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.DoesNotContain("Hl7.Fhir.Model", source);
        Assert.Contains("public static string MiiCsOnkoIntention", source);
    }
}

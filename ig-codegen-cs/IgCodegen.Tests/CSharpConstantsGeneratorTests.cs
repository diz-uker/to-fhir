using IgCodegen;
using Xunit;

namespace IgCodegen.Tests;

public class CSharpConstantsGeneratorTests
{
    private static IgPackageModel ModelWithCodeSystem(
        string constantName,
        string url,
        IReadOnlyList<ConceptConstant>? concepts = null
    )
    {
        var codeSystems = new SortedDictionary<string, string>(StringComparer.Ordinal)
        {
            [constantName] = url,
        };
        var codeSystemConcepts = concepts is not null
            ? new Dictionary<string, IReadOnlyList<ConceptConstant>> { [constantName] = concepts }
            : [];
        return new IgPackageModel(
            "test.package",
            "1.0.0",
            codeSystems,
            new SortedDictionary<string, string>(),
            new SortedDictionary<string, string>(),
            codeSystemConcepts,
            new Dictionary<string, ExtensionValueType>(),
            new Dictionary<string, NamingSystemEntry>()
        );
    }

    [Fact]
    public void GeneratesNamespaceAndOuterClass()
    {
        var model = ModelWithCodeSystem("MII_CS_ONKO_INTENTION", "https://example.org/cs");
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("namespace De.Example.Onkologie;", source);
        Assert.Contains("public static class Onkologie", source);
    }

    [Fact]
    public void GeneratesUrlAccessorInsideUrlsNestedClass()
    {
        var model = ModelWithCodeSystem("MII_CS_ONKO_INTENTION", "https://example.org/cs");
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("public static class Urls", source);
        Assert.Contains("public static string MiiCsOnkoIntention =>", source);
        Assert.Contains("\"https://example.org/cs\";", source);
    }

    [Fact]
    public void DoesNotGenerateProfilesOrExtensionsClassWhenEmpty()
    {
        var model = ModelWithCodeSystem("MII_CS_ONKO_INTENTION", "https://example.org/cs");
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.DoesNotContain("class Profiles", source);
        Assert.DoesNotContain("class Extensions", source);
    }

    [Fact]
    public void GeneratesConceptEnumWithXmlDocComments()
    {
        var concepts = new List<ConceptConstant>
        {
            new("K", "K", "kurativ"),
            new("P", "P", "palliativ"),
        };
        var model = ModelWithCodeSystem(
            "MII_CS_ONKO_INTENTION",
            "https://example.org/cs",
            concepts
        );
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("public enum MiiCsOnkoIntention", source);
        Assert.Contains("/// <summary><c>K</c> - kurativ</summary>", source);
        Assert.Contains("K,", source);
        Assert.Contains("/// <summary><c>P</c> - palliativ</summary>", source);
        Assert.Contains("P,", source);
    }

    [Fact]
    public void GeneratesExtensionClassWithUrlMethod()
    {
        var concepts = new List<ConceptConstant> { new("K", "K", "kurativ") };
        var model = ModelWithCodeSystem(
            "MII_CS_ONKO_INTENTION",
            "https://example.org/cs",
            concepts
        );
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("public static class MiiCsOnkoIntentionExtensions", source);
        Assert.Contains(
            "public static string Url(this Onkologie.CodeSystems.MiiCsOnkoIntention _) =>",
            source
        );
        Assert.Contains("\"https://example.org/cs\";", source);
    }

    [Fact]
    public void GeneratesCodeAndDisplaySwitchExpressions()
    {
        var concepts = new List<ConceptConstant> { new("K", "K", "kurativ"), new("P", "P", null) };
        var model = ModelWithCodeSystem(
            "MII_CS_ONKO_INTENTION",
            "https://example.org/cs",
            concepts
        );
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains(
            "public static string Code(this Onkologie.CodeSystems.MiiCsOnkoIntention value) => value switch",
            source
        );
        Assert.Contains("Onkologie.CodeSystems.MiiCsOnkoIntention.K => \"K\",", source);
        Assert.Contains(
            "public static string? Display(this Onkologie.CodeSystems.MiiCsOnkoIntention value) => value switch",
            source
        );
        Assert.Contains("Onkologie.CodeSystems.MiiCsOnkoIntention.K => \"kurativ\",", source);
        Assert.Contains("Onkologie.CodeSystems.MiiCsOnkoIntention.P => null,", source);
    }

    [Fact]
    public void GeneratesCodingMethod()
    {
        var concepts = new List<ConceptConstant> { new("K", "K", "kurativ") };
        var model = ModelWithCodeSystem(
            "MII_CS_ONKO_INTENTION",
            "https://example.org/cs",
            concepts
        );
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains(
            "public static Coding Coding(this Onkologie.CodeSystems.MiiCsOnkoIntention value) =>",
            source
        );
        Assert.Contains("new(value.Url(), value.Code(), value.Display());", source);
    }

    [Fact]
    public void GeneratesFromValueAndFromValueOrThrow()
    {
        var concepts = new List<ConceptConstant>
        {
            new("K", "K", "kurativ"),
            new("P", "P", "palliativ"),
        };
        var model = ModelWithCodeSystem(
            "MII_CS_ONKO_INTENTION",
            "https://example.org/cs",
            concepts
        );
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains(
            "public static Onkologie.CodeSystems.MiiCsOnkoIntention? FromValue(string code) => code switch",
            source
        );
        Assert.Contains("\"K\" => Onkologie.CodeSystems.MiiCsOnkoIntention.K,", source);
        Assert.Contains("_ => null", source);
        Assert.Contains(
            "public static Onkologie.CodeSystems.MiiCsOnkoIntention FromValueOrThrow(string code) =>",
            source
        );
        Assert.Contains(
            "FromValue(code) ?? throw new ArgumentException($\"Unknown code: {code}\", nameof(code));",
            source
        );
    }

    [Fact]
    public void GeneratesProfilesClass()
    {
        var profiles = new SortedDictionary<string, string>(StringComparer.Ordinal)
        {
            ["MII_PR_ONKO_OPERATION"] =
                "https://example.org/StructureDefinition/mii-pr-onko-operation|1.0.0",
        };
        var model = new IgPackageModel(
            "test.package",
            "1.0.0",
            new SortedDictionary<string, string>(),
            profiles,
            new SortedDictionary<string, string>(),
            new Dictionary<string, IReadOnlyList<ConceptConstant>>(),
            new Dictionary<string, ExtensionValueType>(),
            new Dictionary<string, NamingSystemEntry>()
        );

        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("public static class Profiles", source);
        Assert.Contains("public static string MiiPrOnkoOperation =>", source);
        Assert.Contains("mii-pr-onko-operation|1.0.0", source);
        Assert.DoesNotContain("class CodeSystems", source);
    }

    [Fact]
    public void GeneratesComplexExtensionWithNoValueParam()
    {
        var extensions = new SortedDictionary<string, string>(StringComparer.Ordinal)
        {
            ["MII_EX_ONKO_COMPLEX"] = "https://example.org/StructureDefinition/mii-ex-onko-complex",
        };
        var extensionValueTypes = new Dictionary<string, ExtensionValueType>
        {
            ["MII_EX_ONKO_COMPLEX"] = ExtensionValueType.None,
        };
        var model = new IgPackageModel(
            "test.package",
            "1.0.0",
            new SortedDictionary<string, string>(),
            new SortedDictionary<string, string>(),
            extensions,
            new Dictionary<string, IReadOnlyList<ConceptConstant>>(),
            extensionValueTypes,
            new Dictionary<string, NamingSystemEntry>()
        );

        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains("public static class Extensions", source);
        Assert.Contains("public static Extension MiiExOnkoComplex() =>", source);
        Assert.Contains(
            "new() { Url = \"https://example.org/StructureDefinition/mii-ex-onko-complex\" };",
            source
        );
    }

    [Fact]
    public void GeneratesFixedTypeExtensionWithValueParam()
    {
        var extensions = new SortedDictionary<string, string>(StringComparer.Ordinal)
        {
            ["MII_EX_ONKO_EINZELDOSIS"] =
                "https://example.org/StructureDefinition/mii-ex-onko-einzeldosis",
        };
        var extensionValueTypes = new Dictionary<string, ExtensionValueType>
        {
            ["MII_EX_ONKO_EINZELDOSIS"] = ExtensionValueType.Fixed("decimal"),
        };
        var model = new IgPackageModel(
            "test.package",
            "1.0.0",
            new SortedDictionary<string, string>(),
            new SortedDictionary<string, string>(),
            extensions,
            new Dictionary<string, IReadOnlyList<ConceptConstant>>(),
            extensionValueTypes,
            new Dictionary<string, NamingSystemEntry>()
        );

        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains(
            "public static Extension MiiExOnkoEinzeldosis(FhirDecimal value) =>",
            source
        );
        Assert.Contains(
            "new(\"https://example.org/StructureDefinition/mii-ex-onko-einzeldosis\", value);",
            source
        );
    }

    [Fact]
    public void GeneratesBoundCodingExtensionUsingEnumParam()
    {
        var csUrl = "https://example.org/CodeSystem/mii-cs-onko-intention";
        var extUrl = "https://example.org/StructureDefinition/mii-ex-onko-intention";

        var codeSystems = new SortedDictionary<string, string>(StringComparer.Ordinal)
        {
            ["MII_CS_ONKO_INTENTION"] = csUrl,
        };
        var extensions = new SortedDictionary<string, string>(StringComparer.Ordinal)
        {
            ["MII_EX_ONKO_INTENTION"] = extUrl,
        };
        var concepts = new List<ConceptConstant> { new("K", "K", "kurativ") };
        var codeSystemConcepts = new Dictionary<string, IReadOnlyList<ConceptConstant>>
        {
            ["MII_CS_ONKO_INTENTION"] = concepts,
        };
        var extensionValueTypes = new Dictionary<string, ExtensionValueType>
        {
            ["MII_EX_ONKO_INTENTION"] = ExtensionValueType.BoundCoding("CodeableConcept", csUrl),
        };

        var model = new IgPackageModel(
            "test.package",
            "1.0.0",
            codeSystems,
            new SortedDictionary<string, string>(),
            extensions,
            codeSystemConcepts,
            extensionValueTypes,
            new Dictionary<string, NamingSystemEntry>()
        );

        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.Contains(
            "public static Extension MiiExOnkoIntention(CodeSystems.MiiCsOnkoIntention value) =>",
            source
        );
        Assert.Contains(
            "new(\"https://example.org/StructureDefinition/mii-ex-onko-intention\", new CodeableConcept(value.Url(), value.Code(), value.Display()));",
            source
        );
    }

    [Fact]
    public void GeneratesNamingSystemsClassWithSingleUniqueId()
    {
        var namingSystems = new Dictionary<string, NamingSystemEntry>
        {
            ["ELIGIBILITY_OBSERVATION_ID"] = new NamingSystemEntry(
                "The identifier system for eligibility Observations",
                new SortedDictionary<string, IReadOnlyList<string>>(StringComparer.Ordinal)
                {
                    ["uri"] = ["https://example.org/identifiers/eid"],
                }
            ),
        };
        var model = new IgPackageModel(
            "test.package",
            "1.0.0",
            new SortedDictionary<string, string>(),
            new SortedDictionary<string, string>(),
            new SortedDictionary<string, string>(),
            new Dictionary<string, IReadOnlyList<ConceptConstant>>(),
            new Dictionary<string, ExtensionValueType>(),
            namingSystems
        );

        var source = CSharpConstantsGenerator.Generate(model, "De.Example", "Example");

        Assert.Contains("public static class NamingSystems", source);
        Assert.Contains("public static class EligibilityObservationId", source);
        Assert.Contains("public static class UniqueId", source);
        Assert.Contains("public static string Uri =>", source);
        Assert.Contains("\"https://example.org/identifiers/eid\";", source);
    }

    [Fact]
    public void GeneratesNamingSystemsListAccessorWhenMultipleUniqueIdsOfSameType()
    {
        var namingSystems = new Dictionary<string, NamingSystemEntry>
        {
            ["MY_NS"] = new NamingSystemEntry(
                null,
                new SortedDictionary<string, IReadOnlyList<string>>(StringComparer.Ordinal)
                {
                    ["oid"] = ["1.2.3", "4.5.6"],
                }
            ),
        };
        var model = new IgPackageModel(
            "test.package",
            "1.0.0",
            new SortedDictionary<string, string>(),
            new SortedDictionary<string, string>(),
            new SortedDictionary<string, string>(),
            new Dictionary<string, IReadOnlyList<ConceptConstant>>(),
            new Dictionary<string, ExtensionValueType>(),
            namingSystems
        );

        var source = CSharpConstantsGenerator.Generate(model, "De.Example", "Example");

        Assert.Contains("public static IReadOnlyList<string> Oid =>", source);
        Assert.Contains("\"1.2.3\", \"4.5.6\"", source);
    }

    [Fact]
    public void DoesNotGenerateNamingSystemsClassWhenEmpty()
    {
        var model = ModelWithCodeSystem("MII_CS_ONKO_INTENTION", "https://example.org/cs");
        var source = CSharpConstantsGenerator.Generate(model, "De.Example.Onkologie", "Onkologie");

        Assert.DoesNotContain("class NamingSystems", source);
    }
}

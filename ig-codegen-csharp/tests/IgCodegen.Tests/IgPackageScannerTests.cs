using Dizuker.IgCodegen;

namespace IgCodegen.Tests;

/// <summary>
/// Exercises the scanner against FHIR packages actually restored to the local Firely Terminal
/// cache (<c>~/.fhir/packages</c>), as listed in <c>tests/resources/package.json</c>. Skips if the
/// packages aren't present (e.g. on a machine that hasn't run <c>fhir restore</c>).
/// </summary>
public class IgPackageScannerTests
{
    private const string PackageName = "de.medizininformatikinitiative.kerndatensatz.onkologie";
    private const string PackageVersion = "2026.0.3";

    private readonly IgPackageScanner _scanner = new();
    private readonly string _fhirPackagesDir =
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".fhir", "packages");

    private IgPackageModel? Scan()
    {
        string packageContentDir = _scanner.ResolvePackageContentDir(_fhirPackagesDir, PackageName, PackageVersion);
        if (!Directory.Exists(packageContentDir))
        {
            return null;
        }
        return _scanner.Scan(packageContentDir, PackageName, PackageVersion);
    }

    [Fact]
    public void ClassifiesCodeSystemsByPlainUrl()
    {
        IgPackageModel? model = Scan();
        if (model is null)
        {
            return;
        }

        Assert.True(model.CodeSystems.ContainsKey("MII_CS_ONKO_INTENTION"));
        Assert.Equal(
            "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/CodeSystem/mii-cs-onko-intention",
            model.CodeSystems["MII_CS_ONKO_INTENTION"]);
    }

    [Fact]
    public void ClassifiesProfilesByVersionedUrl()
    {
        IgPackageModel? model = Scan();
        if (model is null)
        {
            return;
        }

        string? value = model.Profiles.GetValueOrDefault("MII_PR_ONKO_ALLGEMEINER_LEISTUNGSZUSTAND_ECOG");
        Assert.NotNull(value);
        Assert.EndsWith($"|{PackageVersion}", value);
        Assert.Contains("StructureDefinition/mii-pr-onko-allgemeiner-leistungszustand-ecog", value);
    }

    [Fact]
    public void ClassifiesExtensionsByPlainUrl()
    {
        IgPackageModel? model = Scan();
        if (model is null)
        {
            return;
        }

        string? value = model.Extensions.GetValueOrDefault("MII_EX_ONKO_STRAHLENTHERAPIE_BESTRAHLUNG_EINZELDOSIS");
        Assert.Equal(
            "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/StructureDefinition/"
                + "mii-ex-onko-strahlentherapie-bestrahlung-einzeldosis",
            value);
    }

    [Fact]
    public void ExcludesLogicalModelsAndBaseTypeSpecializations()
    {
        IgPackageModel? model = Scan();
        if (model is null)
        {
            return;
        }

        Assert.NotEmpty(model.Profiles);
        Assert.All(model.Profiles.Values, value => Assert.Contains("|", value));
    }

    [Fact]
    public void ExpandsCompleteCodeSystemConceptsIntoConceptConstants()
    {
        IgPackageModel? model = Scan();
        if (model is null)
        {
            return;
        }

        var intention = model.CodeSystemConcepts.GetValueOrDefault("MII_CS_ONKO_INTENTION");
        Assert.NotNull(intention);
        Assert.Equal(7, intention.Count);
        Assert.Contains(intention, c => c.PropertyName == "K" && c.Code == "K" && c.Display == "kurativ");
    }

    [Fact]
    public void DoesNotExpandNonCompleteCodeSystems()
    {
        IgPackageModel? model = Scan();
        if (model is null)
        {
            return;
        }

        // mii-cs-onko-krk-operationstyp has content == "fragment" in this package, not "complete".
        Assert.False(model.CodeSystemConcepts.ContainsKey("MII_CS_ONKO_KRK_OPERATIONSTYP"));
    }
}

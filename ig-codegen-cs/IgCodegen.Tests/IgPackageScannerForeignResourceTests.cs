using IgCodegen;
using Xunit;

namespace IgCodegen.Tests;

/// <summary>
/// Covers resources sharing a package directory with the ones <see cref="IgPackageScanner"/>
/// classifies. FHIR reuses field names across resource types with incompatible shapes, so a
/// resource the scanner generates nothing from must not be able to fail the scan of the whole
/// package.
/// </summary>
public class IgPackageScannerForeignResourceTests : IDisposable
{
    private const string PackageName = "de.example.onkologie";
    private const string PackageVersion = "1.0.0";

    private readonly IgPackageScanner scanner = new();
    private readonly string fhirPackagesDir = Directory
        .CreateTempSubdirectory("ig-codegen-")
        .FullName;

    public void Dispose()
    {
        Directory.Delete(fhirPackagesDir, recursive: true);
        GC.SuppressFinalize(this);
    }

    [Fact]
    public void IgnoresResourceTypesItGeneratesNothingFrom()
    {
        var packageContentDir = CreatePackageContentDir();
        WriteCodeSystem(packageContentDir);
        // Library.type is a CodeableConcept, where StructureDefinition.type is a plain code.
        Write(
            packageContentDir,
            "Library-mii-lib-onko-synthesize-tnm.json",
            """
            {
              "resourceType": "Library",
              "id": "mii-lib-onko-synthesize-tnm",
              "url": "https://example.org/Library/mii-lib-onko-synthesize-tnm",
              "type": {
                "coding": [
                  {
                    "system": "http://terminology.hl7.org/CodeSystem/library-type",
                    "code": "logic-library"
                  }
                ]
              }
            }
            """
        );
        // OperationDefinition.type is a boolean.
        Write(
            packageContentDir,
            "OperationDefinition-example.json",
            """
            {
              "resourceType": "OperationDefinition",
              "id": "example",
              "url": "https://example.org/OperationDefinition/example",
              "kind": "operation",
              "type": false
            }
            """
        );
        // The package's own npm-shaped manifest, which carries no resourceType at all.
        Write(
            packageContentDir,
            "package.json",
            """
            {"name": "de.example.onkologie", "version": "1.0.0", "type": "commonjs"}
            """
        );

        var model = Scan();

        Assert.Equal(
            "https://example.org/CodeSystem/mii-cs-onko-intention",
            model.CodeSystems["MII_CS_ONKO_INTENTION"]
        );
        Assert.Empty(model.Profiles);
        Assert.Empty(model.Extensions);
    }

    [Fact]
    public void ReadsANamingSystemWhoseTypeIsACodeableConcept()
    {
        var packageContentDir = CreatePackageContentDir();
        Write(
            packageContentDir,
            "NamingSystem-mii-ns-onko-beispiel.json",
            """
            {
              "resourceType": "NamingSystem",
              "id": "mii-ns-onko-beispiel",
              "kind": "identifier",
              "description": "Beispiel-Namenssystem",
              "type": {
                "coding": [
                  {"system": "http://terminology.hl7.org/CodeSystem/v2-0203", "code": "NI"}
                ]
              },
              "uniqueId": [{"type": "uri", "value": "https://example.org/ns/beispiel"}]
            }
            """
        );

        var model = Scan();

        var namingSystem = model.NamingSystems["MII_NS_ONKO_BEISPIEL"];
        Assert.Equal("Beispiel-Namenssystem", namingSystem.Description);
        Assert.Equal(["https://example.org/ns/beispiel"], namingSystem.ByType["uri"]);
    }

    private string CreatePackageContentDir() =>
        Directory.CreateDirectory(Path.Combine(fhirPackagesDir, PackageName)).FullName;

    private static void WriteCodeSystem(string packageContentDir) =>
        Write(
            packageContentDir,
            "CodeSystem-mii-cs-onko-intention.json",
            """
            {
              "resourceType": "CodeSystem",
              "id": "mii-cs-onko-intention",
              "url": "https://example.org/CodeSystem/mii-cs-onko-intention",
              "version": "1.0.0",
              "content": "complete",
              "concept": [{"code": "K", "display": "kurativ"}]
            }
            """
        );

    private static void Write(string packageContentDir, string fileName, string json) =>
        File.WriteAllText(Path.Combine(packageContentDir, fileName), json);

    private IgPackageModel Scan()
    {
        var packageContentDir = scanner.ResolvePackageContentDir(
            fhirPackagesDir,
            PackageName,
            PackageVersion
        );
        return scanner.Scan(packageContentDir, PackageName, PackageVersion);
    }
}

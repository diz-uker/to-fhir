using System.Text.Json;
using Dizuker.IgCodegen;

namespace IgCodegen.Tests;

/// <summary>
/// Exercises <see cref="IgPackageScanner.ResolvePackageContentDir"/> with synthetic directory
/// trees, to cover the Firely Terminal vs. npm layout fallback without depending on real restored
/// packages.
/// </summary>
public class IgPackageScannerLayoutFallbackTests : IDisposable
{
    private const string PackageName = "de.example.onkologie";
    private const string PackageVersion = "1.0.0";

    private readonly string _tempDir = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
    private readonly IgPackageScanner _scanner = new();

    public void Dispose() => Directory.Delete(_tempDir, recursive: true);

    [Fact]
    public void PrefersFirelyTerminalLayoutWhenBothExist()
    {
        string firelyLayout = Path.Combine(_tempDir, $"{PackageName}#{PackageVersion}", "package");
        Directory.CreateDirectory(firelyLayout);
        Directory.CreateDirectory(Path.Combine(_tempDir, PackageName)); // npm-style, also present

        Assert.Equal(firelyLayout, _scanner.ResolvePackageContentDir(_tempDir, PackageName, PackageVersion));
    }

    [Fact]
    public void FallsBackToFlatNpmLayoutWhenFirelyLayoutIsMissing()
    {
        string npmLayout = Path.Combine(_tempDir, PackageName);
        Directory.CreateDirectory(npmLayout);

        Assert.Equal(npmLayout, _scanner.ResolvePackageContentDir(_tempDir, PackageName, PackageVersion));
    }

    [Fact]
    public void ScansResourcesFromNpmLayout()
    {
        string npmLayout = Path.Combine(_tempDir, PackageName);
        Directory.CreateDirectory(npmLayout);
        File.WriteAllText(
            Path.Combine(npmLayout, "CodeSystem-mii-cs-onko-intention.json"),
            JsonSerializer.Serialize(new
            {
                resourceType = "CodeSystem",
                id = "mii-cs-onko-intention",
                url = "https://example.org/CodeSystem/mii-cs-onko-intention",
                version = "1.0.0",
                content = "complete",
                concept = new[] { new { code = "K", display = "kurativ" } },
            }));

        string packageContentDir = _scanner.ResolvePackageContentDir(_tempDir, PackageName, PackageVersion);
        IgPackageModel model = _scanner.Scan(packageContentDir, PackageName, PackageVersion);

        Assert.Equal(
            "https://example.org/CodeSystem/mii-cs-onko-intention",
            model.CodeSystems["MII_CS_ONKO_INTENTION"]);
    }
}

using Dizuker.IgCodegen;

namespace IgCodegen.Tests;

/// <summary>
/// End-to-end: drives <c>Resources/package.json</c> (this module's own manifest, listing the
/// packages restored for development/testing) against the local Firely Terminal cache.
/// </summary>
public class IgCodegenToolTests
{
    [Fact]
    public void GeneratesOneClassPerNonSkippedDependency()
    {
        string packageJsonFile = Path.Combine("Resources", "package.json");
        string fhirPackagesDir =
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".fhir", "packages");
        string onkologieContentDir = new IgPackageScanner().ResolvePackageContentDir(
            fhirPackagesDir, "de.medizininformatikinitiative.kerndatensatz.onkologie", "2026.0.3");
        if (!Directory.Exists(onkologieContentDir))
        {
            return;
        }

        string tempDir = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
        try
        {
            List<string> generatedFiles = new IgCodegenTool().Generate(packageJsonFile, fhirPackagesDir, tempDir);

            Assert.NotEmpty(generatedFiles);
            string onkologie = Path.Combine(tempDir, "Onkologie.cs");
            Assert.Contains(onkologie, generatedFiles);
            Assert.True(File.Exists(onkologie));
            // hl7.fhir.r4.core has no IG-specific canonical prefix and must be skipped.
            Assert.DoesNotContain(generatedFiles, f => f.Contains("Hl7"));
        }
        finally
        {
            Directory.Delete(tempDir, recursive: true);
        }
    }

    [Fact]
    public void DefaultFhirPackagesDirPrefersHomeDirWhenItExists()
    {
        string fakeHome = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
        try
        {
            string homeFhirPackages = Path.Combine(fakeHome, ".fhir", "packages");
            Directory.CreateDirectory(homeFhirPackages);

            Assert.Equal(homeFhirPackages, IgCodegenTool.DefaultFhirPackagesDir(fakeHome, ""));
        }
        finally
        {
            Directory.Delete(fakeHome, recursive: true);
        }
    }

    [Fact]
    public void DefaultFhirPackagesDirFallsBackToCwdWhenHomeDirIsMissing()
    {
        string fakeHome = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
        string fakeCwd = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
        try
        {
            string cwdFhirPackages = Path.Combine(fakeCwd, ".fhir", "packages");
            Directory.CreateDirectory(cwdFhirPackages);

            Assert.Equal(cwdFhirPackages, IgCodegenTool.DefaultFhirPackagesDir(fakeHome, fakeCwd));
        }
        finally
        {
            Directory.Delete(fakeHome, recursive: true);
            Directory.Delete(fakeCwd, recursive: true);
        }
    }

    [Fact]
    public void DefaultFhirPackagesDirFallsBackToNodeModulesWhenNeitherFhirDirExists()
    {
        string fakeHome = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
        string fakeCwd = Directory.CreateTempSubdirectory("ig-codegen-tests-").FullName;
        try
        {
            Assert.Equal(
                Path.Combine(fakeCwd, "node_modules"),
                IgCodegenTool.DefaultFhirPackagesDir(fakeHome, fakeCwd));
        }
        finally
        {
            Directory.Delete(fakeHome, recursive: true);
            Directory.Delete(fakeCwd, recursive: true);
        }
    }
}

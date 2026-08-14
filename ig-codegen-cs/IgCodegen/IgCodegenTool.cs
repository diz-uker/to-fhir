namespace IgCodegen;

/// <summary>
/// Generates one C# constant class per FHIR IG package declared in a package manifest's
/// <c>dependencies</c>.
/// </summary>
public sealed class IgCodegenTool
{
    private static readonly List<string> SkippedPackages = ["hl7.fhir.r4.core"];

    private readonly IgPackageScanner _scanner = new();

    public List<string> Generate(string packageJsonFile, string fhirPackagesDir, string outputDir)
    {
        var manifest = PackageManifest.Read(packageJsonFile);
        var generated = new List<string>();

        foreach (var (packageName, packageVersion) in manifest.Dependencies)
        {
            if (SkippedPackages.Contains(packageName))
                continue;

            string contentDir = _scanner.ResolvePackageContentDir(
                fhirPackagesDir,
                packageName,
                packageVersion
            );
            var model = _scanner.Scan(contentDir, packageName, packageVersion);

            string csharpNamespace = ToCSharpNamespace(packageName);
            string className = NameUtils.ToPascalCase(LastSegment(packageName));
            generated.Add(
                CSharpConstantsGenerator.WriteTo(model, csharpNamespace, className, outputDir)
            );
        }

        return generated;
    }

    private static string ToCSharpNamespace(string packageName) =>
        string.Join(".", packageName.Split('.').Select(NameUtils.ToPascalCase));

    private static string LastSegment(string name)
    {
        int dot = name.LastIndexOf('.');
        return dot < 0 ? name : name[(dot + 1)..];
    }

    public static string DefaultFhirPackagesDir()
    {
        string home =
            Environment.GetEnvironmentVariable("HOME")
            ?? Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        return DefaultFhirPackagesDir(home, Directory.GetCurrentDirectory());
    }

    internal static string DefaultFhirPackagesDir(string homeDir, string cwd)
    {
        var home = Path.Combine(homeDir, ".fhir", "packages");
        if (Directory.Exists(home))
            return home;
        var local = Path.Combine(cwd, ".fhir", "packages");
        if (Directory.Exists(local))
            return local;
        return Path.Combine(cwd, "node_modules");
    }
}

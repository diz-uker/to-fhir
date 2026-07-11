namespace Dizuker.IgCodegen;

/// <summary>
/// Generates one C# constant class per FHIR IG package declared in a package manifest's
/// <c>dependencies</c>.
///
/// <para>Each generated class resides in a C# namespace with the same name as the FHIR package
/// (e.g. the FHIR package <c>de.medizininformatikinitiative.kerndatensatz.onkologie</c> produces
/// the namespace <c>De.Medizininformatikinitiative.Kerndatensatz.Onkologie</c>), so canonical URLs
/// don't have to be hand-transcribed into application config.</para>
/// </summary>
public sealed class IgCodegenTool
{
    // FHIR packages with no IG-specific canonical prefix of their own; nothing to generate.
    private static readonly HashSet<string> SkippedPackages = new() { "hl7.fhir.r4.core" };

    private readonly IgPackageScanner _scanner = new();

    /// <summary>
    /// Generates one C# constant class per non-skipped dependency in <paramref name="packageJsonFile"/>.
    /// </summary>
    /// <param name="packageJsonFile">the FHIR package manifest listing the IG packages to generate from</param>
    /// <param name="fhirPackagesDir">where restored FHIR packages live, e.g. the Firely Terminal cache
    /// (<c>~/.fhir/packages</c>) or an npm <c>node_modules</c></param>
    /// <param name="outputDir">the C# source root to write generated classes into</param>
    public List<string> Generate(string packageJsonFile, string fhirPackagesDir, string outputDir)
    {
        PackageManifest manifest = PackageManifest.Read(packageJsonFile);

        var generatedFiles = new List<string>();
        foreach ((string packageName, string packageVersion) in manifest.Dependencies)
        {
            if (SkippedPackages.Contains(packageName))
            {
                continue;
            }

            string packageContentDir = _scanner.ResolvePackageContentDir(fhirPackagesDir, packageName, packageVersion);
            IgPackageModel model = _scanner.Scan(packageContentDir, packageName, packageVersion);

            string className = NameUtils.ToPascalCase(LastSegment(packageName));
            string @namespace = string.Join('.', packageName.Split('.').Select(NameUtils.ToPascalCase));
            string outputFile = Path.Combine(outputDir, className + ".cs");
            generatedFiles.Add(CSharpConstantsGenerator.WriteTo(model, @namespace, className, outputFile));
        }
        return generatedFiles;
    }

    private static string LastSegment(string dotSeparatedName)
    {
        int lastDot = dotSeparatedName.LastIndexOf('.');
        return lastDot < 0 ? dotSeparatedName : dotSeparatedName[(lastDot + 1)..];
    }

    /// <summary>
    /// Tries, in order: the Firely Terminal global package cache (<c>~/.fhir/packages</c>); a
    /// project-local <c>./.fhir/packages</c>, e.g. when a local Firely Terminal config restores
    /// packages relative to the current directory instead; and finally <c>./node_modules</c>, for
    /// FHIR packages installed via <c>npm install</c> rather than <c>fhir restore</c>.
    ///
    /// <para>Resolves the home directory from the <c>HOME</c> environment variable rather than
    /// <see cref="Environment.GetFolderPath"/>, which in some containers (e.g. GitHub Actions
    /// container jobs, where <c>HOME</c> is overridden to <c>/github/home</c>) doesn't match the
    /// directory tools like Firely Terminal actually restore into.</para>
    /// </summary>
    public static string DefaultFhirPackagesDir(string? homeDir = null, string? cwd = null)
    {
        homeDir ??= Environment.GetEnvironmentVariable("HOME");
        if (string.IsNullOrEmpty(homeDir))
        {
            homeDir = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        }
        cwd ??= "";

        string homeFhirPackagesDir = Path.Combine(homeDir, ".fhir", "packages");
        if (Directory.Exists(homeFhirPackagesDir))
        {
            return homeFhirPackagesDir;
        }
        string cwdFhirPackagesDir = Path.Combine(cwd, ".fhir", "packages");
        if (Directory.Exists(cwdFhirPackagesDir))
        {
            return cwdFhirPackagesDir;
        }
        return Path.Combine(cwd, "node_modules");
    }
}

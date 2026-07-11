using System.Text.Json;

namespace Dizuker.IgCodegen;

/// <summary>
/// Scans a restored FHIR package directory (<c>~/.fhir/packages/&lt;name&gt;#&lt;version&gt;/package/</c>,
/// or <c>node_modules/&lt;name&gt;/</c> if installed via npm) and classifies its resources into
/// CodeSystem/Profile/Extension canonical URL constants.
/// </summary>
public sealed class IgPackageScanner
{
    /// <summary>
    /// Resolves the directory holding a package's resource JSON files. Tries the Firely Terminal
    /// cache layout (<c>&lt;fhirPackagesDir&gt;/&lt;packageName&gt;#&lt;packageVersion&gt;/package</c>)
    /// first, then falls back to the flat npm layout (<c>&lt;fhirPackagesDir&gt;/&lt;packageName&gt;</c>,
    /// no version segment, no nested <c>package</c> directory) used when FHIR packages are
    /// installed via <c>npm install</c> instead of Firely Terminal's <c>fhir restore</c>.
    /// </summary>
    public string ResolvePackageContentDir(string fhirPackagesDir, string packageName, string packageVersion)
    {
        string firelyLayout = Path.Combine(fhirPackagesDir, $"{packageName}#{packageVersion}", "package");
        if (Directory.Exists(firelyLayout))
        {
            return firelyLayout;
        }
        return Path.Combine(fhirPackagesDir, packageName);
    }

    public IgPackageModel Scan(string packageContentDir, string packageName, string packageVersion)
    {
        var codeSystems = new Dictionary<string, string>();
        var profiles = new Dictionary<string, string>();
        var extensions = new Dictionary<string, string>();
        var codeSystemConcepts = new Dictionary<string, List<ConceptConstant>>();

        foreach (string file in Directory.EnumerateFiles(packageContentDir, "*.json").OrderBy(f => f))
        {
            Classify(file, codeSystems, profiles, extensions, codeSystemConcepts);
        }

        return new IgPackageModel(
            packageName,
            packageVersion,
            new SortedDictionary<string, string>(codeSystems),
            new SortedDictionary<string, string>(profiles),
            new SortedDictionary<string, string>(extensions),
            codeSystemConcepts);
    }

    private static void Classify(
        string file,
        Dictionary<string, string> codeSystems,
        Dictionary<string, string> profiles,
        Dictionary<string, string> extensions,
        Dictionary<string, List<ConceptConstant>> codeSystemConcepts)
    {
        var resource = JsonSerializer.Deserialize<FhirResourceSummary>(File.ReadAllText(file));
        if (resource is null)
        {
            return;
        }

        string? resourceType = resource.ResourceType;
        string? id = resource.Id;
        string? url = resource.Url;
        if (resourceType is null || id is null || url is null)
        {
            return;
        }

        string constantName = NameUtils.ToConstantName(id);

        if (resourceType == "CodeSystem")
        {
            ClassifyCodeSystem(resource, constantName, url, codeSystems, codeSystemConcepts);
            return;
        }

        if (resourceType != "StructureDefinition")
        {
            return;
        }

        string? kind = resource.Kind;
        string? derivation = resource.Derivation;
        if (kind == "logical" || derivation == "specialization")
        {
            return;
        }

        if (kind == "complex-type" && derivation == "constraint" && resource.Type == "Extension")
        {
            extensions[constantName] = url;
            return;
        }

        if (kind == "resource" && derivation == "constraint")
        {
            string? version = resource.Version;
            profiles[constantName] = version is null ? url : $"{url}|{version}";
        }
    }

    private static void ClassifyCodeSystem(
        FhirResourceSummary resource,
        string constantName,
        string url,
        Dictionary<string, string> codeSystems,
        Dictionary<string, List<ConceptConstant>> codeSystemConcepts)
    {
        codeSystems[constantName] = url;
        if (resource.Content != "complete" || resource.ConceptList is null)
        {
            return;
        }
        List<ConceptConstant> concepts = FlattenConcepts(resource.ConceptList);
        if (concepts.Count > 0)
        {
            codeSystemConcepts[constantName] = concepts;
        }
    }

    /// <summary>
    /// Flattens a CodeSystem's concept hierarchy (including both group/parent and leaf concepts)
    /// into a flat, name-collision-free list, in document order.
    /// </summary>
    private static List<ConceptConstant> FlattenConcepts(List<FhirResourceSummary.Concept> concepts)
    {
        var result = new List<ConceptConstant>();
        var usedNames = new HashSet<string>();
        FlattenConcepts(concepts, result, usedNames);
        return result;
    }

    private static void FlattenConcepts(
        List<FhirResourceSummary.Concept> concepts, List<ConceptConstant> result, HashSet<string> usedNames)
    {
        foreach (var concept in concepts)
        {
            string? code = concept.Code;
            if (code is not null)
            {
                result.Add(new ConceptConstant(UniquePropertyName(code, usedNames), code, concept.Display));
            }
            if (concept.Children is not null)
            {
                FlattenConcepts(concept.Children, result, usedNames);
            }
        }
    }

    private static string UniquePropertyName(string code, HashSet<string> usedNames)
    {
        string baseName = NameUtils.ToIdentifierName(code);
        string name = baseName;
        int suffix = 2;
        while (!usedNames.Add(name))
        {
            name = $"{baseName}_{suffix}";
            suffix++;
        }
        return name;
    }
}

using System.Text.Json;
using System.Text.Json.Serialization;

namespace IgCodegen;

/// <summary>
/// Scans a restored FHIR package directory and classifies its resources into
/// CodeSystem/Profile/Extension canonical URL constants.
/// </summary>
public sealed class IgPackageScanner
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        AllowTrailingCommas = true,
        ReadCommentHandling = JsonCommentHandling.Skip,
        UnmappedMemberHandling = JsonUnmappedMemberHandling.Skip,
    };

    /// <summary>
    /// Tries the Firely Terminal cache layout (<c>&lt;dir&gt;/&lt;name&gt;#&lt;version&gt;/package</c>)
    /// first, then falls back to the flat npm layout (<c>&lt;dir&gt;/&lt;name&gt;</c>).
    /// </summary>
    public string ResolvePackageContentDir(
        string fhirPackagesDir,
        string packageName,
        string packageVersion
    )
    {
        var firelyLayout = Path.Combine(
            fhirPackagesDir,
            $"{packageName}#{packageVersion}",
            "package"
        );
        if (Directory.Exists(firelyLayout))
            return firelyLayout;
        return Path.Combine(fhirPackagesDir, packageName);
    }

    public IgPackageModel Scan(string packageContentDir, string packageName, string packageVersion)
    {
        var resources = ReadResources(packageContentDir);

        var codeSystems = new SortedDictionary<string, string>(StringComparer.Ordinal);
        var profiles = new SortedDictionary<string, string>(StringComparer.Ordinal);
        var extensions = new SortedDictionary<string, string>(StringComparer.Ordinal);
        var codeSystemConcepts = new Dictionary<string, IReadOnlyList<ConceptConstant>>();
        var extensionValueTypes = new Dictionary<string, ExtensionValueType>();

        foreach (var resource in resources)
        {
            if (resource.ResourceType != "CodeSystem")
                continue;
            if (resource.Id is null || resource.Url is null)
                continue;
            ClassifyCodeSystem(
                resource,
                NameUtils.ToConstantName(resource.Id),
                resource.Url,
                codeSystems,
                codeSystemConcepts
            );
        }

        var codeSystemUrlByValueSetUrl = IndexSingleCodeSystemValueSets(resources);

        foreach (var resource in resources)
        {
            if (resource.ResourceType != "StructureDefinition")
                continue;
            if (resource.Id is null || resource.Url is null)
                continue;
            ClassifyStructureDefinition(
                resource,
                NameUtils.ToConstantName(resource.Id),
                resource.Url,
                profiles,
                extensions,
                extensionValueTypes,
                codeSystemUrlByValueSetUrl
            );
        }

        var namingSystems = new SortedDictionary<string, NamingSystemEntry>(StringComparer.Ordinal);
        foreach (var resource in resources)
        {
            if (resource.ResourceType != "NamingSystem")
                continue;
            if (resource.Id is null || resource.UniqueId is null || resource.UniqueId.Count == 0)
                continue;
            var constantName = NameUtils.ToConstantName(resource.Id);
            var byTypeMutable = new SortedDictionary<string, List<string>>(StringComparer.Ordinal);
            foreach (var uid in resource.UniqueId)
            {
                if (uid.Type is null || uid.Value is null)
                    continue;
                if (!byTypeMutable.TryGetValue(uid.Type, out var list))
                    byTypeMutable[uid.Type] = list = [];
                list.Add(uid.Value);
            }
            if (byTypeMutable.Count == 0)
                continue;
            var byType = new SortedDictionary<string, IReadOnlyList<string>>(
                StringComparer.Ordinal
            );
            foreach (var (type, values) in byTypeMutable)
                byType[type] = values;
            namingSystems[constantName] = new NamingSystemEntry(resource.Description, byType);
        }

        return new IgPackageModel(
            packageName,
            packageVersion,
            codeSystems,
            profiles,
            extensions,
            codeSystemConcepts,
            extensionValueTypes,
            namingSystems
        );
    }

    private static List<FhirResourceSummary> ReadResources(string packageContentDir)
    {
        var results = new List<FhirResourceSummary>();
        foreach (var file in Directory.EnumerateFiles(packageContentDir, "*.json"))
        {
            using var stream = File.OpenRead(file);
            var resource = JsonSerializer.Deserialize<FhirResourceSummary>(stream, JsonOptions);
            if (resource is not null)
                results.Add(resource);
        }
        return results;
    }

    private static void ClassifyStructureDefinition(
        FhirResourceSummary resource,
        string constantName,
        string url,
        SortedDictionary<string, string> profiles,
        SortedDictionary<string, string> extensions,
        Dictionary<string, ExtensionValueType> extensionValueTypes,
        Dictionary<string, string> codeSystemUrlByValueSetUrl
    )
    {
        if (resource.Kind == "logical" || resource.Derivation == "specialization")
            return;

        if (
            resource.Kind == "complex-type"
            && resource.Derivation == "constraint"
            && resource.Type == "Extension"
        )
        {
            extensions[constantName] = url;
            extensionValueTypes[constantName] = ExtensionValueTypeFor(
                resource,
                codeSystemUrlByValueSetUrl
            );
            return;
        }

        if (resource.Kind == "resource" && resource.Derivation == "constraint")
        {
            profiles[constantName] = resource.Version is null ? url : $"{url}|{resource.Version}";
        }
    }

    private static ExtensionValueType ExtensionValueTypeFor(
        FhirResourceSummary resource,
        Dictionary<string, string> codeSystemUrlByValueSetUrl
    )
    {
        var elements = resource.Snapshot?.Element;
        if (elements is null)
            return ExtensionValueType.None;

        foreach (var element in elements)
        {
            if (element.Path != "Extension.value[x]")
                continue;
            var types = element.Type;
            if (element.Max == "0" || types is null || types.Count == 0)
                return ExtensionValueType.None;
            if (types.Count > 1)
                return ExtensionValueType.ChoiceType;
            var code = types[0].Code;
            if (code is null)
                return ExtensionValueType.None;
            var boundUrl = BoundCodeSystemUrl(elements, element, code, codeSystemUrlByValueSetUrl);
            return boundUrl is null
                ? ExtensionValueType.Fixed(code)
                : ExtensionValueType.BoundCoding(code, boundUrl);
        }
        return ExtensionValueType.None;
    }

    private static string? BoundCodeSystemUrl(
        List<FhirElement> elements,
        FhirElement valueElement,
        string valueTypeCode,
        Dictionary<string, string> codeSystemUrlByValueSetUrl
    )
    {
        if (valueTypeCode != "CodeableConcept" && valueTypeCode != "Coding")
            return null;

        var systemPath =
            valueTypeCode == "CodeableConcept"
                ? "Extension.value[x].coding.system"
                : "Extension.value[x].system";

        foreach (var element in elements)
        {
            if (element.Path == systemPath && element.FixedUri is not null)
                return element.FixedUri;
        }

        var binding = valueElement.Binding;
        if (binding is null || binding.Strength != "required" || binding.ValueSet is null)
            return null;

        var valueSetUrl = binding.ValueSet;
        var sep = valueSetUrl.IndexOf('|');
        var bareUrl = sep < 0 ? valueSetUrl : valueSetUrl[..sep];
        return codeSystemUrlByValueSetUrl.GetValueOrDefault(bareUrl);
    }

    private static Dictionary<string, string> IndexSingleCodeSystemValueSets(
        List<FhirResourceSummary> resources
    )
    {
        var result = new Dictionary<string, string>();
        foreach (var resource in resources)
        {
            if (resource.ResourceType != "ValueSet")
                continue;
            if (resource.Url is null)
                continue;
            var csUrl = SingleCodeSystemUrl(resource.Compose);
            if (csUrl is not null)
                result[resource.Url] = csUrl;
        }
        return result;
    }

    private static string? SingleCodeSystemUrl(FhirCompose? compose)
    {
        if (compose is null)
            return null;
        if (compose.Exclude is { Count: > 0 })
            return null;
        var includes = compose.Include;
        if (includes is null || includes.Count != 1)
            return null;
        FhirComposeInclude only = includes[0];
        if (only.ValueSet is { Count: > 0 })
            return null;
        return only.System;
    }

    private static void ClassifyCodeSystem(
        FhirResourceSummary resource,
        string constantName,
        string url,
        SortedDictionary<string, string> codeSystems,
        Dictionary<string, IReadOnlyList<ConceptConstant>> codeSystemConcepts
    )
    {
        codeSystems[constantName] = url;
        if (resource.Content != "complete" || resource.ConceptList is null)
            return;
        IReadOnlyList<ConceptConstant> concepts = FlattenConcepts(resource.ConceptList);
        if (concepts.Count > 0)
            codeSystemConcepts[constantName] = concepts;
    }

    private static IReadOnlyList<ConceptConstant> FlattenConcepts(List<FhirConcept> concepts)
    {
        var result = new List<ConceptConstant>();
        var usedNames = new HashSet<string>(StringComparer.Ordinal);
        FlattenConcepts(concepts, result, usedNames);
        return result;
    }

    private static void FlattenConcepts(
        List<FhirConcept> concepts,
        List<ConceptConstant> result,
        HashSet<string> usedNames
    )
    {
        foreach (var concept in concepts)
        {
            if (concept.Code is not null)
                result.Add(
                    new ConceptConstant(
                        UniqueMemberName(concept.Code, usedNames),
                        concept.Code,
                        concept.Display
                    )
                );
            if (concept.Children is not null)
                FlattenConcepts(concept.Children, result, usedNames);
        }
    }

    private static string UniqueMemberName(string code, HashSet<string> usedNames)
    {
        var baseName = NameUtils.ToEnumMemberName(code);
        var name = baseName;
        var suffix = 2;
        while (!usedNames.Add(name))
        {
            name = $"{baseName}_{suffix}";
            suffix++;
        }
        return name;
    }
}

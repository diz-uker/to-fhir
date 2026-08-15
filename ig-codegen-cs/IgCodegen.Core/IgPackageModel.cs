namespace IgCodegen;

/// <summary>
/// Classified canonical URLs of a single FHIR IG package, keyed by the generated constant name
/// (e.g. <c>MII_CS_ONKO_INTENTION</c>).
/// </summary>
public sealed record IgPackageModel(
    string PackageName,
    string PackageVersion,
    IReadOnlyDictionary<string, string> CodeSystems,
    IReadOnlyDictionary<string, string> Profiles,
    IReadOnlyDictionary<string, string> Extensions,
    IReadOnlyDictionary<string, IReadOnlyList<ConceptConstant>> CodeSystemConcepts,
    IReadOnlyDictionary<string, ExtensionValueType> ExtensionValueTypes,
    IReadOnlyDictionary<string, NamingSystemEntry> NamingSystems
);

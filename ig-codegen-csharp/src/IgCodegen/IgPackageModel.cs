namespace Dizuker.IgCodegen;

/// <summary>
/// Classified canonical URLs of a single FHIR IG package, keyed by the generated C# constant name
/// (e.g. <c>MII_CS_ONKO_INTENTION</c>).
///
/// <para><see cref="CodeSystemConcepts"/> holds, for each entry in <see cref="CodeSystems"/> that
/// is a locally defined (<c>content == "complete"</c>) CodeSystem, the flattened list of its
/// concepts - everything needed to additionally render a static class of Coding properties.
/// Entries absent from this dictionary (e.g. external terminologies like SNOMED CT or LOINC,
/// which FHIR doesn't redistribute inline) only get the plain URL constant.</para>
/// </summary>
public sealed record IgPackageModel(
    string PackageName,
    string PackageVersion,
    IReadOnlyDictionary<string, string> CodeSystems,
    IReadOnlyDictionary<string, string> Profiles,
    IReadOnlyDictionary<string, string> Extensions,
    IReadOnlyDictionary<string, List<ConceptConstant>> CodeSystemConcepts);

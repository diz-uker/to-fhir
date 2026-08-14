namespace IgCodegen;

/// <summary>
/// The classified unique identifiers of a single FHIR NamingSystem, grouped by their
/// <c>type</c> (e.g. <c>"uri"</c>, <c>"oid"</c>).
/// </summary>
public sealed record NamingSystemEntry(
    string? Description,
    IReadOnlyDictionary<string, IReadOnlyList<string>> ByType
);

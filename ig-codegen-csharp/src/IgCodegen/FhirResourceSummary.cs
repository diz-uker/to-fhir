using System.Text.Json.Serialization;

namespace Dizuker.IgCodegen;

/// <summary>The subset of a FHIR resource JSON file's fields needed to classify it.</summary>
public sealed record FhirResourceSummary(
    [property: JsonPropertyName("resourceType")] string? ResourceType,
    [property: JsonPropertyName("id")] string? Id,
    [property: JsonPropertyName("url")] string? Url,
    [property: JsonPropertyName("version")] string? Version,
    [property: JsonPropertyName("kind")] string? Kind,
    [property: JsonPropertyName("derivation")] string? Derivation,
    [property: JsonPropertyName("type")] string? Type,
    [property: JsonPropertyName("content")] string? Content,
    [property: JsonPropertyName("concept")] List<FhirResourceSummary.Concept>? ConceptList)
{
    /// <summary>A CodeSystem.concept entry; <see cref="Children"/> holds nested child concepts, if any.</summary>
    public sealed record Concept(
        [property: JsonPropertyName("code")] string? Code,
        [property: JsonPropertyName("display")] string? Display,
        [property: JsonPropertyName("concept")] List<Concept>? Children);
}

using System.Text.Json.Serialization;

namespace IgCodegen;

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
    [property: JsonPropertyName("description")] string? Description,
    [property: JsonPropertyName("concept")] List<FhirConcept>? ConceptList,
    [property: JsonPropertyName("snapshot")] FhirSnapshot? Snapshot,
    [property: JsonPropertyName("compose")] FhirCompose? Compose,
    [property: JsonPropertyName("uniqueId")] List<FhirUniqueId>? UniqueId
);

/// <summary>A CodeSystem.concept entry; <c>Children</c> holds nested child concepts, if any.</summary>
public sealed record FhirConcept(
    [property: JsonPropertyName("code")] string? Code,
    [property: JsonPropertyName("display")] string? Display,
    [property: JsonPropertyName("concept")] List<FhirConcept>? Children
);

/// <summary>A StructureDefinition.snapshot, reduced to what's needed to inspect <c>value[x]</c>.</summary>
public sealed record FhirSnapshot(
    [property: JsonPropertyName("element")] List<FhirElement>? Element
);

/// <summary>A snapshot element, reduced to what's needed to inspect <c>Extension.value[x]</c>.</summary>
public sealed record FhirElement(
    [property: JsonPropertyName("path")] string? Path,
    [property: JsonPropertyName("max")] string? Max,
    [property: JsonPropertyName("type")] List<FhirElementType>? Type,
    [property: JsonPropertyName("fixedUri")] string? FixedUri,
    [property: JsonPropertyName("binding")] FhirBinding? Binding
);

/// <summary>One entry of an element's <c>type</c> array, e.g. <c>{"code": "CodeableConcept"}</c>.</summary>
public sealed record FhirElementType([property: JsonPropertyName("code")] string? Code);

/// <summary>An element's terminology binding.</summary>
public sealed record FhirBinding(
    [property: JsonPropertyName("strength")] string? Strength,
    [property: JsonPropertyName("valueSet")] string? ValueSet
);

/// <summary>A ValueSet.compose, reduced to what's needed to tell whether it draws from one CodeSystem.</summary>
public sealed record FhirCompose(
    [property: JsonPropertyName("include")] List<FhirComposeInclude>? Include,
    [property: JsonPropertyName("exclude")] List<FhirComposeInclude>? Exclude
);

/// <summary>One entry of <c>ValueSet.compose.include</c> (or <c>.exclude</c>).</summary>
public sealed record FhirComposeInclude(
    [property: JsonPropertyName("system")] string? System,
    [property: JsonPropertyName("concept")] List<object>? Concept,
    [property: JsonPropertyName("filter")] List<object>? Filter,
    [property: JsonPropertyName("valueSet")] List<string>? ValueSet
);

/// <summary>One entry of a <c>NamingSystem.uniqueId</c> array.</summary>
public sealed record FhirUniqueId(
    [property: JsonPropertyName("type")] string? Type,
    [property: JsonPropertyName("value")] string? Value
);

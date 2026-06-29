namespace Dizuker.IgCodegen;

/// <summary>A single, already name-sanitized CodeSystem concept, ready to render as a static property.</summary>
public sealed record ConceptConstant(string PropertyName, string Code, string? Display);

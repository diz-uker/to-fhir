namespace IgCodegen;

/// <summary>A single, already name-sanitized CodeSystem concept, ready to render as an enum constant.</summary>
public sealed record ConceptConstant(string ConstantName, string Code, string? Display);

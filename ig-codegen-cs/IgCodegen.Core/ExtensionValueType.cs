namespace IgCodegen;

/// <summary>
/// The <c>Extension.value[x]</c> shape of a FHIR extension's StructureDefinition,
/// determining the parameter type of its generated factory method.
/// </summary>
public sealed record ExtensionValueType(
    string? FhirTypeCode,
    bool Choice,
    string? BoundCodeSystemUrl
)
{
    public static readonly ExtensionValueType None = new(null, false, null);
    public static readonly ExtensionValueType ChoiceType = new(null, true, null);

    public static ExtensionValueType Fixed(string fhirTypeCode) => new(fhirTypeCode, false, null);

    public static ExtensionValueType BoundCoding(string fhirTypeCode, string codeSystemUrl) =>
        new(fhirTypeCode, false, codeSystemUrl);
}

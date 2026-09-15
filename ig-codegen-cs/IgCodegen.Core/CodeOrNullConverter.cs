using System.Text.Json;
using System.Text.Json.Serialization;

namespace IgCodegen;

/// <summary>
/// Reads a JSON string, and yields <c>null</c> for any other shape rather than failing.
/// </summary>
/// <remarks>
/// FHIR reuses the same field name across resource types with incompatible shapes, so a field that
/// is a plain <c>code</c> on the resource type <see cref="FhirResourceSummary"/> is modelled for
/// can be an object on another resource type that happens to share the field name - e.g.
/// <c>StructureDefinition.type</c> is a <c>code</c> while <c>NamingSystem.type</c> is a
/// <c>CodeableConcept</c>. Reading the foreign shape as absent keeps one stray resource from
/// failing the scan of an entire package.
/// </remarks>
public sealed class CodeOrNullConverter : JsonConverter<string?>
{
    public override string? Read(
        ref Utf8JsonReader reader,
        Type typeToConvert,
        JsonSerializerOptions options
    )
    {
        if (reader.TokenType == JsonTokenType.String)
            return reader.GetString();
        reader.Skip();
        return null;
    }

    public override void Write(Utf8JsonWriter writer, string? value, JsonSerializerOptions options)
    {
        if (value is null)
            writer.WriteNullValue();
        else
            writer.WriteStringValue(value);
    }
}

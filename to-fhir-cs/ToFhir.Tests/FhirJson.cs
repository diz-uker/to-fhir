using System.Text.Json;
using Hl7.Fhir.Model;
using Hl7.Fhir.Serialization;

namespace ToFhir.Tests;

/// <summary>Pretty-printing FHIR JSON serialization, shared by the snapshot tests.</summary>
internal static class FhirJson
{
    private static readonly JsonSerializerOptions Options = new JsonSerializerOptions()
        .ForFhir(ModelInfo.ModelInspector)
        .Pretty();

    public static string Serialize(Resource resource) =>
        JsonSerializer.Serialize(resource, Options);
}

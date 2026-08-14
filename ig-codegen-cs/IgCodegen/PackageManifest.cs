using System.Text.Json;
using System.Text.Json.Serialization;

namespace IgCodegen;

/// <summary>
/// A FHIR package manifest (<c>package.json</c>, restored by Firely Terminal's <c>fhir restore</c>).
/// </summary>
public sealed record PackageManifest(
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("version")] string Version,
    [property: JsonPropertyName("dependencies")] Dictionary<string, string> Dependencies
)
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        AllowTrailingCommas = true,
        ReadCommentHandling = JsonCommentHandling.Skip,
    };

    public static PackageManifest Read(string packageJsonFile)
    {
        using var stream = File.OpenRead(packageJsonFile);
        return JsonSerializer.Deserialize<PackageManifest>(stream, JsonOptions)
            ?? throw new InvalidDataException($"Could not deserialise {packageJsonFile}");
    }
}

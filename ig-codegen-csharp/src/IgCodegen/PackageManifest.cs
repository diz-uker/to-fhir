using System.Text.Json;
using System.Text.Json.Serialization;

namespace Dizuker.IgCodegen;

/// <summary>
/// A FHIR package manifest (<c>package.json</c>, restored by Firely Terminal's <c>fhir restore</c>).
/// Despite the filename, this is the FHIR package ecosystem's manifest shape, not npm's.
/// </summary>
public sealed record PackageManifest(
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("version")] string Version,
    [property: JsonPropertyName("dependencies")] Dictionary<string, string> Dependencies)
{
    public static PackageManifest Read(string packageJsonFile)
    {
        string json = File.ReadAllText(packageJsonFile);
        return JsonSerializer.Deserialize<PackageManifest>(json)
            ?? throw new InvalidOperationException($"Failed to parse {packageJsonFile}");
    }
}

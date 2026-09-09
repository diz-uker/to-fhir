using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;
using DiffEngine;

namespace ToFhir.Tests;

/// <summary>Global Verify configuration for the snapshot tests.</summary>
public static partial class ModuleInitializer
{
    [GeneratedRegex("\"(occurredDateTime|recorded)\": \"[^\"]*\"")]
    private static partial Regex ProvenanceTimestamps();

    [ModuleInitializer]
    public static void Initialize()
    {
        // Keep the snapshots next to the tests, mirroring the Java side's snapshots/ directory.
        UseSourceFileRelativeDirectory("Snapshots");

        // Verify defaults to UTF-8 *with* a BOM; write plain UTF-8, like the Java snapshots.
        VerifierSettings.UseUtf8NoBom();

        // Provenance.occurred and Provenance.recorded are wall-clock timestamps, so they would
        // differ on every run.
        VerifierSettings.AddScrubber(builder =>
        {
            var scrubbed = ProvenanceTimestamps()
                .Replace(builder.ToString(), "\"$1\": \"2000-01-01T11:11:11Z\"");
            builder.Clear();
            builder.Append(scrubbed);
        });

        // Never pop open a diff tool from a test run.
        DiffRunner.Disabled = true;
    }
}

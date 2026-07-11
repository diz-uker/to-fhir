using System.Text.RegularExpressions;

namespace Dizuker.IgCodegen;

/// <summary>
/// Converts FHIR resource ids (kebab-case or, occasionally, PascalCase) into C# constant names.
/// </summary>
public static partial class NameUtils
{
    [GeneratedRegex("(?<=[a-z0-9])(?=[A-Z])")]
    private static partial Regex LowerOrDigitFollowedByUpper();

    [GeneratedRegex("[^A-Za-z0-9]+")]
    private static partial Regex NonAlphanumericRun();

    public static string ToConstantName(string id)
    {
        string withWordBoundaries = LowerOrDigitFollowedByUpper().Replace(id, "_");
        return NonAlphanumericRun().Replace(withWordBoundaries, "_").ToUpperInvariant();
    }

    /// <summary>Converts a dot-separated FHIR package name segment into a PascalCase C# class name.</summary>
    public static string ToPascalCase(string segment)
    {
        string withWordBoundaries = LowerOrDigitFollowedByUpper().Replace(segment, "_");
        var words = NonAlphanumericRun().Split(withWordBoundaries).Where(w => w.Length > 0);
        return string.Concat(words.Select(w => char.ToUpperInvariant(w[0]) + w[1..].ToLowerInvariant()));
    }

    /// <summary>
    /// Converts a <c>SCREAMING_SNAKE_CASE</c> constant name (as produced by <see cref="ToConstantName"/>)
    /// into a PascalCase C# identifier, e.g. <c>MII_PR_DIAGNOSE_CONDITION -&gt; MiiPrDiagnoseCondition</c>.
    /// Short FHIR-type abbreviations (pr, cs, ex, ...) are not treated specially - only their first
    /// letter is capitalized, matching standard C# naming style.
    /// </summary>
    public static string ToPropertyCase(string constantName)
    {
        var words = constantName.Split('_').Where(w => w.Length > 0);
        return string.Concat(words.Select(w => char.ToUpperInvariant(w[0]) + w[1..].ToLowerInvariant()));
    }

    /// <summary>
    /// Converts a CodeSystem concept code into a valid C# identifier. Real FHIR code systems use
    /// codes that aren't valid C# identifiers as-is - e.g. purely numeric codes (<c>"2"</c>), or
    /// receptor-status codes ending in <c>+</c>/<c>-</c> (<c>"mol+"</c>, <c>"i-"</c>), which would
    /// otherwise collide once the sign is stripped by <see cref="ToConstantName"/>.
    ///
    /// <para>Deliberately not run through <see cref="ToPropertyCase"/>: these are opaque codes, not
    /// English words, so word-casing them (lowercasing everything after each word's first letter)
    /// would mangle codes like <c>"T1a1"</c>. Stays in the same <c>SCREAMING_SNAKE_CASE</c> form
    /// Java/Python use for enum constants - a plain, unmolested identifier is more important here
    /// than C#'s usual PascalCase property style.</para>
    ///
    /// <para>This does not guarantee uniqueness across a whole CodeSystem by itself - callers must
    /// still disambiguate any remaining collisions (e.g. two codes differing only in characters
    /// this method treats as equivalent).</para>
    /// </summary>
    public static string ToIdentifierName(string code)
    {
        string baseCode;
        string suffix;
        if (code.EndsWith('+'))
        {
            baseCode = code[..^1];
            suffix = "_POS";
        }
        else if (code.EndsWith('-'))
        {
            baseCode = code[..^1];
            suffix = "_NEG";
        }
        else
        {
            baseCode = code;
            suffix = "";
        }

        string identifier = ToConstantName(baseCode) + suffix;
        if (identifier.Length == 0)
        {
            return "_";
        }
        return char.IsDigit(identifier[0]) ? "_" + identifier : identifier;
    }
}

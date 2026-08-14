using System.Text.RegularExpressions;

namespace IgCodegen;

/// <summary>Converts FHIR resource ids (kebab-case or PascalCase) into C# identifier names.</summary>
public static partial class NameUtils
{
    [GeneratedRegex(@"(?<=[a-z0-9])(?=[A-Z])")]
    private static partial Regex LowerOrDigitFollowedByUpper();

    [GeneratedRegex(@"[^A-Za-z0-9]+")]
    private static partial Regex NonAlphanumericRun();

    [GeneratedRegex(@"_")]
    private static partial Regex Underscore();

    public static string ToConstantName(string id)
    {
        var withWordBoundaries = LowerOrDigitFollowedByUpper().Replace(id, "_");
        return NonAlphanumericRun().Replace(withWordBoundaries, "_").ToUpperInvariant();
    }

    /// <summary>Converts a dot-separated FHIR package name segment into a PascalCase C# identifier.</summary>
    public static string ToPascalCase(string segment)
    {
        var withWordBoundaries = LowerOrDigitFollowedByUpper().Replace(segment, "_");
        var sb = new System.Text.StringBuilder();
        foreach (var word in NonAlphanumericRun().Split(withWordBoundaries))
        {
            if (word.Length == 0)
                continue;
            sb.Append(char.ToUpperInvariant(word[0]));
            if (word.Length > 1)
                sb.Append(word[1..].ToLowerInvariant());
        }
        return sb.ToString();
    }

    /// <summary>
    /// Converts a CodeSystem concept code into a valid C# enum member name, applying the same
    /// <c>+</c>/<c>-</c> suffix disambiguation and digit-prefix guard as the Java counterpart.
    /// </summary>
    public static string ToEnumMemberName(string code)
    {
        string basePart,
            suffix;
        if (code.EndsWith('+'))
        {
            basePart = code[..^1];
            suffix = "_POS";
        }
        else if (code.EndsWith('-'))
        {
            basePart = code[..^1];
            suffix = "_NEG";
        }
        else
        {
            basePart = code;
            suffix = "";
        }

        var name = ToConstantName(basePart) + suffix;
        if (name.Length == 0)
            return "_";
        return char.IsDigit(name[0]) ? "_" + name : name;
    }
}

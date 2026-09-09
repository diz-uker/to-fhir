using Hl7.Fhir.Model;

namespace ToFhir;

/// <summary>
/// Codes from the <c>http://terminology.hl7.org/CodeSystem/data-absent-reason</c> code system, see
/// <see href="https://hl7.org/fhir/R4B/codesystem-data-absent-reason.html">CodeSystem:
/// DataAbsentReason</see>.
/// </summary>
public enum DataAbsentReasonCode
{
    /// <summary>The value is expected to exist but is not known.</summary>
    Unknown,

    /// <summary>The source was asked but does not know the value.</summary>
    AskedUnknown,

    /// <summary>There is reason to expect (from the workflow) that the value may become known.</summary>
    TempUnknown,

    /// <summary>The workflow didn't lead to this value being known.</summary>
    NotAsked,

    /// <summary>The source was asked but declined to answer.</summary>
    AskedDeclined,

    /// <summary>The information is not available due to security, privacy or related reasons.</summary>
    Masked,

    /// <summary>There is no proper value for this element (e.g. last menstrual period for a male).</summary>
    NotApplicable,

    /// <summary>The source system wasn't capable of supporting this element.</summary>
    Unsupported,

    /// <summary>The content of the data is represented in the resource narrative.</summary>
    AsText,

    /// <summary>Some system or workflow process error means that the information is not available.</summary>
    Error,

    /// <summary>
    /// The numeric value is undefined or unrepresentable due to a floating point processing error.
    /// </summary>
    NotANumber,

    /// <summary>
    /// The numeric value is excessively low and unrepresentable due to a floating point processing
    /// error.
    /// </summary>
    NegativeInfinity,

    /// <summary>
    /// The numeric value is excessively high and unrepresentable due to a floating point processing
    /// error.
    /// </summary>
    PositiveInfinity,

    /// <summary>
    /// The value is not available because the observation procedure (test, etc.) was not performed.
    /// </summary>
    NotPerformed,

    /// <summary>
    /// The value is not permitted in this context (e.g. due to profiles, or the base data types).
    /// </summary>
    NotPermitted,
}

/// <summary>Extension methods for <see cref="DataAbsentReasonCode"/>.</summary>
public static class DataAbsentReasonCodeExtensions
{
    /// <summary>The code system URL backing <see cref="DataAbsentReasonCode"/>.</summary>
    public const string CodeSystemUrl = "http://terminology.hl7.org/CodeSystem/data-absent-reason";

    /// <returns>The FHIR code for this concept, e.g. <c>not-asked</c>.</returns>
    public static string Code(this DataAbsentReasonCode value) =>
        value switch
        {
            DataAbsentReasonCode.Unknown => "unknown",
            DataAbsentReasonCode.AskedUnknown => "asked-unknown",
            DataAbsentReasonCode.TempUnknown => "temp-unknown",
            DataAbsentReasonCode.NotAsked => "not-asked",
            DataAbsentReasonCode.AskedDeclined => "asked-declined",
            DataAbsentReasonCode.Masked => "masked",
            DataAbsentReasonCode.NotApplicable => "not-applicable",
            DataAbsentReasonCode.Unsupported => "unsupported",
            DataAbsentReasonCode.AsText => "as-text",
            DataAbsentReasonCode.Error => "error",
            DataAbsentReasonCode.NotANumber => "not-a-number",
            DataAbsentReasonCode.NegativeInfinity => "negative-infinity",
            DataAbsentReasonCode.PositiveInfinity => "positive-infinity",
            DataAbsentReasonCode.NotPerformed => "not-performed",
            DataAbsentReasonCode.NotPermitted => "not-permitted",
            _ => throw new ArgumentOutOfRangeException(nameof(value), value, null),
        };

    /// <returns>A fresh <see cref="Coding"/> for this concept.</returns>
    public static Coding Coding(this DataAbsentReasonCode value) =>
        new(CodeSystemUrl, value.Code());

    /// <returns>
    /// A fresh data-absent-reason <see cref="Extension"/> carrying this concept, equivalent to
    /// <see cref="FhirExtensions.DataAbsentReason(DataAbsentReasonCode)"/>.
    /// </returns>
    public static Extension ToExtension(this DataAbsentReasonCode value) =>
        FhirExtensions.DataAbsentReason(value);
}

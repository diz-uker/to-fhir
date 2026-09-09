using Hl7.Fhir.Model;

namespace ToFhir;

/// <summary>Factory methods for default <see cref="Extension"/> templates used across to-FHIR®.</summary>
public static class FhirExtensions
{
    /// <summary>The data-absent-reason extension URL.</summary>
    public const string DataAbsentReasonUrl =
        "http://hl7.org/fhir/StructureDefinition/data-absent-reason";

    /// <summary>Returns a fresh data-absent-reason extension template, without a value.</summary>
    public static Extension DataAbsentReason() => new() { Url = DataAbsentReasonUrl };

    /// <summary>
    /// Returns a fresh data-absent-reason extension pre-populated with <paramref name="reason"/>.
    /// </summary>
    /// <param name="reason">The reason the value is absent.</param>
    public static Extension DataAbsentReason(DataAbsentReasonCode reason) =>
        new(DataAbsentReasonUrl, new Code(reason.Code()));
}

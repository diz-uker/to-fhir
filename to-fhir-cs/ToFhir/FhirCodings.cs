using Hl7.Fhir.Model;

namespace ToFhir;

/// <summary>
/// Factory methods for default <see cref="Coding"/> templates (system + version, no code/display)
/// used across to-FHIR®. Each method returns a fresh instance, since <see cref="Coding"/> is
/// mutable.
/// </summary>
public static class FhirCodings
{
    /// <summary>Returns a fresh LOINC coding template.</summary>
    public static Coding Loinc() => new() { System = FhirSystems.Loinc, Version = "2.82" };

    /// <summary>Returns a fresh SNOMED CT coding template.</summary>
    public static Coding Snomed() =>
        new()
        {
            System = FhirSystems.Snomed,
            Version = "http://snomed.info/sct/900000000000207008/version/20250701",
        };

    /// <summary>Returns a fresh OPS coding template.</summary>
    public static Coding Ops() => new() { System = FhirSystems.Ops, Version = "2026" };

    /// <summary>Returns a fresh ATC coding template.</summary>
    public static Coding Atc() => new() { System = FhirSystems.Atc, Version = "2026" };

    /// <summary>Returns a fresh ICD-10-GM coding template.</summary>
    public static Coding Icd10Gm() => new() { System = FhirSystems.Icd10Gm, Version = "2026" };

    /// <summary>Returns a fresh PZN coding template.</summary>
    public static Coding Pzn() => new() { System = FhirSystems.Pzn };
}

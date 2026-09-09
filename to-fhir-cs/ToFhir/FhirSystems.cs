namespace ToFhir;

/// <summary>Canonical FHIR coding system URIs used across to-FHIR®.</summary>
public static class FhirSystems
{
    /// <summary>The UCUM system.</summary>
    public const string Ucum = "http://unitsofmeasure.org";

    /// <summary>The LOINC system.</summary>
    public const string Loinc = "http://loinc.org";

    /// <summary>The SNOMED CT system.</summary>
    public const string Snomed = "http://snomed.info/sct";

    /// <summary>The ATC system.</summary>
    public const string Atc = "http://fhir.de/CodeSystem/bfarm/atc";

    /// <summary>The OPS system.</summary>
    public const string Ops = "http://fhir.de/CodeSystem/bfarm/ops";

    /// <summary>The ICD-10-GM system.</summary>
    public const string Icd10Gm = "http://fhir.de/CodeSystem/bfarm/icd-10-gm";

    /// <summary>The PZN system.</summary>
    public const string Pzn = "http://fhir.de/CodeSystem/ifa/pzn";
}

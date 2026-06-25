package io.github.dizuker.tofhir;

/** Canonical FHIR coding system URIs used across to-FHIR®. */
public final class FhirSystems {
  /** The UCUM system. */
  public static final String UCUM = "http://unitsofmeasure.org";

  /** The LOINC system. */
  public static final String LOINC = "http://loinc.org";

  /** The SNOMED CT system. */
  public static final String SNOMED = "http://snomed.info/sct";

  /** The ATC system. */
  public static final String ATC = "http://fhir.de/CodeSystem/bfarm/atc";

  /** The OPS system. */
  public static final String OPS = "http://fhir.de/CodeSystem/bfarm/ops";

  /** The ICD-10-GM system. */
  public static final String ICD10GM = "http://fhir.de/CodeSystem/bfarm/icd-10-gm";

  /** The PZN system. */
  public static final String PZN = "http://fhir.de/CodeSystem/ifa/pzn";

  private FhirSystems() {
    // Utility class, prevent instantiation
  }
}

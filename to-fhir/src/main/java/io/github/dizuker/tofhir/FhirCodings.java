package io.github.dizuker.tofhir;

import org.hl7.fhir.r4.model.Coding;

/**
 * Factory methods for default {@link Coding} templates (system + version, no code/display) used
 * across to-FHIR®. Each method returns a fresh instance, since {@link Coding} is mutable.
 */
public final class FhirCodings {

  /** Returns a fresh LOINC coding template. */
  public static Coding loinc() {
    return new Coding().setSystem(FhirSystems.LOINC).setVersion("2.82");
  }

  /** Returns a fresh SNOMED CT coding template. */
  public static Coding snomed() {
    return new Coding()
        .setSystem(FhirSystems.SNOMED)
        .setVersion("http://snomed.info/sct/900000000000207008/version/20250701");
  }

  /** Returns a fresh OPS coding template. */
  public static Coding ops() {
    return new Coding().setSystem(FhirSystems.OPS).setVersion("2026");
  }

  /** Returns a fresh ATC coding template. */
  public static Coding atc() {
    return new Coding().setSystem(FhirSystems.ATC).setVersion("2026");
  }

  /** Returns a fresh ICD-10-GM coding template. */
  public static Coding icd10gm() {
    return new Coding().setSystem(FhirSystems.ICD10GM).setVersion("2026");
  }

  /** Returns a fresh PZN coding template. */
  public static Coding pzn() {
    return new Coding().setSystem(FhirSystems.PZN);
  }

  private FhirCodings() {
    // Utility class, prevent instantiation
  }
}

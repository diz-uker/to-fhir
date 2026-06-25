package io.github.dizuker.tofhir;

import org.hl7.fhir.r4.model.Extension;

/** Factory methods for default {@link Extension} templates used across to-FHIR®. */
public final class FhirExtensions {

  /** The data-absent-reason extension URL. */
  public static final String DATA_ABSENT_REASON_URL =
      "http://hl7.org/fhir/StructureDefinition/data-absent-reason";

  /** Returns a fresh data-absent-reason extension template. */
  public static Extension dataAbsentReason() {
    return new Extension().setUrl(DATA_ABSENT_REASON_URL);
  }

  private FhirExtensions() {
    // Utility class, prevent instantiation
  }
}

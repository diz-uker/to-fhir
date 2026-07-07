package io.github.dizuker.tofhir;

import org.hl7.fhir.r4.model.CodeType;
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

  /**
   * Factory methods for data-absent-reason extensions pre-populated with a code from the {@code
   * http://terminology.hl7.org/CodeSystem/data-absent-reason} code system, see <a
   * href="https://hl7.org/fhir/R4B/codesystem-data-absent-reason.html">CodeSystem:
   * DataAbsentReason</a>.
   */
  public static final class DataAbsentReason {

    /** The value is expected to exist but is not known. */
    public static Extension unknown() {
      return code("unknown");
    }

    /** The source was asked but does not know the value. */
    public static Extension askedUnknown() {
      return code("asked-unknown");
    }

    /** There is reason to expect (from the workflow) that the value may become known. */
    public static Extension tempUnknown() {
      return code("temp-unknown");
    }

    /** The workflow didn't lead to this value being known. */
    public static Extension notAsked() {
      return code("not-asked");
    }

    /** The source was asked but declined to answer. */
    public static Extension askedDeclined() {
      return code("asked-declined");
    }

    /** The information is not available due to security, privacy or related reasons. */
    public static Extension masked() {
      return code("masked");
    }

    /** There is no proper value for this element (e.g. last menstrual period for a male). */
    public static Extension notApplicable() {
      return code("not-applicable");
    }

    /** The source system wasn't capable of supporting this element. */
    public static Extension unsupported() {
      return code("unsupported");
    }

    /** The content of the data is represented in the resource narrative. */
    public static Extension asText() {
      return code("as-text");
    }

    /** Some system or workflow process error means that the information is not available. */
    public static Extension error() {
      return code("error");
    }

    /**
     * The numeric value is undefined or unrepresentable due to a floating point processing error.
     */
    public static Extension notANumber() {
      return code("not-a-number");
    }

    /**
     * The numeric value is excessively low and unrepresentable due to a floating point processing
     * error.
     */
    public static Extension negativeInfinity() {
      return code("negative-infinity");
    }

    /**
     * The numeric value is excessively high and unrepresentable due to a floating point processing
     * error.
     */
    public static Extension positiveInfinity() {
      return code("positive-infinity");
    }

    /**
     * The value is not available because the observation procedure (test, etc.) was not performed.
     */
    public static Extension notPerformed() {
      return code("not-performed");
    }

    /**
     * The value is not permitted in this context (e.g. due to profiles, or the base data types).
     */
    public static Extension notPermitted() {
      return code("not-permitted");
    }

    private static Extension code(String code) {
      return dataAbsentReason().setValue(new CodeType(code));
    }

    private DataAbsentReason() {
      // Utility class, prevent instantiation
    }
  }

  private FhirExtensions() {
    // Utility class, prevent instantiation
  }
}

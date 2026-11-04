package io.github.dizuker.tofhir;

import ca.uhn.fhir.util.ResourceUtil;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;

/** Factory methods for default {@link Extension} templates used across to-FHIR®. */
public final class FhirExtensions {

  /** The data-absent-reason extension URL. */
  public static final String DATA_ABSENT_REASON_URL = ResourceUtil.DATA_ABSENT_REASON_EXTENSION_URI;

  /** Returns a fresh data-absent-reason extension template, without a value. */
  public static Extension dataAbsentReason() {
    return new Extension().setUrl(DATA_ABSENT_REASON_URL);
  }

  /**
   * Returns a fresh data-absent-reason extension pre-populated with {@code reason}.
   *
   * @param reason the reason the value is absent
   * @throws IllegalArgumentException if {@code reason} is {@link
   *     Enumerations.DataAbsentReason#NULL}
   */
  public static Extension dataAbsentReason(Enumerations.DataAbsentReason reason) {
    return dataAbsentReason().setValue(new CodeType(DataAbsentReason.code(reason)));
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
      return dataAbsentReason(Enumerations.DataAbsentReason.UNKNOWN);
    }

    /** The source was asked but does not know the value. */
    public static Extension askedUnknown() {
      return dataAbsentReason(Enumerations.DataAbsentReason.ASKEDUNKNOWN);
    }

    /** There is reason to expect (from the workflow) that the value may become known. */
    public static Extension tempUnknown() {
      return dataAbsentReason(Enumerations.DataAbsentReason.TEMPUNKNOWN);
    }

    /** The workflow didn't lead to this value being known. */
    public static Extension notAsked() {
      return dataAbsentReason(Enumerations.DataAbsentReason.NOTASKED);
    }

    /** The source was asked but declined to answer. */
    public static Extension askedDeclined() {
      return dataAbsentReason(Enumerations.DataAbsentReason.ASKEDDECLINED);
    }

    /** The information is not available due to security, privacy or related reasons. */
    public static Extension masked() {
      return dataAbsentReason(Enumerations.DataAbsentReason.MASKED);
    }

    /** There is no proper value for this element (e.g. last menstrual period for a male). */
    public static Extension notApplicable() {
      return dataAbsentReason(Enumerations.DataAbsentReason.NOTAPPLICABLE);
    }

    /** The source system wasn't capable of supporting this element. */
    public static Extension unsupported() {
      return dataAbsentReason(Enumerations.DataAbsentReason.UNSUPPORTED);
    }

    /** The content of the data is represented in the resource narrative. */
    public static Extension asText() {
      return dataAbsentReason(Enumerations.DataAbsentReason.ASTEXT);
    }

    /** Some system or workflow process error means that the information is not available. */
    public static Extension error() {
      return dataAbsentReason(Enumerations.DataAbsentReason.ERROR);
    }

    /**
     * The numeric value is undefined or unrepresentable due to a floating point processing error.
     */
    public static Extension notANumber() {
      return dataAbsentReason(Enumerations.DataAbsentReason.NOTANUMBER);
    }

    /**
     * The numeric value is excessively low and unrepresentable due to a floating point processing
     * error.
     */
    public static Extension negativeInfinity() {
      return dataAbsentReason(Enumerations.DataAbsentReason.NEGATIVEINFINITY);
    }

    /**
     * The numeric value is excessively high and unrepresentable due to a floating point processing
     * error.
     */
    public static Extension positiveInfinity() {
      return dataAbsentReason(Enumerations.DataAbsentReason.POSITIVEINFINITY);
    }

    /**
     * The value is not available because the observation procedure (test, etc.) was not performed.
     */
    public static Extension notPerformed() {
      return dataAbsentReason(Enumerations.DataAbsentReason.NOTPERFORMED);
    }

    /**
     * The value is not permitted in this context (e.g. due to profiles, or the base data types).
     */
    public static Extension notPermitted() {
      return dataAbsentReason(Enumerations.DataAbsentReason.NOTPERMITTED);
    }

    /**
     * Returns a {@link CodeableConcept} carrying {@code reason}, for elements typed as a
     * data-absent-reason concept rather than the extension, e.g. {@code
     * Observation.dataAbsentReason}.
     *
     * @param reason the reason the value is absent
     * @throws IllegalArgumentException if {@code reason} is {@link
     *     Enumerations.DataAbsentReason#NULL}
     */
    public static CodeableConcept codeableConcept(Enumerations.DataAbsentReason reason) {
      return new CodeableConcept(
          new Coding()
              .setCode(code(reason))
              .setSystem(reason.getSystem())
              .setDisplay(reason.getDisplay()));
    }

    private static String code(Enumerations.DataAbsentReason reason) {
      if (reason == Enumerations.DataAbsentReason.NULL) {
        throw new IllegalArgumentException("DataAbsentReason.NULL has no code");
      }
      return reason.toCode();
    }

    private DataAbsentReason() {
      // Utility class, prevent instantiation
    }
  }

  private FhirExtensions() {
    // Utility class, prevent instantiation
  }
}

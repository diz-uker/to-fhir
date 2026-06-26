package io.github.dizuker.tofhir.config;

import io.github.dizuker.tofhir.FhirCodings;
import io.github.dizuker.tofhir.FhirExtensions;
import io.github.dizuker.tofhir.FhirSystems;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Properties for configuring to-FHIR utilities.
 *
 * <p>Every value defaults to the constants in {@code io.github.dizuker.tofhir}, so no {@code
 * application.yml} ships with this starter. Set {@code fhir.*} properties in the consuming
 * application to override individual defaults.
 *
 * @param systems FHIR systems
 * @param codings FHIR codings
 * @param extensions FHIR extensions
 */
@ConfigurationProperties(prefix = "fhir")
public record FhirProperties(
    @DefaultValue Systems systems, Codings codings, Extensions extensions) {

  /** Default constructor. */
  public FhirProperties {
    codings = codings != null ? codings : new Codings(null, null, null, null, null, null);
    extensions = extensions != null ? extensions : new Extensions(null);
  }

  /**
   * FHIR Systems
   *
   * @param ucum The units of measure system
   * @param loinc The LOINC system
   * @param snomed The SNOMED CT system
   * @param atc The ATC system
   * @param ops The OPS system
   * @param icd10gm The ICD-10-GM system
   */
  public record Systems(
      @DefaultValue(FhirSystems.UCUM) String ucum,
      @DefaultValue(FhirSystems.LOINC) String loinc,
      @DefaultValue(FhirSystems.SNOMED) String snomed,
      @DefaultValue(FhirSystems.ATC) String atc,
      @DefaultValue(FhirSystems.OPS) String ops,
      @DefaultValue(FhirSystems.ICD10GM) String icd10gm) {}

  /**
   * FHIR codings
   *
   * @param loinc LOINC Coding
   * @param snomed SNOMED CT Coding
   * @param ops OPS Coding
   * @param atc ATC Coding
   * @param icd10gm ICD-10-GM Coding
   * @param pzn PZN Coding
   */
  public record Codings(
      Coding loinc, Coding snomed, Coding ops, Coding atc, Coding icd10gm, Coding pzn) {
    /** Default constructor. */
    public Codings {
      loinc = loinc != null ? loinc : FhirCodings.loinc();
      snomed = snomed != null ? snomed : FhirCodings.snomed();
      ops = ops != null ? ops : FhirCodings.ops();
      atc = atc != null ? atc : FhirCodings.atc();
      icd10gm = icd10gm != null ? icd10gm : FhirCodings.icd10gm();
      pzn = pzn != null ? pzn : FhirCodings.pzn();
    }

    @Override
    public Coding loinc() {
      // return a fresh copy, otherwise the original instance will be modified
      return loinc.copy();
    }

    @Override
    public Coding snomed() {
      return snomed.copy();
    }

    @Override
    public Coding atc() {
      return atc.copy();
    }

    @Override
    public Coding ops() {
      return ops.copy();
    }

    @Override
    public Coding icd10gm() {
      return icd10gm.copy();
    }

    @Override
    public Coding pzn() {
      return pzn.copy();
    }
  }

  /**
   * FHIR extensions
   *
   * @param dataAbsentReason The data absent reason extension
   */
  public record Extensions(Extension dataAbsentReason) {
    /** Default constructor. */
    public Extensions {
      dataAbsentReason =
          dataAbsentReason != null ? dataAbsentReason : FhirExtensions.dataAbsentReason();
    }

    @Override
    public Extension dataAbsentReason() {
      return dataAbsentReason.copy();
    }
  }
}

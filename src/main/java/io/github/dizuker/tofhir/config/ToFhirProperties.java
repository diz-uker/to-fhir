package io.github.dizuker.tofhir.config;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for configuring to-FHIR utilities.
 *
 * @param fhir FHIR codings, systems, etc.
 */
@ConfigurationProperties(prefix = "to-fhir")
public record ToFhirProperties(Fhir fhir) {

  /**
   * FHIR properties
   *
   * @param systems FHIR systems
   * @param codings FHIR codings
   * @param extensions FHIR extensions
   */
  public record Fhir(Systems systems, Codings codings, Extensions extensions) {}

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
      String ucum, String loinc, String snomed, String atc, String ops, String icd10gm) {}

  /**
   * FHIR codings
   *
   * @param loinc LOINC Coding
   * @param snomed SNOMED CT Coding
   * @param ops OPS Coding
   * @param atc ATC Coding
   * @param pzn PZN Coding
   */
  public record Codings(Coding loinc, Coding snomed, Coding ops, Coding atc, Coding pzn) {
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
    @Override
    public Extension dataAbsentReason() {
      return dataAbsentReason.copy();
    }
  }
}

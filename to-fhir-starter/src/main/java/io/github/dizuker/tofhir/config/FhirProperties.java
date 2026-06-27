package io.github.dizuker.tofhir.config;

import io.github.dizuker.tofhir.FhirCodings;
import io.github.dizuker.tofhir.FhirExtensions;
import io.github.dizuker.tofhir.FhirSystems;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;

/**
 * Properties for configuring to-FHIR utilities, bound from the {@code fhir} prefix.
 *
 * <p>Every value defaults to the constants in {@code io.github.dizuker.tofhir}. Set {@code fhir.*}
 * properties in the consuming application to override individual defaults.
 *
 * <p>This is a plain mutable class rather than a record so that applications can subclass it to add
 * their own application-specific systems/codings while still binding from the same {@code fhir}
 * prefix. To do so, define a subclass and register it as a {@code @ConfigurationProperties(prefix =
 * "fhir")} bean of your own; {@link ToFhirAutoConfiguration}'s default bean backs off via
 * {@code @ConditionalOnMissingBean}.
 *
 * <p>The nested {@link Systems}, {@link Codings}, and {@link Extensions} types are likewise plain
 * mutable classes (not records) for the same reason: applications can subclass them to add their
 * own entries, e.g.
 *
 * <pre>{@code
 * public class MySystems extends FhirProperties.Systems {
 *   private String myHospitalSystem;
 *
 *   public String myHospitalSystem() { return myHospitalSystem; }
 *   public void setMyHospitalSystem(String myHospitalSystem) {
 *     this.myHospitalSystem = myHospitalSystem;
 *   }
 * }
 *
 * public class MyFhirProperties extends FhirProperties {
 *   private MySystems systems = new MySystems();
 *
 *   @Override
 *   public MySystems systems() { return systems; }
 *
 *   // Overload, not an override: setSystems(Systems) is inherited unchanged. Spring's binder
 *   // resolves the ambiguity by matching the setter whose parameter type equals the getter's
 *   // (overridden) return type.
 *   public void setSystems(MySystems systems) { this.systems = systems; }
 * }
 * }</pre>
 *
 * <p>{@code fhir.systems.my-hospital-system=...} then binds onto {@code
 * myFhirProperties.systems().myHospitalSystem()}, alongside the inherited {@code
 * fhir.systems.loinc} etc.
 */
public class FhirProperties {

  private Systems systems = new Systems();

  private Codings codings = new Codings();

  private Extensions extensions = new Extensions();

  /** Returns the FHIR systems. */
  public Systems systems() {
    return systems;
  }

  /** Used by Spring Boot for property binding. */
  public void setSystems(Systems systems) {
    this.systems = systems;
  }

  /** Returns the FHIR codings. */
  public Codings codings() {
    return codings;
  }

  /** Used by Spring Boot for property binding. */
  public void setCodings(Codings codings) {
    this.codings = codings;
  }

  /** Returns the FHIR extensions. */
  public Extensions extensions() {
    return extensions;
  }

  /** Used by Spring Boot for property binding. */
  public void setExtensions(Extensions extensions) {
    this.extensions = extensions;
  }

  /** FHIR Systems. */
  public static class Systems {
    private String ucum = FhirSystems.UCUM;
    private String loinc = FhirSystems.LOINC;
    private String snomed = FhirSystems.SNOMED;
    private String atc = FhirSystems.ATC;
    private String ops = FhirSystems.OPS;
    private String icd10gm = FhirSystems.ICD10GM;

    /** Returns the units of measure system. */
    public String ucum() {
      return ucum;
    }

    /** Used by Spring Boot for property binding. */
    public void setUcum(String ucum) {
      this.ucum = ucum;
    }

    /** Returns the LOINC system. */
    public String loinc() {
      return loinc;
    }

    /** Used by Spring Boot for property binding. */
    public void setLoinc(String loinc) {
      this.loinc = loinc;
    }

    /** Returns the SNOMED CT system. */
    public String snomed() {
      return snomed;
    }

    /** Used by Spring Boot for property binding. */
    public void setSnomed(String snomed) {
      this.snomed = snomed;
    }

    /** Returns the ATC system. */
    public String atc() {
      return atc;
    }

    /** Used by Spring Boot for property binding. */
    public void setAtc(String atc) {
      this.atc = atc;
    }

    /** Returns the OPS system. */
    public String ops() {
      return ops;
    }

    /** Used by Spring Boot for property binding. */
    public void setOps(String ops) {
      this.ops = ops;
    }

    /** Returns the ICD-10-GM system. */
    public String icd10gm() {
      return icd10gm;
    }

    /** Used by Spring Boot for property binding. */
    public void setIcd10gm(String icd10gm) {
      this.icd10gm = icd10gm;
    }
  }

  /** FHIR codings. */
  public static class Codings {
    private Coding loinc = FhirCodings.loinc();
    private Coding snomed = FhirCodings.snomed();
    private Coding ops = FhirCodings.ops();
    private Coding atc = FhirCodings.atc();
    private Coding icd10gm = FhirCodings.icd10gm();
    private Coding pzn = FhirCodings.pzn();

    /** Returns a fresh copy of the LOINC coding. */
    public Coding loinc() {
      // return a fresh copy, otherwise the original instance will be modified
      return loinc.copy();
    }

    /** Used by Spring Boot for property binding. */
    public void setLoinc(Coding loinc) {
      this.loinc = loinc;
    }

    /** Returns a fresh copy of the SNOMED CT coding. */
    public Coding snomed() {
      return snomed.copy();
    }

    /** Used by Spring Boot for property binding. */
    public void setSnomed(Coding snomed) {
      this.snomed = snomed;
    }

    /** Returns a fresh copy of the OPS coding. */
    public Coding ops() {
      return ops.copy();
    }

    /** Used by Spring Boot for property binding. */
    public void setOps(Coding ops) {
      this.ops = ops;
    }

    /** Returns a fresh copy of the ATC coding. */
    public Coding atc() {
      return atc.copy();
    }

    /** Used by Spring Boot for property binding. */
    public void setAtc(Coding atc) {
      this.atc = atc;
    }

    /** Returns a fresh copy of the ICD-10-GM coding. */
    public Coding icd10gm() {
      return icd10gm.copy();
    }

    /** Used by Spring Boot for property binding. */
    public void setIcd10gm(Coding icd10gm) {
      this.icd10gm = icd10gm;
    }

    /** Returns a fresh copy of the PZN coding. */
    public Coding pzn() {
      return pzn.copy();
    }

    /** Used by Spring Boot for property binding. */
    public void setPzn(Coding pzn) {
      this.pzn = pzn;
    }
  }

  /** FHIR extensions. */
  public static class Extensions {
    private Extension dataAbsentReason = FhirExtensions.dataAbsentReason();

    /** Returns a fresh copy of the data-absent-reason extension. */
    public Extension dataAbsentReason() {
      return dataAbsentReason.copy();
    }

    /** Used by Spring Boot for property binding. */
    public void setDataAbsentReason(Extension dataAbsentReason) {
      this.dataAbsentReason = dataAbsentReason;
    }
  }
}

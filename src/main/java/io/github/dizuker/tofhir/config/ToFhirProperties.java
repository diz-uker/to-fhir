package io.github.dizuker.tofhir.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for to-FHIR utilities. */
@ConfigurationProperties(prefix = "to-fhir")
public class ToFhirProperties {

  /** Enable to-FHIR auto-configuration. Default: true */
  private boolean enabled = true;

  /** FHIR version to use (R4, R5, etc.). Default: R4 */
  private String fhirVersion = "R4";

  /** Default resource validation enabled. Default: true */
  private boolean validateResources = true;

  /** Default strict mode for reference validation. Default: false */
  private boolean strictReferenceValidation = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getFhirVersion() {
    return fhirVersion;
  }

  public void setFhirVersion(String fhirVersion) {
    this.fhirVersion = fhirVersion;
  }

  public boolean isValidateResources() {
    return validateResources;
  }

  public void setValidateResources(boolean validateResources) {
    this.validateResources = validateResources;
  }

  public boolean isStrictReferenceValidation() {
    return strictReferenceValidation;
  }

  public void setStrictReferenceValidation(boolean strictReferenceValidation) {
    this.strictReferenceValidation = strictReferenceValidation;
  }
}

package io.github.dizuker.tofhir.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Boot auto-configuration for to-FHIR utilities.
 *
 * <p>This configuration automatically configures and exposes the FHIR mapping utilities as Spring
 * beans when the starter is included on the classpath.
 */
@AutoConfiguration
@EnableConfigurationProperties(ToFhirProperties.class)
public class ToFhirAutoConfiguration {
  /** Default constructor. */
  public ToFhirAutoConfiguration() {
    super();
  }
}

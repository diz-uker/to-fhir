package io.github.dizuker.tofhir.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for to-FHIR utilities.
 *
 * <p>This configuration automatically configures and exposes the FHIR mapping utilities as Spring
 * beans when the starter is included on the classpath.
 */
@AutoConfiguration
public class ToFhirAutoConfiguration {
  /** Default constructor. */
  public ToFhirAutoConfiguration() {
    super();
  }

  /**
   * Registers the default {@link FhirProperties} bean bound to the {@code fhir} prefix.
   *
   * <p>Applications that need application-specific systems or codings can define their own subclass
   * of {@link FhirProperties}, register it as a {@code @ConfigurationProperties(prefix = "fhir")}
   * bean, and this default will back off in its favor.
   *
   * @return the default {@link FhirProperties}
   */
  @Bean
  @ConditionalOnMissingBean
  @ConfigurationProperties(prefix = "fhir")
  public FhirProperties fhirProperties() {
    return new FhirProperties();
  }
}

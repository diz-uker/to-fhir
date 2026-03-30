package io.github.dizuker.tofhir.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Boot auto-configuration for to-FHIR utilities.
 *
 * <p>This configuration automatically configures and exposes the FHIR mapping utilities as Spring
 * beans when the starter is included on the classpath.
 */
@AutoConfiguration
@EnableConfigurationProperties(ToFhirProperties.class)
@ConditionalOnProperty(
    prefix = "to-fhir",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ToFhirAutoConfiguration {}

package io.github.dizuker.tofhir.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.github.dizuker.tofhir.FhirSystems;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Proves that an application can subclass {@link FhirProperties} (and its nested {@link
 * FhirProperties.Systems}) to add its own application-specific properties while still binding from
 * the shared {@code fhir} prefix, and that the starter's default bean backs off in favor of the
 * application's subclass.
 */
class FhirPropertiesExtensionTest {

  /** An application-specific extension of {@link FhirProperties.Systems}. */
  public static class AcmeSystems extends FhirProperties.Systems {
    private String acmeSystem;

    /** Returns the ACME-specific system. */
    public String acmeSystem() {
      return acmeSystem;
    }

    /** Used by Spring Boot for property binding. */
    public void setAcmeSystem(String acmeSystem) {
      this.acmeSystem = acmeSystem;
    }
  }

  /** An application-specific extension of {@link FhirProperties}. */
  public static class AcmeFhirProperties extends FhirProperties {
    private AcmeSystems systems = new AcmeSystems();

    @Override
    public AcmeSystems systems() {
      return systems;
    }

    // Overload, not an override: setSystems(Systems) is inherited unchanged. Spring's binder
    // resolves the ambiguity by matching the setter whose parameter type equals the getter's
    // (overridden) return type.
    /** Used by Spring Boot for property binding. */
    public void setSystems(AcmeSystems systems) {
      this.systems = systems;
    }
  }

  @Configuration
  static class AcmeConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "fhir")
    AcmeFhirProperties fhirProperties() {
      return new AcmeFhirProperties();
    }
  }

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(AcmeConfiguration.class)
          .withConfiguration(
              AutoConfigurations.of(
                  ConfigurationPropertiesAutoConfiguration.class, ToFhirAutoConfiguration.class));

  @Test
  void applicationSubclassWinsOverDefaultBean() {
    contextRunner
        .withPropertyValues(
            "fhir.systems.loinc=https://example.com/loinc",
            "fhir.systems.acme-system=https://acme.example.com/codes")
        .run(
            context -> {
              var props = context.getBean(FhirProperties.class);
              assertInstanceOf(AcmeFhirProperties.class, props);

              var systems = props.systems();
              assertInstanceOf(AcmeSystems.class, systems);
              assertEquals("https://example.com/loinc", systems.loinc());

              var acmeSystems = (AcmeSystems) systems;
              assertEquals("https://acme.example.com/codes", acmeSystems.acmeSystem());
              // untouched inherited default is preserved
              assertEquals(FhirSystems.SNOMED, acmeSystems.snomed());
            });
  }
}

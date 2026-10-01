package io.github.dizuker.tofhir.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.dizuker.tofhir.FhirCodings;
import io.github.dizuker.tofhir.FhirSystems;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ToFhirPropertiesTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              org.springframework.boot.autoconfigure.AutoConfigurations.of(
                  ConfigurationPropertiesAutoConfiguration.class, ToFhirAutoConfiguration.class));

  @Test
  void testDefaults() {
    contextRunner.run(
        context -> {
          var props = context.getBean(ToFhirProperties.class);
          assertEquals(FhirSystems.LOINC, props.systems().loinc());
          assertEquals(FhirSystems.LOINC, props.codings().loinc().getSystem());
        });
  }

  @Test
  void testOverride() {
    contextRunner
        .withPropertyValues("fhir.systems.loinc=https://example.com/loinc")
        .run(
            context -> {
              var props = context.getBean(ToFhirProperties.class);
              assertEquals("https://example.com/loinc", props.systems().loinc());
            });
  }

  @Test
  void testCodingVersionOverrideKeepsDefaultSystem() {
    contextRunner
        .withPropertyValues(
            "fhir.codings.snomed.version=http://snomed.info/sct/11000274103/version/20260515")
        .run(
            context -> {
              var props = context.getBean(ToFhirProperties.class);
              var snomed = props.codings().snomed();
              assertEquals(FhirSystems.SNOMED, snomed.getSystem());
              assertEquals(
                  "http://snomed.info/sct/11000274103/version/20260515", snomed.getVersion());
              // codings that weren't overridden keep their defaults
              assertEquals(FhirCodings.loinc().getVersion(), props.codings().loinc().getVersion());
            });
  }
}

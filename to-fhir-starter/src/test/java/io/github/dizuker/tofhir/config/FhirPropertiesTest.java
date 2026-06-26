package io.github.dizuker.tofhir.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.dizuker.tofhir.FhirSystems;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class FhirPropertiesTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              org.springframework.boot.autoconfigure.AutoConfigurations.of(
                  ToFhirAutoConfiguration.class));

  @Test
  void testDefaults() {
    contextRunner.run(
        context -> {
          var props = context.getBean(FhirProperties.class);
          assertEquals(FhirSystems.LOINC, props.systems().loinc());
          assertEquals(
              "http://hl7.org/fhir/StructureDefinition/data-absent-reason",
              props.extensions().dataAbsentReason().getUrl());
          assertEquals(FhirSystems.LOINC, props.codings().loinc().getSystem());
        });
  }

  @Test
  void testOverride() {
    contextRunner
        .withPropertyValues("fhir.systems.loinc=https://example.com/loinc")
        .run(
            context -> {
              var props = context.getBean(FhirProperties.class);
              assertEquals("https://example.com/loinc", props.systems().loinc());
            });
  }
}

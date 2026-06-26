package io.github.dizuker.tofhir.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.dizuker.tofhir.FhirSystems;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ToFhirPropertiesTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              org.springframework.boot.autoconfigure.AutoConfigurations.of(
                  ToFhirAutoConfiguration.class));

  @Test
  void testDefaults() {
    contextRunner.run(
        context -> {
          var props = context.getBean(ToFhirProperties.class);
          assertEquals(FhirSystems.LOINC, props.fhir().systems().loinc());
          assertEquals(
              "http://hl7.org/fhir/StructureDefinition/data-absent-reason",
              props.fhir().extensions().dataAbsentReason().getUrl());
          assertEquals(FhirSystems.LOINC, props.fhir().codings().loinc().getSystem());
        });
  }

  @Test
  void testOverride() {
    contextRunner
        .withPropertyValues("to-fhir.fhir.systems.loinc=https://example.com/loinc")
        .run(
            context -> {
              var props = context.getBean(ToFhirProperties.class);
              assertEquals("https://example.com/loinc", props.fhir().systems().loinc());
            });
  }
}

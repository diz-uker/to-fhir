package io.github.dizuker.tofhir.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ToFhirPropertiesTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              org.springframework.boot.autoconfigure.AutoConfigurations.of(
                  ToFhirAutoConfiguration.class))
          .withPropertyValues(
              "to-fhir.fhir.extensions.data-absent-reason.url=http://hl7.org/fhir/StructureDefinition/data-absent-reason");

  @Test
  void testFhir() {
    contextRunner.run(
        context -> {
          var props = context.getBean(ToFhirProperties.class);
          assertEquals(
              "http://hl7.org/fhir/StructureDefinition/data-absent-reason",
              props.fhir().extensions().dataAbsentReason().getUrl());
        });
  }
}

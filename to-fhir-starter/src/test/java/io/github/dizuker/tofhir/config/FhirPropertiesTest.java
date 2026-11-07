package io.github.dizuker.tofhir.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.dizuker.tofhir.FhirCodings;
import io.github.dizuker.tofhir.FhirSystems;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class FhirPropertiesTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              org.springframework.boot.autoconfigure.AutoConfigurations.of(
                  ConfigurationPropertiesAutoConfiguration.class, ToFhirAutoConfiguration.class));

  @Test
  void testDefaults() {
    contextRunner.run(
        context -> {
          var props = context.getBean(FhirProperties.class);
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
              var props = context.getBean(FhirProperties.class);
              assertEquals("https://example.com/loinc", props.systems().loinc());
            });
  }

  @Test
  void testCodingVersionOverrideKeepsDefaultSystem() {
    contextRunner
        .withPropertyValues(
            "fhir.codings.loinc.version=test",
            "fhir.codings.snomed.version=test",
            "fhir.codings.ops.version=test",
            "fhir.codings.atc.version=test",
            "fhir.codings.icd10gm.version=test",
            "fhir.codings.pzn.version=test")
        .run(
            context -> {
              var codings = context.getBean(FhirProperties.class).codings();
              assertSystemAndTestVersion(FhirSystems.LOINC, codings.loinc());
              assertSystemAndTestVersion(FhirSystems.SNOMED, codings.snomed());
              assertSystemAndTestVersion(FhirSystems.OPS, codings.ops());
              assertSystemAndTestVersion(FhirSystems.ATC, codings.atc());
              assertSystemAndTestVersion(FhirSystems.ICD10GM, codings.icd10gm());
              assertSystemAndTestVersion(FhirSystems.PZN, codings.pzn());
            });
  }

  @Test
  void testCodingSystemOverrideKeepsDefaultVersion() {
    contextRunner
        .withPropertyValues("fhir.codings.snomed.system=https://example.com/snomed")
        .run(
            context -> {
              var snomed = context.getBean(FhirProperties.class).codings().snomed();
              assertEquals("https://example.com/snomed", snomed.getSystem());
              assertEquals(FhirCodings.snomed().getVersion(), snomed.getVersion());
            });
  }

  private static void assertSystemAndTestVersion(String expectedSystem, Coding coding) {
    assertEquals(expectedSystem, coding.getSystem());
    assertEquals("test", coding.getVersion());
  }
}

package io.github.dizuker.tofhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.core.Scrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.approvaltests.scrubbers.Scrubbers;
import org.approvaltests.writers.ApprovalTextWriter;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionBuilderTest {
  private static final FhirContext fhirContext = FhirContext.forR4();
  public static final Scrubber FHIR_DATE_TIME_SCRUBBER =
      Scrubbers.scrubAll(
          new RegExScrubber(
              "\"occurredDateTime\": \"(.*)\"", "\"occurredDateTime\": \"2000-01-01T11:11:11Z\""),
          new RegExScrubber("\"recorded\": \"(.*)\"", "\"recorded\": \"2000-01-01T11:11:11Z\""));

  @Test
  void testBuildWithSingleEntry() {
    var fhirParser = fhirContext.newJsonParser().setPrettyPrint(true);
    final var sut = new TransactionBuilder();

    var patient = new Patient();
    patient.setId("test-patient");
    patient.addExtension("test", new CodeType("test"));

    var trx = sut.addEntry(patient).build();

    Approvals.verify(
        new ApprovalTextWriter(
            fhirParser.encodeResourceToString(trx),
            new Options().forFile().withExtension(".fhir.json")));
  }

  @Test
  void testBuildWithMultipleEntries() {
    var fhirParser = fhirContext.newJsonParser().setPrettyPrint(true);
    final var sut = new TransactionBuilder();

    var patient = new Patient();
    patient.setId("test-patient");
    patient.addExtension("test", new CodeType("test"));

    var observation = new Observation();
    observation.setId("test-observation");
    observation.setStatus(ObservationStatus.FINAL);

    var trx = sut.withId("test-patient").addEntries(List.of(patient, observation)).build();

    Approvals.verify(
        new ApprovalTextWriter(
            fhirParser.encodeResourceToString(trx),
            new Options().forFile().withExtension(".fhir.json")));
  }

  @Test
  void testBuildWithMultipleEntriesAndProvenance() {
    var fhirParser = fhirContext.newJsonParser().setPrettyPrint(true);
    final var sut = new TransactionBuilder();

    var patient = new Patient();
    patient.setId("test-patient");
    patient.addExtension("test", new CodeType("test"));

    var observation = new Observation();
    observation.setId("test-observation");
    observation.setStatus(ObservationStatus.FINAL);

    var toDelete = new Reference("Observation/test-observation-to-delete");
    var toDelete2 = new Reference("Observation/test-observation-to-delte-as-well");

    var trx =
        sut.withId("test-patient")
            .withProvenance(
                new Reference("Device/the-etl-job").setDisplay("The test etl job in version 1.2.3"),
                new Reference().setDisplay("The source system"))
            .addEntries(List.of(patient, observation))
            .addDeleteEntries(List.of(toDelete, toDelete2))
            .build();

    Approvals.verify(
        new ApprovalTextWriter(
            fhirParser.encodeResourceToString(trx),
            new Options(FHIR_DATE_TIME_SCRUBBER).forFile().withExtension(".fhir.json")));
  }

  @Test
  void testDefaultBundleType() {
    var bundle = new TransactionBuilder().build();

    assertEquals(BundleType.TRANSACTION, bundle.getType());
  }

  @ParameterizedTest
  @EnumSource(BundleType.class)
  void testWithDifferentBundleTypes(BundleType bundleType) {
    var bundle = new TransactionBuilder().withType(bundleType).build();

    assertEquals(bundleType, bundle.getType());
  }

  @Test
  void testAddSingleEntry() {
    var patient = new Patient();
    patient.setId("test-patient");

    var bundle = new TransactionBuilder().addEntry(patient).build();

    assertEquals(1, bundle.getEntry().size());
    assertEquals(patient, bundle.getEntry().get(0).getResource());
  }

  @Test
  void testAddMultipleEntriesIndividually() {
    var patient1 = new Patient();
    patient1.setId("patient-1");

    var patient2 = new Patient();
    patient2.setId("patient-2");

    var bundle = new TransactionBuilder().addEntry(patient1).addEntry(patient2).build();

    assertEquals(2, bundle.getEntry().size());
    assertEquals(patient1, bundle.getEntry().get(0).getResource());
    assertEquals(patient2, bundle.getEntry().get(1).getResource());
  }

  @Test
  void testChainedConfiguration() {
    var patient = new Patient();
    patient.setId("test-patient");

    var bundle =
        new TransactionBuilder()
            .withType(BundleType.BATCH)
            .withId(patient.getId())
            .addEntry(patient)
            .build();

    assertEquals(BundleType.BATCH, bundle.getType());
    assertEquals(1, bundle.getEntry().size());
    assertEquals(HTTPVerb.PUT, bundle.getEntry().get(0).getRequest().getMethod());
    assertEquals("test-patient", bundle.getIdElement().getIdPart());
  }

  @Test
  void testWithIdString() {
    var bundle = new TransactionBuilder().withId("bundle-123").build();

    assertEquals("bundle-123", bundle.getIdElement().getIdPart());
  }

  @Test
  void testWithIdIIdType() {
    var idType = new IdType("bundle-456");
    var bundle = new TransactionBuilder().withId(idType).build();

    assertEquals("bundle-456", bundle.getIdElement().getIdPart());
  }

  @ParameterizedTest
  @ValueSource(strings = {"bundle-1", "123", "my-bundle-id"})
  void testWithIdVariousValues(String idValue) {
    var bundle = new TransactionBuilder().withId(idValue).build();

    assertEquals(idValue, bundle.getIdElement().getIdPart());
  }

  @Test
  void testThrowExceptionOnDuplicateResourceIds() {
    var patient1 = new Patient();
    patient1.setId("patient-123");

    var patient2 = new Patient();
    patient2.setId("patient-123");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new TransactionBuilder()
                .failOnDuplicateEntries()
                .addEntry(patient1)
                .addEntry(patient2)
                .build(),
        "Should throw exception for duplicate resource IDs");
  }

  @Test
  void testNoDuplicateExceptionWhenFlagNotEnabled() {
    var patient1 = new Patient();
    patient1.setId("patient-123");

    var patient2 = new Patient();
    patient2.setId("patient-123");

    var bundle = new TransactionBuilder().addEntry(patient1).addEntry(patient2).build();

    assertEquals(2, bundle.getEntry().size());
  }

  @Test
  void testDifferentResourceTypesWithSameIdAllowed() {
    var patient = new Patient();
    patient.setId("resource-123");

    var observation = new Observation();
    observation.setId("resource-123");
    observation.setStatus(ObservationStatus.FINAL);

    var bundle =
        new TransactionBuilder()
            .failOnDuplicateEntries()
            .addEntry(patient)
            .addEntry(observation)
            .build();

    assertEquals(2, bundle.getEntry().size());
  }

  @Test
  void testThrowExceptionOnDuplicateResourceIdsMultipleDuplicates() {
    var patient1 = new Patient();
    patient1.setId("patient-1");

    var patient2 = new Patient();
    patient2.setId("patient-1");

    var patient3 = new Patient();
    patient3.setId("patient-2");

    var patient4 = new Patient();
    patient4.setId("patient-2");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new TransactionBuilder()
                .failOnDuplicateEntries()
                .addEntry(patient1)
                .addEntry(patient2)
                .addEntry(patient3)
                .addEntry(patient4)
                .build());
  }
}

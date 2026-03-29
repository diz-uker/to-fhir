package io.github.dizuker.tofhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.writers.ApprovalTextWriter;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

public class BundleBuilderTest {
  private static final FhirContext fhirContext = FhirContext.forR4();

  @Test
  void testBuildWithSingleEntry() {
    var fhirParser = fhirContext.newJsonParser().setPrettyPrint(true);
    final var sut = new BundleBuilder();

    var patient = new Patient();
    patient.setId("test-patient");
    patient.addExtension("test", new CodeType("test"));

    var trx = sut.withUpdateAsCreate(true).addEntry(patient).build();

    Approvals.verify(
        new ApprovalTextWriter(
            fhirParser.encodeResourceToString(trx),
            new Options().forFile().withExtension(".json")));
  }

  @Test
  void testBuildWithMultipleEntries() {
    var fhirParser = fhirContext.newJsonParser().setPrettyPrint(true);
    final var sut = new BundleBuilder();

    var patient = new Patient();
    patient.setId("test-patient");
    patient.addExtension("test", new CodeType("test"));

    var observation = new Observation();
    observation.setId("test-observation");
    observation.setStatus(ObservationStatus.FINAL);

    var trx =
        sut.withUpdateAsCreate(true)
            .withUseFirstEntryResourceIdAsBundleId(true)
            .addEntries(List.of(patient, observation))
            .build();

    Approvals.verify(
        new ApprovalTextWriter(
            fhirParser.encodeResourceToString(trx),
            new Options().forFile().withExtension(".json")));
  }

  @Test
  void testDefaultBundleType() {
    var bundle = new BundleBuilder().build();

    assertEquals(BundleType.TRANSACTION, bundle.getType());
  }

  @ParameterizedTest
  @EnumSource(BundleType.class)
  void testWithDifferentBundleTypes(BundleType bundleType) {
    var bundle = new BundleBuilder().withType(bundleType).build();

    assertEquals(bundleType, bundle.getType());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testWithUpdateAsCreate(boolean updateAsCreate) {
    var patient = new Patient();
    patient.setId("test-patient");

    var bundle = new BundleBuilder().withUpdateAsCreate(updateAsCreate).addEntry(patient).build();

    assertNotNull(bundle.getEntry());
    assertEquals(1, bundle.getEntry().size());

    var entry = bundle.getEntry().get(0);
    var expectedMethod = updateAsCreate ? HTTPVerb.PUT : HTTPVerb.POST;
    assertEquals(expectedMethod, entry.getRequest().getMethod());

    var expectedUrl = updateAsCreate ? "Patient/test-patient" : "Patient";
    assertEquals(expectedUrl, entry.getRequest().getUrl());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void testWithUseFirstEntryResourceIdAsBundleId(boolean useFirstEntryId) {
    var patient = new Patient();
    patient.setId("patient-123");

    var bundle =
        new BundleBuilder()
            .withUseFirstEntryResourceIdAsBundleId(useFirstEntryId)
            .addEntry(patient)
            .build();

    if (useFirstEntryId) {
      assertEquals("patient-123", bundle.getIdElement().getIdPart());
    } else {
      assertEquals(null, bundle.getIdElement().getIdPart());
    }
  }

  @Test
  void testAddSingleEntry() {
    var patient = new Patient();
    patient.setId("test-patient");

    var bundle = new BundleBuilder().addEntry(patient).build();

    assertEquals(1, bundle.getEntry().size());
    assertEquals(patient, bundle.getEntry().get(0).getResource());
  }

  @Test
  void testAddMultipleEntriesIndividually() {
    var patient1 = new Patient();
    patient1.setId("patient-1");

    var patient2 = new Patient();
    patient2.setId("patient-2");

    var bundle = new BundleBuilder().addEntry(patient1).addEntry(patient2).build();

    assertEquals(2, bundle.getEntry().size());
    assertEquals(patient1, bundle.getEntry().get(0).getResource());
    assertEquals(patient2, bundle.getEntry().get(1).getResource());
  }

  @Test
  void testChainedConfiguration() {
    var patient = new Patient();
    patient.setId("test-patient");

    var bundle =
        new BundleBuilder()
            .withType(BundleType.BATCH)
            .withUpdateAsCreate(false)
            .withUseFirstEntryResourceIdAsBundleId(true)
            .addEntry(patient)
            .build();

    assertEquals(BundleType.BATCH, bundle.getType());
    assertEquals(1, bundle.getEntry().size());
    assertEquals(HTTPVerb.POST, bundle.getEntry().get(0).getRequest().getMethod());
    assertEquals("test-patient", bundle.getIdElement().getIdPart());
  }
}

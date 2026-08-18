package io.github.dizuker.tofhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

public class ReferenceUtilsTest {
  @Test
  void testCreateReferenceToPatient() {
    var patient = new Patient();
    patient.setId("patient-123");

    var reference = ReferenceUtils.createReferenceTo(patient);

    assertEquals("Patient/patient-123", reference.getReference());
  }

  @Test
  void testCreateReferenceToObservation() {
    var observation = new Observation();
    observation.setId("obs-456");

    var reference = ReferenceUtils.createReferenceTo(observation);

    assertEquals("Observation/obs-456", reference.getReference());
  }

  @Test
  void testCreateReferenceWithoutIdThrows() {
    var patient = new Patient();
    // No ID set

    assertThrows(Exception.class, () -> ReferenceUtils.createReferenceTo(patient));
  }

  @Test
  void testCreateReferenceWithBlankIdThrows() {
    var patient = new Patient();
    patient.setId("");

    assertThrows(Exception.class, () -> ReferenceUtils.createReferenceTo(patient));
  }

  @Test
  void testCreateReferenceToIdentifier() {
    var identifier = new Identifier().setSystem("http://example.com/patient").setValue("12345");
    var reference = ReferenceUtils.createReferenceTo(identifier, ResourceType.Patient);
    assertEquals(
        "Patient/ab0ecec0f0b7f2e7d0034eb57fceee58120d2ecb95d4e05c2613143ae439f652",
        reference.getReference());
  }
}

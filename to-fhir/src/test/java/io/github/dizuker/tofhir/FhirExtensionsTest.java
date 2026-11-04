package io.github.dizuker.tofhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.dizuker.tofhir.FhirExtensions.DataAbsentReason;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class FhirExtensionsTest {
  @Test
  void testDataAbsentReasonNotAsked() {
    var extension = DataAbsentReason.notAsked();

    assertEquals(FhirExtensions.DATA_ABSENT_REASON_URL, extension.getUrl());
    assertEquals("not-asked", ((CodeType) extension.getValue()).getCode());
  }

  @Test
  void testDataAbsentReasonReturnsFreshInstance() {
    var extension1 = DataAbsentReason.notAsked();
    var extension2 = DataAbsentReason.notAsked();

    assertNotSame(extension1, extension2);
  }

  @Test
  void testDataAbsentReasonTemplateHasNoValue() {
    var extension = FhirExtensions.dataAbsentReason();

    assertEquals(FhirExtensions.DATA_ABSENT_REASON_URL, extension.getUrl());
    assertFalse(extension.hasValue());
  }

  @ParameterizedTest
  @EnumSource(
      value = Enumerations.DataAbsentReason.class,
      names = "NULL",
      mode = EnumSource.Mode.EXCLUDE)
  void testDataAbsentReasonMapsEveryCode(Enumerations.DataAbsentReason reason) {
    var extension = FhirExtensions.dataAbsentReason(reason);

    assertEquals(reason.toCode(), ((CodeType) extension.getValue()).getCode());
  }

  @Test
  void testDataAbsentReasonRejectsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> FhirExtensions.dataAbsentReason(Enumerations.DataAbsentReason.NULL));
  }

  @Test
  void testDataAbsentReasonCodeableConcept() {
    var concept = DataAbsentReason.codeableConcept(Enumerations.DataAbsentReason.NOTPERFORMED);

    assertEquals(1, concept.getCoding().size());
    assertEquals(
        "http://terminology.hl7.org/CodeSystem/data-absent-reason",
        concept.getCodingFirstRep().getSystem());
    assertEquals("not-performed", concept.getCodingFirstRep().getCode());
    assertEquals("Not Performed", concept.getCodingFirstRep().getDisplay());
  }

  @Test
  void testDataAbsentReasonCodeableConceptRejectsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> DataAbsentReason.codeableConcept(Enumerations.DataAbsentReason.NULL));
  }
}

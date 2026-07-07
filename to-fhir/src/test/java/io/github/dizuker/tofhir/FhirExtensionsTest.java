package io.github.dizuker.tofhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import io.github.dizuker.tofhir.FhirExtensions.DataAbsentReason;
import org.hl7.fhir.r4.model.CodeType;
import org.junit.jupiter.api.Test;

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
}

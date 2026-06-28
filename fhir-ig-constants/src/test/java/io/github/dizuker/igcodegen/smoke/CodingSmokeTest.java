package io.github.dizuker.igcodegen.smoke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.medizininformatikinitiative.kerndatensatz.onkologie.Onkologie;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;

class CodingSmokeTest {

  @Test
  void codingAccessorReturnsExpectedSystemCodeAndDisplay() {
    Coding coding = Onkologie.CodeSystems.MiiCsOnkoIntention.K.coding();
    assertEquals(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/CodeSystem/mii-cs-onko-intention",
        coding.getSystem());
    assertEquals("K", coding.getCode());
    assertEquals("kurativ", coding.getDisplay());
  }

  @Test
  void disambiguatedConstantsRoundTripTheirOriginalCode() {
    Coding pos = Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_POS.coding();
    Coding neg = Onkologie.CodeSystems.MiiCsOnkoTnmUicc.I_NEG.coding();
    assertEquals("i+", pos.getCode());
    assertEquals("i-", neg.getCode());
  }
}

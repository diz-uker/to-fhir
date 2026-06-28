package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameUtilsTest {

  @ParameterizedTest
  @CsvSource({
    "mii-cs-onko-intention, MII_CS_ONKO_INTENTION",
    "mii-pr-onko-operation, MII_PR_ONKO_OPERATION",
    "PatientPseudonymisiert, PATIENT_PSEUDONYMISIERT",
    "mii-ex-onko-zahnstatus, MII_EX_ONKO_ZAHNSTATUS",
    "Vitalstatus, VITALSTATUS",
    "a, A",
  })
  void toConstantName(String id, String expected) {
    assertEquals(expected, NameUtils.toConstantName(id));
  }

  @ParameterizedTest
  @CsvSource({
    "onkologie, Onkologie",
    "base, Base",
    "kerndatensatz, Kerndatensatz",
    "r4, R4",
  })
  void toPascalCase(String segment, String expected) {
    assertEquals(expected, NameUtils.toPascalCase(segment));
  }

  @ParameterizedTest
  @CsvSource({
    "MII_PR_DIAGNOSE_CONDITION, miiPrDiagnoseCondition",
    "MII_CS_ONKO_INTENTION, miiCsOnkoIntention",
    "PATIENT_PSEUDONYMISIERT, patientPseudonymisiert",
    "A, a",
  })
  void toCamelCase(String constantName, String expected) {
    assertEquals(expected, NameUtils.toCamelCase(constantName));
  }

  @ParameterizedTest
  @CsvSource({
    "K, K",
    "T1a1, T1A1",
    "'Tis(LAMN)', TIS_LAMN_",
    "'2', _2",
    "'10', _10",
    "'mol+', MOL_POS",
    "'i-', I_NEG",
  })
  void toEnumConstantName(String code, String expected) {
    assertEquals(expected, NameUtils.toEnumConstantName(code));
  }
}

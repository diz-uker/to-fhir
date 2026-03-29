package io.github.dizuker.tofhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

public class IdUtilsTest {
  @Test
  void testComputeIdFromIdentifier() {
    var identifier = new Identifier();
    identifier.setSystem("http://example.com/patient");
    identifier.setValue("12345");

    var id = IdUtils.fromIdentifier(identifier).getIdPart();

    assertEquals("ab0ecec0f0b7f2e7d0034eb57fceee58120d2ecb95d4e05c2613143ae439f652", id);
  }

  @Test
  void testComputeIdFromIdentifierIsConsistent() {
    var identifier = new Identifier();
    identifier.setSystem("http://example.com/patient");
    identifier.setValue("12345");

    var id1 = IdUtils.fromIdentifier(identifier).getIdPart();
    var id2 = IdUtils.fromIdentifier(identifier).getIdPart();

    assertEquals(id1, id2);
  }

  @Test
  void testComputeIdFromIdentifierDifferentValues() {
    var identifier1 = new Identifier();
    identifier1.setSystem("http://example.com/patient");
    identifier1.setValue("12345");

    var identifier2 = new Identifier();
    identifier2.setSystem("http://example.com/patient");
    identifier2.setValue("54321");

    var id1 = IdUtils.fromIdentifier(identifier1).getIdPart();
    var id2 = IdUtils.fromIdentifier(identifier2).getIdPart();

    assertNotEquals(id1, id2);
  }

  @Test
  void testComputeIdFromIdentifierDifferentSystems() {
    var identifier1 = new Identifier();
    identifier1.setSystem("http://example.com/patient");
    identifier1.setValue("12345");

    var identifier2 = new Identifier();
    identifier2.setSystem("http://other.com/patient");
    identifier2.setValue("12345");

    var id1 = IdUtils.fromIdentifier(identifier1).getIdPart();
    var id2 = IdUtils.fromIdentifier(identifier2).getIdPart();

    assertNotEquals(id1, id2);
  }

  @Test
  void testComputeIdFromIdentifierWithBlankSystemThrows() {
    var identifier = new Identifier();
    identifier.setSystem("");
    identifier.setValue("12345");

    assertThrows(Exception.class, () -> IdUtils.fromIdentifier(identifier));
  }

  @Test
  void testComputeIdFromIdentifierWithNullSystemThrows() {
    var identifier = new Identifier();
    // System not set (null)
    identifier.setValue("12345");

    assertThrows(Exception.class, () -> IdUtils.fromIdentifier(identifier));
  }

  @Test
  void testComputeIdFromIdentifierWithBlankValueThrows() {
    var identifier = new Identifier();
    identifier.setSystem("http://example.com/patient");
    identifier.setValue("");

    assertThrows(Exception.class, () -> IdUtils.fromIdentifier(identifier));
  }

  @Test
  void testComputeIdFromIdentifierWithNullValueThrows() {
    var identifier = new Identifier();
    identifier.setSystem("http://example.com/patient");
    // Value not set (null)

    assertThrows(Exception.class, () -> IdUtils.fromIdentifier(identifier));
  }

  private void assertNotEquals(String id1, String id2) {
    if (id1.equals(id2)) {
      throw new AssertionError("Values should not be equal: " + id1);
    }
  }
}

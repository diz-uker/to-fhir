package io.github.dizuker.tofhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class Mod1110ChecksumTest {
  @Test
  void testComputeWithNonNumericValueThrows() {
    assertThrows(IllegalArgumentException.class, () -> Mod1110Checksum.compute("123a"));
  }

  // Source:
  // https://github.com/arthurdejong/python-stdnum/blob/master/stdnum/iso7064/mod_11_10.py
  @Test
  void testComputeForKnownValue() {
    assertEquals(3, Mod1110Checksum.compute("79462"));
  }

  @Test
  void testAddForKnownValue() {
    assertEquals("794623", Mod1110Checksum.add("79462"));
  }
}

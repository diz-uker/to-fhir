package io.github.dizuker.tofhir;

/** Utility class for computing and appending MOD 11/10 check digits. */
public final class Mod1110Checksum {
  private Mod1110Checksum() {
    // Utility class, prevent instantiation
  }

  /**
   * Computes the MOD 11/10 check digit for a numeric value.
   *
   * @param value value containing only decimal digits
   * @return calculated check digit
   * @throws IllegalArgumentException if the value is null or does not contain only digits
   */
  public static int compute(String value) {
    if (value == null || !value.matches("[0-9]+")) {
      throw new IllegalArgumentException("Value must contain only digits: " + value);
    }
    int product = 10;
    for (int i = 0; i < value.length(); i++) {
      int digit = value.charAt(i) - '0';
      int valueSum = (digit + product) % 10;
      if (valueSum == 0) {
        valueSum = 10;
      }
      product = (valueSum * 2) % 11;
    }
    int checksum = 11 - product;
    if (checksum == 10) {
      checksum = 0;
    }
    return checksum;
  }

  /**
   * Appends the MOD 11/10 check digit to a numeric value.
   *
   * @param value value containing only decimal digits
   * @return the original value appended by its check digit
   */
  public static String add(String value) {
    return value + compute(value);
  }
}

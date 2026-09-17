package io.github.dizuker.tofhir;

/**
 * Utility class for computing and appending MOD 11/10 check digits.
 */
public final class Mod1110Checksum {
    private Mod1110Checksum() {
        // Utility class, prevent instantiation
    }

    /**
     * Computes the MOD 11/10 check digit for a nine-digit numeric value.
     *
     * @param value the nine-digit value
     * @return calculated check digit
     * @throws IllegalArgumentException if the value is null or does not contain exactly 9 digits
     */
    public static int compute(String value) {
        if (value == null || !value.matches("\\d{9}")) {
            throw new IllegalArgumentException("Invalid 9-digit: " + value);
        }
        int product = 10;
        String s = "0" + value;
        for (int i = 0; i < s.length(); i++) {
            int digit = s.charAt(i) - '0';
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
     * Appends the MOD 11/10 check digit to a nine-digit numeric value.
     *
     * @param value the nine-digit value
     * @return the original value appended by its check digit
     */
    public static String add(String value) {
        return value + compute(value);
    }
}

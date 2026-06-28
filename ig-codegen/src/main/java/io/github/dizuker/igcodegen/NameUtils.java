package io.github.dizuker.igcodegen;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Converts FHIR resource ids (kebab-case or, occasionally, PascalCase) into Java constant names.
 */
public final class NameUtils {

  private static final Pattern LOWER_OR_DIGIT_FOLLOWED_BY_UPPER =
      Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");
  private static final Pattern NON_ALPHANUMERIC_RUN = Pattern.compile("[^A-Za-z0-9]+");
  private static final Pattern UNDERSCORE = Pattern.compile("_");

  private NameUtils() {}

  public static String toConstantName(String id) {
    String withWordBoundaries = LOWER_OR_DIGIT_FOLLOWED_BY_UPPER.matcher(id).replaceAll("_");
    return NON_ALPHANUMERIC_RUN
        .matcher(withWordBoundaries)
        .replaceAll("_")
        .toUpperCase(Locale.ROOT);
  }

  /** Converts a dot-separated FHIR package name segment into a PascalCase Java class name. */
  public static String toPascalCase(String segment) {
    String withWordBoundaries = LOWER_OR_DIGIT_FOLLOWED_BY_UPPER.matcher(segment).replaceAll("_");
    StringBuilder result = new StringBuilder();
    for (String word : NON_ALPHANUMERIC_RUN.split(withWordBoundaries, -1)) {
      if (word.isEmpty()) {
        continue;
      }
      result.append(Character.toUpperCase(word.charAt(0)));
      if (word.length() > 1) {
        result.append(word.substring(1).toLowerCase(Locale.ROOT));
      }
    }
    return result.toString();
  }

  /**
   * Converts a {@code SCREAMING_SNAKE_CASE} constant name (as produced by {@link #toConstantName})
   * into a lowerCamelCase Java identifier, e.g. {@code MII_PR_DIAGNOSE_CONDITION ->
   * miiPrDiagnoseCondition}. Short FHIR-type abbreviations (pr, cs, ex, ...) are not treated
   * specially - only their first letter is capitalized, matching standard Java method-naming style.
   */
  public static String toCamelCase(String constantName) {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (String word : UNDERSCORE.split(constantName, -1)) {
      if (word.isEmpty()) {
        continue;
      }
      String lowerWord = word.toLowerCase(Locale.ROOT);
      if (first) {
        result.append(lowerWord);
        first = false;
      } else {
        result.append(Character.toUpperCase(lowerWord.charAt(0)));
        result.append(lowerWord.substring(1));
      }
    }
    return result.toString();
  }
}

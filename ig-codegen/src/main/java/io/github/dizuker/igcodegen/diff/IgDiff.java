package io.github.dizuker.igcodegen.diff;

import io.github.dizuker.igcodegen.IgPackageModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Diffs two versions of an {@link IgPackageModel}, reporting added, removed and (heuristically)
 * renamed canonical URLs per constant category.
 *
 * <p>This is the actual value proposition of ig-codegen beyond saving typing: comparing two IG
 * package versions surfaces real upstream renames and silent removals that are otherwise easy to
 * miss in a hand-maintained config file.
 */
public final class IgDiff {

  /**
   * Minimum length of the shorter normalized id for an add/remove pair to be considered for the
   * rename heuristic, to avoid trivially short ids matching everything.
   */
  private static final int MIN_OVERLAP_LENGTH = 6;

  private IgDiff() {}

  public static List<CategoryDiff> diff(IgPackageModel oldModel, IgPackageModel newModel) {
    return List.of(
        diffCategory("CodeSystems", oldModel.codeSystems(), newModel.codeSystems()),
        diffCategory("Profiles", oldModel.profiles(), newModel.profiles()),
        diffCategory("Extensions", oldModel.extensions(), newModel.extensions()));
  }

  private static CategoryDiff diffCategory(
      String categoryName, Map<String, String> oldEntries, Map<String, String> newEntries) {
    Map<String, CategoryDiff.Entry> stillRemoved = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : oldEntries.entrySet()) {
      if (!newEntries.containsKey(entry.getKey())) {
        stillRemoved.put(entry.getKey(), new CategoryDiff.Entry(entry.getKey(), entry.getValue()));
      }
    }
    Map<String, CategoryDiff.Entry> stillAdded = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : newEntries.entrySet()) {
      if (!oldEntries.containsKey(entry.getKey())) {
        stillAdded.put(entry.getKey(), new CategoryDiff.Entry(entry.getKey(), entry.getValue()));
      }
    }

    List<CategoryDiff.Rename> renamed = new ArrayList<>();
    for (CategoryDiff.Entry removedEntry : List.copyOf(stillRemoved.values())) {
      CategoryDiff.Entry bestMatch = null;
      int bestOverlapLength = MIN_OVERLAP_LENGTH - 1;
      for (CategoryDiff.Entry addedEntry : stillAdded.values()) {
        int overlapLength =
            nameOverlapLength(removedEntry.constantName(), addedEntry.constantName());
        if (overlapLength > bestOverlapLength) {
          bestOverlapLength = overlapLength;
          bestMatch = addedEntry;
        }
      }
      if (bestMatch != null) {
        renamed.add(
            new CategoryDiff.Rename(
                removedEntry.constantName(),
                removedEntry.url(),
                bestMatch.constantName(),
                bestMatch.url()));
        stillAdded.remove(bestMatch.constantName());
        stillRemoved.remove(removedEntry.constantName());
      }
    }

    return new CategoryDiff(
        categoryName,
        List.copyOf(stillAdded.values()),
        List.copyOf(stillRemoved.values()),
        renamed);
  }

  /**
   * Returns the length of the shorter normalized constant name if it is fully contained in the
   * longer one (e.g. {@code VITALSTATUS} in {@code MII_PR_PERSON_VITALSTATUS}), or {@code 0}
   * otherwise. This catches the common IG-evolution pattern of a bare id being adopted into the
   * IG's prefixed naming convention, while shared boilerplate prefixes (e.g. {@code mii-cs-onko-})
   * alone don't trigger a false match, since neither full id is contained in the other.
   */
  private static int nameOverlapLength(String oldConstantName, String newConstantName) {
    String oldNormalized = normalize(oldConstantName);
    String newNormalized = normalize(newConstantName);
    String shorter =
        oldNormalized.length() <= newNormalized.length() ? oldNormalized : newNormalized;
    String longer =
        oldNormalized.length() <= newNormalized.length() ? newNormalized : oldNormalized;
    if (shorter.length() < MIN_OVERLAP_LENGTH || !longer.contains(shorter)) {
      return 0;
    }
    return shorter.length();
  }

  private static String normalize(String constantName) {
    return constantName.replace("_", "").toLowerCase(Locale.ROOT);
  }
}

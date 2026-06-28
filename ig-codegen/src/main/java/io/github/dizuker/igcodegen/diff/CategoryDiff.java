package io.github.dizuker.igcodegen.diff;

import java.util.List;

/**
 * The result of diffing one constant category (CodeSystems, Profiles or Extensions) between two
 * versions of an {@link io.github.dizuker.igcodegen.IgPackageModel}.
 *
 * <p>{@code renamed} is a heuristic: an added and a removed entry in the same category whose
 * constant names are similar enough are reported as a likely rename rather than as two unrelated
 * add/remove events. It is best-effort — IG authors are free to rename a resource's {@code id}
 * beyond what name-similarity can detect, in which case it will surface as a plain add + remove.
 */
public record CategoryDiff(
    String categoryName, List<Entry> added, List<Entry> removed, List<Rename> renamed) {

  public boolean isEmpty() {
    return added.isEmpty() && removed.isEmpty() && renamed.isEmpty();
  }

  public record Entry(String constantName, String url) {}

  public record Rename(
      String oldConstantName, String oldUrl, String newConstantName, String newUrl) {}
}

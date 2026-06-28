package io.github.dizuker.igcodegen.diff;

import io.github.dizuker.igcodegen.IgPackageModel;
import io.github.dizuker.igcodegen.IgPackageScanner;
import java.nio.file.Path;
import java.util.List;
import tools.jackson.databind.ObjectMapper;

/**
 * CLI: report added/removed/renamed canonical URLs between two versions of the same FHIR package,
 * both already restored under the Firely Terminal package cache.
 *
 * <p>Usage: {@code IgDiffMain <fhirPackagesDir> <packageName> <oldVersion> <newVersion>}
 */
public final class IgDiffMain {

  private IgDiffMain() {}

  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println(
          "Usage: IgDiffMain <fhirPackagesDir> <packageName> <oldVersion> <newVersion>");
      return;
    }
    Path fhirPackagesDir = Path.of(args[0]);
    String packageName = args[1];
    String oldVersion = args[2];
    String newVersion = args[3];

    IgPackageScanner scanner = new IgPackageScanner(new ObjectMapper());
    IgPackageModel oldModel =
        scanner.scan(
            scanner.resolvePackageContentDir(fhirPackagesDir, packageName, oldVersion),
            packageName,
            oldVersion);
    IgPackageModel newModel =
        scanner.scan(
            scanner.resolvePackageContentDir(fhirPackagesDir, packageName, newVersion),
            packageName,
            newVersion);

    List<CategoryDiff> categoryDiffs = IgDiff.diff(oldModel, newModel);
    print(packageName, oldVersion, newVersion, categoryDiffs);
  }

  private static void print(
      String packageName, String oldVersion, String newVersion, List<CategoryDiff> categoryDiffs) {
    System.out.printf("%s: %s -> %s%n", packageName, oldVersion, newVersion);
    for (CategoryDiff categoryDiff : categoryDiffs) {
      if (categoryDiff.isEmpty()) {
        continue;
      }
      System.out.println("  " + categoryDiff.categoryName() + ":");
      for (CategoryDiff.Entry entry : categoryDiff.added()) {
        System.out.println("    + " + entry.constantName() + " = " + entry.url());
      }
      for (CategoryDiff.Entry entry : categoryDiff.removed()) {
        System.out.println("    - " + entry.constantName() + " = " + entry.url());
      }
      for (CategoryDiff.Rename rename : categoryDiff.renamed()) {
        System.out.println(
            "    ~ "
                + rename.oldConstantName()
                + " -> "
                + rename.newConstantName()
                + " (possible rename, verify manually)");
      }
    }
  }
}

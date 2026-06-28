package io.github.dizuker.igcodegen.diff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.dizuker.igcodegen.IgPackageModel;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class IgDiffTest {

  @Test
  void reportsPlainAdditionsAndRemovals() {
    TreeMap<String, String> oldProfiles = new TreeMap<>();
    oldProfiles.put(
        "MII_PR_ONKO_OPERATION",
        "https://example.org/StructureDefinition/mii-pr-onko-operation|1.0.0");
    TreeMap<String, String> newProfiles = new TreeMap<>();
    newProfiles.put(
        "MII_PR_ONKO_OPERATION",
        "https://example.org/StructureDefinition/mii-pr-onko-operation|2.0.0");
    newProfiles.put(
        "MII_PR_ONKO_RADIATION",
        "https://example.org/StructureDefinition/mii-pr-onko-radiation|2.0.0");

    IgPackageModel oldModel = model(new TreeMap<>(), oldProfiles, new TreeMap<>());
    IgPackageModel newModel = model(new TreeMap<>(), newProfiles, new TreeMap<>());

    CategoryDiff profilesDiff = categoryDiff(IgDiff.diff(oldModel, newModel), "Profiles");

    assertEquals(1, profilesDiff.added().size());
    assertEquals("MII_PR_ONKO_RADIATION", profilesDiff.added().get(0).constantName());
    assertEquals(List.of(), profilesDiff.removed());
    assertEquals(List.of(), profilesDiff.renamed());
  }

  @Test
  void detectsLikelyRenameByTokenOverlapBetweenOldAndNewId() {
    // Mirrors a real upstream rename: a base-package id like "Vitalstatus" became the
    // IG-convention id "mii-pr-person-vitalstatus" in a later package version.
    TreeMap<String, String> oldProfiles = new TreeMap<>();
    oldProfiles.put("VITALSTATUS", "https://example.org/StructureDefinition/Vitalstatus|1.0.0");
    TreeMap<String, String> newProfiles = new TreeMap<>();
    newProfiles.put(
        "MII_PR_PERSON_VITALSTATUS",
        "https://example.org/StructureDefinition/mii-pr-person-vitalstatus|2.0.0");

    IgPackageModel oldModel = model(new TreeMap<>(), oldProfiles, new TreeMap<>());
    IgPackageModel newModel = model(new TreeMap<>(), newProfiles, new TreeMap<>());

    CategoryDiff profilesDiff = categoryDiff(IgDiff.diff(oldModel, newModel), "Profiles");

    assertEquals(List.of(), profilesDiff.added());
    assertEquals(List.of(), profilesDiff.removed());
    assertEquals(1, profilesDiff.renamed().size());
    CategoryDiff.Rename rename = profilesDiff.renamed().get(0);
    assertEquals("VITALSTATUS", rename.oldConstantName());
    assertEquals("MII_PR_PERSON_VITALSTATUS", rename.newConstantName());
  }

  @Test
  void unrelatedAddAndRemoveAreNotReportedAsRename() {
    TreeMap<String, String> oldCodeSystems = new TreeMap<>();
    oldCodeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    TreeMap<String, String> newCodeSystems = new TreeMap<>();
    newCodeSystems.put("MII_CS_ONKO_GRADING", "https://example.org/CodeSystem/mii-cs-onko-grading");

    IgPackageModel oldModel = model(oldCodeSystems, new TreeMap<>(), new TreeMap<>());
    IgPackageModel newModel = model(newCodeSystems, new TreeMap<>(), new TreeMap<>());

    CategoryDiff codeSystemsDiff = categoryDiff(IgDiff.diff(oldModel, newModel), "CodeSystems");

    assertTrue(codeSystemsDiff.renamed().isEmpty());
    assertEquals(1, codeSystemsDiff.added().size());
    assertEquals(1, codeSystemsDiff.removed().size());
  }

  private static CategoryDiff categoryDiff(List<CategoryDiff> diffs, String categoryName) {
    return diffs.stream()
        .filter(d -> d.categoryName().equals(categoryName))
        .findFirst()
        .orElseThrow();
  }

  private static IgPackageModel model(
      TreeMap<String, String> codeSystems,
      TreeMap<String, String> profiles,
      TreeMap<String, String> extensions) {
    return new IgPackageModel(
        "de.example.onkologie", "1.0.0", codeSystems, profiles, extensions, Map.of());
  }
}

package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises the scanner against FHIR packages actually restored to the local Firely Terminal cache
 * ({@code ~/.fhir/packages}), as listed in {@code ig-codegen/package.json}. Skips if the packages
 * aren't present (e.g. on a machine that hasn't run {@code fhir restore}).
 */
class IgPackageScannerTest {

  private static final String PACKAGE_NAME =
      "de.medizininformatikinitiative.kerndatensatz.onkologie";
  private static final String PACKAGE_VERSION = "2026.0.3";

  private final IgPackageScanner scanner = new IgPackageScanner(new ObjectMapper());
  private Path fhirPackagesDir;

  @BeforeEach
  void setUp() {
    fhirPackagesDir = Path.of(System.getProperty("user.home"), ".fhir", "packages");
    Path packageContentDir =
        scanner.resolvePackageContentDir(fhirPackagesDir, PACKAGE_NAME, PACKAGE_VERSION);
    assumeTrue(
        Files.isDirectory(packageContentDir),
        "FHIR package not restored locally: " + packageContentDir);
  }

  @Test
  void classifiesCodeSystemsByPlainUrl() {
    IgPackageModel model = scan();

    assertTrue(model.codeSystems().containsKey("MII_CS_ONKO_INTENTION"));
    assertEquals(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/CodeSystem/mii-cs-onko-intention",
        model.codeSystems().get("MII_CS_ONKO_INTENTION"));
  }

  @Test
  void classifiesProfilesByVersionedUrl() {
    IgPackageModel model = scan();

    String value = model.profiles().get("MII_PR_ONKO_ALLGEMEINER_LEISTUNGSZUSTAND_ECOG");
    assertTrue(value != null && value.endsWith("|" + PACKAGE_VERSION));
    assertTrue(value.contains("StructureDefinition/mii-pr-onko-allgemeiner-leistungszustand-ecog"));
  }

  @Test
  void classifiesExtensionsByPlainUrl() {
    IgPackageModel model = scan();

    String value = model.extensions().get("MII_EX_ONKO_STRAHLENTHERAPIE_BESTRAHLUNG_EINZELDOSIS");
    assertEquals(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/StructureDefinition/mii-ex-onko-strahlentherapie-bestrahlung-einzeldosis",
        value);
  }

  @Test
  void excludesLogicalModelsAndBaseTypeSpecializations() {
    IgPackageModel model = scan();

    boolean anyVersionFreeProfile =
        model.profiles().values().stream().noneMatch(value -> !value.contains("|"));
    assertFalse(model.profiles().isEmpty());
    assertTrue(anyVersionFreeProfile, "every Profile value must carry a |<version> suffix");
  }

  @Test
  void expandsCompleteCodeSystemConceptsIntoConceptConstants() {
    IgPackageModel model = scan();

    List<ConceptConstant> intention = model.codeSystemConcepts().get("MII_CS_ONKO_INTENTION");
    assertTrue(intention != null && intention.size() == 7);
    assertTrue(
        intention.stream()
            .anyMatch(
                c ->
                    c.constantName().equals("K")
                        && c.code().equals("K")
                        && "kurativ".equals(c.display())));
  }

  @Test
  void doesNotExpandNonCompleteCodeSystems() {
    IgPackageModel model = scan();

    // mii-cs-onko-krk-operationstyp has content == "fragment" in this package, not "complete".
    assertFalse(model.codeSystemConcepts().containsKey("MII_CS_ONKO_KRK_OPERATIONSTYP"));
  }

  private IgPackageModel scan() {
    Path packageContentDir =
        scanner.resolvePackageContentDir(fhirPackagesDir, PACKAGE_NAME, PACKAGE_VERSION);
    return scanner.scan(packageContentDir, PACKAGE_NAME, PACKAGE_VERSION);
  }
}

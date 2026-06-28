package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

/**
 * Exercises {@link IgPackageScanner#resolvePackageContentDir} with synthetic directory trees, to
 * cover the Firely Terminal vs. npm layout fallback without depending on real restored packages.
 */
class IgPackageScannerLayoutFallbackTest {

  private static final String PACKAGE_NAME = "de.example.onkologie";
  private static final String PACKAGE_VERSION = "1.0.0";

  private final IgPackageScanner scanner = new IgPackageScanner(new ObjectMapper());

  @Test
  void prefersFirelyTerminalLayoutWhenBothExist(@TempDir Path fhirPackagesDir) throws Exception {
    Path firelyLayout =
        Files.createDirectories(
            fhirPackagesDir.resolve(PACKAGE_NAME + "#" + PACKAGE_VERSION).resolve("package"));
    Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME)); // npm-style, also present

    assertEquals(
        firelyLayout,
        scanner.resolvePackageContentDir(fhirPackagesDir, PACKAGE_NAME, PACKAGE_VERSION));
  }

  @Test
  void fallsBackToFlatNpmLayoutWhenFirelyLayoutIsMissing(@TempDir Path fhirPackagesDir)
      throws Exception {
    Path npmLayout = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));

    assertEquals(
        npmLayout,
        scanner.resolvePackageContentDir(fhirPackagesDir, PACKAGE_NAME, PACKAGE_VERSION));
  }

  @Test
  void scansResourcesFromNpmLayout(@TempDir Path fhirPackagesDir) throws Exception {
    Path npmLayout = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        npmLayout.resolve("CodeSystem-mii-cs-onko-intention.json"),
        """
        {
          "resourceType": "CodeSystem",
          "id": "mii-cs-onko-intention",
          "url": "https://example.org/CodeSystem/mii-cs-onko-intention",
          "version": "1.0.0",
          "content": "complete",
          "concept": [{"code": "K", "display": "kurativ"}]
        }
        """);

    Path packageContentDir =
        scanner.resolvePackageContentDir(fhirPackagesDir, PACKAGE_NAME, PACKAGE_VERSION);
    IgPackageModel model = scanner.scan(packageContentDir, PACKAGE_NAME, PACKAGE_VERSION);

    assertEquals(
        "https://example.org/CodeSystem/mii-cs-onko-intention",
        model.codeSystems().get("MII_CS_ONKO_INTENTION"));
  }
}

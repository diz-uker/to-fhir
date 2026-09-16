package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

/**
 * Covers resources sharing a package directory with the ones {@link IgPackageScanner} classifies.
 * FHIR reuses field names across resource types with incompatible shapes, so a resource the scanner
 * generates nothing from must not be able to fail the scan of the whole package.
 */
class IgPackageScannerForeignResourceTest {

  private static final String PACKAGE_NAME = "de.example.onkologie";
  private static final String PACKAGE_VERSION = "1.0.0";

  private final IgPackageScanner scanner = new IgPackageScanner(new ObjectMapper());

  @Test
  void ignoresResourceTypesItGeneratesNothingFrom(@TempDir Path fhirPackagesDir) throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    writeCodeSystem(packageContentDir);
    // Library.type is a CodeableConcept, where StructureDefinition.type is a plain code.
    Files.writeString(
        packageContentDir.resolve("Library-mii-lib-onko-synthesize-tnm.json"),
        """
        {
          "resourceType": "Library",
          "id": "mii-lib-onko-synthesize-tnm",
          "url": "https://example.org/Library/mii-lib-onko-synthesize-tnm",
          "type": {
            "coding": [
              {
                "system": "http://terminology.hl7.org/CodeSystem/library-type",
                "code": "logic-library"
              }
            ]
          }
        }
        """);
    // OperationDefinition.type is a boolean.
    Files.writeString(
        packageContentDir.resolve("OperationDefinition-example.json"),
        """
        {
          "resourceType": "OperationDefinition",
          "id": "example",
          "url": "https://example.org/OperationDefinition/example",
          "kind": "operation",
          "type": false
        }
        """);
    // The package's own npm-shaped manifest, which carries no resourceType at all.
    Files.writeString(
        packageContentDir.resolve("package.json"),
        """
        {"name": "de.example.onkologie", "version": "1.0.0", "type": "commonjs"}
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        "https://example.org/CodeSystem/mii-cs-onko-intention",
        model.codeSystems().get("MII_CS_ONKO_INTENTION"));
    assertTrue(model.profiles().isEmpty());
    assertTrue(model.extensions().isEmpty());
  }

  @Test
  void readsANamingSystemWhoseTypeIsACodeableConcept(@TempDir Path fhirPackagesDir)
      throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("NamingSystem-mii-ns-onko-beispiel.json"),
        """
        {
          "resourceType": "NamingSystem",
          "id": "mii-ns-onko-beispiel",
          "kind": "identifier",
          "description": "Beispiel-Namenssystem",
          "type": {
            "coding": [
              {"system": "http://terminology.hl7.org/CodeSystem/v2-0203", "code": "NI"}
            ]
          },
          "uniqueId": [{"type": "uri", "value": "https://example.org/ns/beispiel"}]
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    NamingSystemUniqueIds namingSystem = model.namingSystems().get("MII_NS_ONKO_BEISPIEL");
    assertNotNull(namingSystem);
    assertEquals("Beispiel-Namenssystem", namingSystem.description());
    assertEquals(List.of("https://example.org/ns/beispiel"), namingSystem.byType().get("uri"));
  }

  private static void writeCodeSystem(Path packageContentDir) throws Exception {
    Files.writeString(
        packageContentDir.resolve("CodeSystem-mii-cs-onko-intention.json"),
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
  }

  private IgPackageModel scan(Path fhirPackagesDir) {
    Path packageContentDir =
        scanner.resolvePackageContentDir(fhirPackagesDir, PACKAGE_NAME, PACKAGE_VERSION);
    return scanner.scan(packageContentDir, PACKAGE_NAME, PACKAGE_VERSION);
  }
}

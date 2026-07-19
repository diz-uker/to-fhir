package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

/**
 * Exercises {@link IgPackageScanner}'s detection of an extension's {@code Extension.value[x]}
 * shape, against synthetic {@code StructureDefinition} fixtures rather than real restored packages.
 */
class IgPackageScannerExtensionValueTypeTest {

  private static final String PACKAGE_NAME = "de.example.onkologie";
  private static final String PACKAGE_VERSION = "1.0.0";

  private final IgPackageScanner scanner = new IgPackageScanner(new ObjectMapper());

  @Test
  void detectsSingleValueTypeOnASimpleExtension(@TempDir Path fhirPackagesDir) throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-einzeldosis.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-einzeldosis",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-einzeldosis",
          "kind": "complex-type",
          "type": "Extension",
          "derivation": "constraint",
          "snapshot": {
            "element": [
              {"id": "Extension", "path": "Extension"},
              {"id": "Extension.url", "path": "Extension.url"},
              {
                "id": "Extension.value[x]",
                "path": "Extension.value[x]",
                "max": "1",
                "type": [{"code": "decimal"}]
              }
            ]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.fixed("decimal"),
        model.extensionValueTypes().get("MII_EX_ONKO_EINZELDOSIS"));
  }

  @Test
  void detectsChoiceValueTypeWhenMultipleTypesAreAllowed(@TempDir Path fhirPackagesDir)
      throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-choice.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-choice",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-choice",
          "kind": "complex-type",
          "type": "Extension",
          "derivation": "constraint",
          "snapshot": {
            "element": [
              {"id": "Extension", "path": "Extension"},
              {
                "id": "Extension.value[x]",
                "path": "Extension.value[x]",
                "max": "1",
                "type": [{"code": "string"}, {"code": "CodeableConcept"}]
              }
            ]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(ExtensionValueType.CHOICE, model.extensionValueTypes().get("MII_EX_ONKO_CHOICE"));
  }

  @Test
  void detectsNoValueOnAComplexExtensionWithSubExtensions(@TempDir Path fhirPackagesDir)
      throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-complex.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-complex",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-complex",
          "kind": "complex-type",
          "type": "Extension",
          "derivation": "constraint",
          "snapshot": {
            "element": [
              {"id": "Extension", "path": "Extension"},
              {
                "id": "Extension.extension:teil",
                "path": "Extension.extension"
              },
              {
                "id": "Extension.value[x]",
                "path": "Extension.value[x]",
                "max": "0"
              }
            ]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(ExtensionValueType.NONE, model.extensionValueTypes().get("MII_EX_ONKO_COMPLEX"));
  }

  @Test
  void defaultsToNoValueWhenSnapshotIsAbsent(@TempDir Path fhirPackagesDir) throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-nosnapshot.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-nosnapshot",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-nosnapshot",
          "kind": "complex-type",
          "type": "Extension",
          "derivation": "constraint"
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.NONE, model.extensionValueTypes().get("MII_EX_ONKO_NOSNAPSHOT"));
  }

  private IgPackageModel scan(Path fhirPackagesDir) {
    Path packageContentDir =
        scanner.resolvePackageContentDir(fhirPackagesDir, PACKAGE_NAME, PACKAGE_VERSION);
    return scanner.scan(packageContentDir, PACKAGE_NAME, PACKAGE_VERSION);
  }
}

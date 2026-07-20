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
  void detectsCodeSystemBoundToACodeableConceptValueViaAFixedCodingSystem(
      @TempDir Path fhirPackagesDir) throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-intention.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-intention",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-intention",
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
                "type": [{"code": "CodeableConcept"}]
              },
              {
                "id": "Extension.value[x].coding.system",
                "path": "Extension.value[x].coding.system",
                "fixedUri": "https://example.org/CodeSystem/mii-cs-onko-intention"
              }
            ]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.boundCoding(
            "CodeableConcept", "https://example.org/CodeSystem/mii-cs-onko-intention"),
        model.extensionValueTypes().get("MII_EX_ONKO_INTENTION"));
  }

  @Test
  void detectsCodeSystemBoundToADirectCodingValueViaAFixedSystem(@TempDir Path fhirPackagesDir)
      throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-status.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-status",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-status",
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
                "type": [{"code": "Coding"}]
              },
              {
                "id": "Extension.value[x].system",
                "path": "Extension.value[x].system",
                "fixedUri": "https://example.org/CodeSystem/mii-cs-onko-status"
              }
            ]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.boundCoding(
            "Coding", "https://example.org/CodeSystem/mii-cs-onko-status"),
        model.extensionValueTypes().get("MII_EX_ONKO_STATUS"));
  }

  @Test
  void codeableConceptWithoutAFixedCodingSystemIsJustAFixedType(@TempDir Path fhirPackagesDir)
      throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-freitext.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-freitext",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-freitext",
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
                "type": [{"code": "CodeableConcept"}]
              }
            ]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.fixed("CodeableConcept"),
        model.extensionValueTypes().get("MII_EX_ONKO_FREITEXT"));
  }

  @Test
  void detectsCodeSystemBoundViaARequiredValueSetBindingWithNoFixedUri(
      @TempDir Path fhirPackagesDir) throws Exception {
    // Mirrors mii-ex-onko-strahlentherapie-bestrahlung-boost: the snapshot doesn't expand
    // value[x].coding.system at all, only a required binding to a single-CodeSystem ValueSet.
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-boost.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-boost",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-boost",
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
                "type": [{"code": "CodeableConcept"}],
                "binding": {
                  "strength": "required",
                  "valueSet": "https://example.org/ValueSet/mii-vs-onko-boost"
                }
              }
            ]
          }
        }
        """);
    Files.writeString(
        packageContentDir.resolve("ValueSet-mii-vs-onko-boost.json"),
        """
        {
          "resourceType": "ValueSet",
          "id": "mii-vs-onko-boost",
          "url": "https://example.org/ValueSet/mii-vs-onko-boost",
          "compose": {
            "include": [{"system": "https://example.org/CodeSystem/mii-cs-onko-boost"}]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.boundCoding(
            "CodeableConcept", "https://example.org/CodeSystem/mii-cs-onko-boost"),
        model.extensionValueTypes().get("MII_EX_ONKO_BOOST"));
  }

  @Test
  void ignoresAnExtensibleValueSetBinding(@TempDir Path fhirPackagesDir) throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-extensible.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-extensible",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-extensible",
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
                "type": [{"code": "CodeableConcept"}],
                "binding": {
                  "strength": "extensible",
                  "valueSet": "https://example.org/ValueSet/mii-vs-onko-extensible"
                }
              }
            ]
          }
        }
        """);
    Files.writeString(
        packageContentDir.resolve("ValueSet-mii-vs-onko-extensible.json"),
        """
        {
          "resourceType": "ValueSet",
          "id": "mii-vs-onko-extensible",
          "url": "https://example.org/ValueSet/mii-vs-onko-extensible",
          "compose": {
            "include": [{"system": "https://example.org/CodeSystem/mii-cs-onko-extensible"}]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.fixed("CodeableConcept"),
        model.extensionValueTypes().get("MII_EX_ONKO_EXTENSIBLE"));
  }

  @Test
  void ignoresARequiredBindingToAValueSetComposedFromMultipleCodeSystems(
      @TempDir Path fhirPackagesDir) throws Exception {
    Path packageContentDir = Files.createDirectories(fhirPackagesDir.resolve(PACKAGE_NAME));
    Files.writeString(
        packageContentDir.resolve("StructureDefinition-mii-ex-onko-multi.json"),
        """
        {
          "resourceType": "StructureDefinition",
          "id": "mii-ex-onko-multi",
          "url": "https://example.org/StructureDefinition/mii-ex-onko-multi",
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
                "type": [{"code": "CodeableConcept"}],
                "binding": {
                  "strength": "required",
                  "valueSet": "https://example.org/ValueSet/mii-vs-onko-multi"
                }
              }
            ]
          }
        }
        """);
    Files.writeString(
        packageContentDir.resolve("ValueSet-mii-vs-onko-multi.json"),
        """
        {
          "resourceType": "ValueSet",
          "id": "mii-vs-onko-multi",
          "url": "https://example.org/ValueSet/mii-vs-onko-multi",
          "compose": {
            "include": [
              {"system": "https://example.org/CodeSystem/mii-cs-onko-a"},
              {"system": "https://example.org/CodeSystem/mii-cs-onko-b"}
            ]
          }
        }
        """);

    IgPackageModel model = scan(fhirPackagesDir);

    assertEquals(
        ExtensionValueType.fixed("CodeableConcept"),
        model.extensionValueTypes().get("MII_EX_ONKO_MULTI"));
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

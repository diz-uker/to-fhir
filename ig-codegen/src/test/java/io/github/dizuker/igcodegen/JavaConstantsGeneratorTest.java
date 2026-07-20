package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.palantir.javapoet.JavaFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaConstantsGeneratorTest {

  @Test
  void generatesAccessorClassesOnlyForNonEmptyCategories() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    IgPackageModel model = model(codeSystems, new TreeMap<>(), new TreeMap<>(), Map.of());

    JavaFile javaFile = JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie");
    String source = javaFile.toString();

    assertTrue(source.contains("class CodeSystems"));
    assertFalse(source.contains("class Profiles"));
    assertFalse(source.contains("class Extensions"));
    assertTrue(source.contains("String miiCsOnkoIntention()"));
    assertTrue(source.contains("https://example.org/CodeSystem/mii-cs-onko-intention"));
  }

  @Test
  void accessorMethodHasJavadocWithTheUrlValue() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    IgPackageModel model = model(codeSystems, new TreeMap<>(), new TreeMap<>(), Map.of());

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    int javadocIndex = source.indexOf("/**");
    int methodIndex = source.indexOf("String miiCsOnkoIntention()");
    assertTrue(
        javadocIndex >= 0 && javadocIndex < methodIndex,
        "expected a javadoc comment before the accessor method");
    assertTrue(
        source.contains("{@code https://example.org/CodeSystem/mii-cs-onko-intention}"),
        "expected the javadoc to contain the URL value");
  }

  @Test
  void profileValuesCarryVersionSuffix() {
    TreeMap<String, String> profiles = new TreeMap<>();
    profiles.put(
        "MII_PR_ONKO_OPERATION",
        "https://example.org/StructureDefinition/mii-pr-onko-operation|1.0.0");
    IgPackageModel model = model(new TreeMap<>(), profiles, new TreeMap<>(), Map.of());

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("String miiPrOnkoOperation()"));
    assertTrue(source.contains("mii-pr-onko-operation|1.0.0"));
  }

  @Test
  void writeToProducesFileAtExpectedJavaPackagePath(@TempDir Path tempDir) throws Exception {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put("FOO", "https://example.org/CodeSystem/foo");
    IgPackageModel model = model(codeSystems, new TreeMap<>(), new TreeMap<>(), Map.of());

    Path written =
        JavaConstantsGenerator.writeTo(model, "de.example.onkologie", "Onkologie", tempDir);

    assertEquals(tempDir.resolve("de/example/onkologie/Onkologie.java"), written);
    assertTrue(Files.exists(written));
    String source = Files.readString(written);
    assertTrue(source.contains("package de.example.onkologie;"));
    assertTrue(source.contains("String foo()"));
  }

  @Test
  void generatesEnumWithCodingAccessorForCodeSystemConcepts() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    Map<String, List<ConceptConstant>> codeSystemConcepts =
        Map.of(
            "MII_CS_ONKO_INTENTION",
            List.of(
                new ConceptConstant("K", "K", "kurativ"),
                new ConceptConstant("P", "P", "palliativ")));
    IgPackageModel model = model(codeSystems, new TreeMap<>(), new TreeMap<>(), codeSystemConcepts);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("import org.hl7.fhir.r4.model.Coding;"));
    assertTrue(source.contains("enum MiiCsOnkoIntention"));
    assertTrue(source.contains("{@code K} - kurativ"));
    assertTrue(source.contains("K(\"K\", \"kurativ\")"));
    assertTrue(source.contains("{@code P} - palliativ"));
    assertTrue(source.contains("P(\"P\", \"palliativ\")"));
    assertTrue(source.contains("public @NonNull Coding coding(@NonNull MiiCsOnkoIntention this)"));
    assertTrue(
        source.contains(
            "return new Coding(\"https://example.org/CodeSystem/mii-cs-onko-intention\", code,"
                + " display)"));
  }

  @Test
  void generatesFromValueLookupOnConceptEnum() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    Map<String, List<ConceptConstant>> codeSystemConcepts =
        Map.of(
            "MII_CS_ONKO_INTENTION",
            List.of(
                new ConceptConstant("K", "K", "kurativ"),
                new ConceptConstant("P", "P", "palliativ")));
    IgPackageModel model = model(codeSystems, new TreeMap<>(), new TreeMap<>(), codeSystemConcepts);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(
        source.contains(
            "public static Optional<@NonNull MiiCsOnkoIntention> fromValue(@NonNull String code)"));
    assertTrue(source.contains("for (MiiCsOnkoIntention value : values())"));
    assertTrue(source.contains("if (value.code.equals(code))"));
    assertTrue(source.contains("return Optional.of(value)"));
    assertTrue(source.contains("return Optional.empty()"));
  }

  @Test
  void extensionWithFixedValueTypeGetsATypedFactoryMethod() {
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_EINZELDOSIS",
        "https://example.org/StructureDefinition/mii-ex-onko-einzeldosis");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of("MII_EX_ONKO_EINZELDOSIS", ExtensionValueType.fixed("decimal"));
    IgPackageModel model =
        model(new TreeMap<>(), new TreeMap<>(), extensions, Map.of(), extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("import org.hl7.fhir.r4.model.DecimalType;"));
    assertTrue(source.contains("import org.hl7.fhir.r4.model.Extension;"));
    assertTrue(
        source.contains(
            "public static @NonNull Extension miiExOnkoEinzeldosis(@NonNull DecimalType value)"));
    assertTrue(
        source.contains(
            "return new Extension(\"https://example.org/StructureDefinition/mii-ex-onko-einzeldosis\","
                + " value)"));
  }

  @Test
  void extensionWithCodeableConceptValueTypeTakesACodeableConceptParameter() {
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_INTENTION", "https://example.org/StructureDefinition/mii-ex-onko-intention");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of("MII_EX_ONKO_INTENTION", ExtensionValueType.fixed("CodeableConcept"));
    IgPackageModel model =
        model(new TreeMap<>(), new TreeMap<>(), extensions, Map.of(), extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("import org.hl7.fhir.r4.model.CodeableConcept;"));
    assertTrue(
        source.contains(
            "public static @NonNull Extension miiExOnkoIntention(@NonNull CodeableConcept"
                + " value)"));
    assertTrue(
        source.contains(
            "return new Extension(\"https://example.org/StructureDefinition/mii-ex-onko-intention\","
                + " value)"));
  }

  @Test
  void extensionWithCodingValueTypeTakesACodingParameter() {
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_STATUS", "https://example.org/StructureDefinition/mii-ex-onko-status");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of("MII_EX_ONKO_STATUS", ExtensionValueType.fixed("Coding"));
    IgPackageModel model =
        model(new TreeMap<>(), new TreeMap<>(), extensions, Map.of(), extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("import org.hl7.fhir.r4.model.Coding;"));
    assertTrue(
        source.contains("public static @NonNull Extension miiExOnkoStatus(@NonNull Coding value)"));
  }

  @Test
  void codeableConceptExtensionBoundToALocalCodeSystemTakesItsGeneratedEnum() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    Map<String, List<ConceptConstant>> codeSystemConcepts =
        Map.of("MII_CS_ONKO_INTENTION", List.of(new ConceptConstant("K", "K", "kurativ")));
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_INTENTION", "https://example.org/StructureDefinition/mii-ex-onko-intention");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of(
            "MII_EX_ONKO_INTENTION",
            ExtensionValueType.boundCoding(
                "CodeableConcept", "https://example.org/CodeSystem/mii-cs-onko-intention"));
    IgPackageModel model =
        model(codeSystems, new TreeMap<>(), extensions, codeSystemConcepts, extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("import org.hl7.fhir.r4.model.CodeableConcept;"));
    assertTrue(source.contains("public static @NonNull Extension miiExOnkoIntention("));
    assertTrue(source.contains("CodeSystems. @NonNull MiiCsOnkoIntention value)"));
    assertTrue(
        source.contains(
            "return new Extension(\"https://example.org/StructureDefinition/mii-ex-onko-intention\","
                + " new CodeableConcept(value.coding()))"));
  }

  @Test
  void codingExtensionBoundToALocalCodeSystemTakesItsGeneratedEnum() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    Map<String, List<ConceptConstant>> codeSystemConcepts =
        Map.of("MII_CS_ONKO_INTENTION", List.of(new ConceptConstant("K", "K", "kurativ")));
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_INTENTION", "https://example.org/StructureDefinition/mii-ex-onko-intention");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of(
            "MII_EX_ONKO_INTENTION",
            ExtensionValueType.boundCoding(
                "Coding", "https://example.org/CodeSystem/mii-cs-onko-intention"));
    IgPackageModel model =
        model(codeSystems, new TreeMap<>(), extensions, codeSystemConcepts, extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("public static @NonNull Extension miiExOnkoIntention("));
    assertTrue(source.contains("CodeSystems. @NonNull MiiCsOnkoIntention value)"));
    assertTrue(
        source.contains(
            "return new Extension(\"https://example.org/StructureDefinition/mii-ex-onko-intention\","
                + " value.coding())"));
  }

  @Test
  void extensionBoundToCodeSystemWithoutAGeneratedEnumFallsBackToTheGenericDatatype() {
    // A CodeSystem present in the model but without concepts (e.g. an external terminology like
    // SNOMED CT) has no generated enum to bind to.
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put("SNOMED_CT", "http://snomed.info/sct");
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_PROZEDUR_INTENTION",
        "https://example.org/StructureDefinition/mii-ex-prozedur-intention");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of(
            "MII_EX_PROZEDUR_INTENTION",
            ExtensionValueType.boundCoding("Coding", "http://snomed.info/sct"));
    IgPackageModel model =
        model(codeSystems, new TreeMap<>(), extensions, Map.of(), extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(
        source.contains(
            "public static @NonNull Extension miiExProzedurIntention(@NonNull Coding value)"));
  }

  @Test
  void extensionWithChoiceValueTypeFallsBackToGenericHapiType() {
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_CHOICE", "https://example.org/StructureDefinition/mii-ex-onko-choice");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of("MII_EX_ONKO_CHOICE", ExtensionValueType.CHOICE);
    IgPackageModel model =
        model(new TreeMap<>(), new TreeMap<>(), extensions, Map.of(), extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("import org.hl7.fhir.r4.model.Type;"));
    assertTrue(
        source.contains("public static @NonNull Extension miiExOnkoChoice(@NonNull Type value)"));
  }

  @Test
  void complexExtensionWithNoValueGetsANoArgFactoryMethod() {
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_COMPLEX", "https://example.org/StructureDefinition/mii-ex-onko-complex");
    Map<String, ExtensionValueType> extensionValueTypes =
        Map.of("MII_EX_ONKO_COMPLEX", ExtensionValueType.NONE);
    IgPackageModel model =
        model(new TreeMap<>(), new TreeMap<>(), extensions, Map.of(), extensionValueTypes);

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("public static @NonNull Extension miiExOnkoComplex()"));
    assertTrue(
        source.contains(
            "return new Extension(\"https://example.org/StructureDefinition/mii-ex-onko-complex\")"));
  }

  @Test
  void extensionWithNoRecordedValueTypeDefaultsToNoArgFactoryMethod() {
    TreeMap<String, String> extensions = new TreeMap<>();
    extensions.put(
        "MII_EX_ONKO_UNKNOWN", "https://example.org/StructureDefinition/mii-ex-onko-unknown");
    IgPackageModel model = model(new TreeMap<>(), new TreeMap<>(), extensions, Map.of());

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("public static @NonNull Extension miiExOnkoUnknown()"));
  }

  @Test
  void doesNotGenerateEnumWhenCodeSystemHasNoConcepts() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    IgPackageModel model = model(codeSystems, new TreeMap<>(), new TreeMap<>(), Map.of());

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertFalse(source.contains("enum"));
  }

  private static IgPackageModel model(
      TreeMap<String, String> codeSystems,
      TreeMap<String, String> profiles,
      TreeMap<String, String> extensions,
      Map<String, List<ConceptConstant>> codeSystemConcepts) {
    return model(codeSystems, profiles, extensions, codeSystemConcepts, Map.of());
  }

  private static IgPackageModel model(
      TreeMap<String, String> codeSystems,
      TreeMap<String, String> profiles,
      TreeMap<String, String> extensions,
      Map<String, List<ConceptConstant>> codeSystemConcepts,
      Map<String, ExtensionValueType> extensionValueTypes) {
    return new IgPackageModel(
        "de.example.onkologie",
        "1.0.0",
        codeSystems,
        profiles,
        extensions,
        codeSystemConcepts,
        extensionValueTypes);
  }
}

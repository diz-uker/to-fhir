package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.palantir.javapoet.JavaFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaConstantsGeneratorTest {

  @Test
  void generatesAccessorClassesOnlyForNonEmptyCategories() {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put(
        "MII_CS_ONKO_INTENTION", "https://example.org/CodeSystem/mii-cs-onko-intention");
    IgPackageModel model =
        new IgPackageModel(
            "de.example.onkologie", "1.0.0", codeSystems, new TreeMap<>(), new TreeMap<>());

    JavaFile javaFile = JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie");
    String source = javaFile.toString();

    assertTrue(source.contains("class CodeSystems"));
    assertFalse(source.contains("class Profiles"));
    assertFalse(source.contains("class Extensions"));
    assertTrue(source.contains("String miiCsOnkoIntention()"));
    assertTrue(source.contains("https://example.org/CodeSystem/mii-cs-onko-intention"));
  }

  @Test
  void profileValuesCarryVersionSuffix() {
    TreeMap<String, String> profiles = new TreeMap<>();
    profiles.put(
        "MII_PR_ONKO_OPERATION",
        "https://example.org/StructureDefinition/mii-pr-onko-operation|1.0.0");
    IgPackageModel model =
        new IgPackageModel(
            "de.example.onkologie", "1.0.0", new TreeMap<>(), profiles, new TreeMap<>());

    String source =
        JavaConstantsGenerator.generate(model, "de.example.onkologie", "Onkologie").toString();

    assertTrue(source.contains("String miiPrOnkoOperation()"));
    assertTrue(source.contains("mii-pr-onko-operation|1.0.0"));
  }

  @Test
  void writeToProducesFileAtExpectedJavaPackagePath(@TempDir Path tempDir) throws Exception {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    codeSystems.put("FOO", "https://example.org/CodeSystem/foo");
    IgPackageModel model =
        new IgPackageModel(
            "de.example.onkologie", "1.0.0", codeSystems, new TreeMap<>(), new TreeMap<>());

    Path written =
        JavaConstantsGenerator.writeTo(model, "de.example.onkologie", "Onkologie", tempDir);

    assertEquals(tempDir.resolve("de/example/onkologie/Onkologie.java"), written);
    assertTrue(Files.exists(written));
    String source = Files.readString(written);
    assertTrue(source.contains("package de.example.onkologie;"));
    assertTrue(source.contains("String foo()"));
  }
}

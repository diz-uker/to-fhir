package io.github.dizuker.igcodegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end: drives {@code ig-codegen/package.json} (this module's own manifest, listing the
 * packages restored for development/testing) against the local Firely Terminal cache.
 */
class IgCodegenTest {

  @Test
  void generatesOneClassPerNonSkippedDependency(@TempDir Path tempDir) throws Exception {
    Path packageJsonFile = Path.of("package.json");
    Path fhirPackagesDir = Path.of(System.getProperty("user.home"), ".fhir", "packages");
    assumeTrue(Files.isRegularFile(packageJsonFile), "missing " + packageJsonFile.toAbsolutePath());

    List<Path> generatedFiles = new IgCodegen().generate(packageJsonFile, fhirPackagesDir, tempDir);

    assertFalse(generatedFiles.isEmpty());
    Path onkologie =
        tempDir.resolve("de/medizininformatikinitiative/kerndatensatz/onkologie/Onkologie.java");
    assertTrue(generatedFiles.contains(onkologie));
    assertTrue(Files.exists(onkologie));
    // hl7.fhir.r4.core has no IG-specific canonical prefix and must be skipped.
    assertTrue(generatedFiles.stream().noneMatch(p -> p.toString().contains("hl7")));
  }

  @Test
  void defaultFhirPackagesDirPrefersHomeDirWhenItExists(@TempDir Path fakeHome) throws Exception {
    Path homeFhirPackages = Files.createDirectories(fakeHome.resolve(".fhir").resolve("packages"));

    withUserHome(
        fakeHome, () -> assertEquals(homeFhirPackages, IgCodegen.defaultFhirPackagesDir()));
  }

  @Test
  void defaultFhirPackagesDirFallsBackToCurrentDirWhenHomeDirIsMissing(@TempDir Path fakeHome) {
    withUserHome(
        fakeHome,
        () -> assertEquals(Path.of(".fhir", "packages"), IgCodegen.defaultFhirPackagesDir()));
  }

  private static void withUserHome(Path fakeHome, Runnable test) {
    String originalUserHome = System.getProperty("user.home");
    System.setProperty("user.home", fakeHome.toString());
    try {
      test.run();
    } finally {
      System.setProperty("user.home", originalUserHome);
    }
  }
}

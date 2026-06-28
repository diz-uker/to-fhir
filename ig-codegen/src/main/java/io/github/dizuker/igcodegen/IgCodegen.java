package io.github.dizuker.igcodegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates one Java constant class per FHIR IG package declared in a package manifest's {@code
 * dependencies}.
 *
 * <p>Each generated class resides in a Java package with the same name as the FHIR package (e.g.
 * the FHIR package {@code de.medizininformatikinitiative.kerndatensatz.onkologie} produces the Java
 * package {@code de.medizininformatikinitiative.kerndatensatz.onkologie}), so canonical URLs don't
 * have to be hand-transcribed into application config.
 */
public final class IgCodegen {

  /** FHIR packages with no IG-specific canonical prefix of their own; nothing to generate. */
  private static final List<String> SKIPPED_PACKAGES = List.of("hl7.fhir.r4.core");

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final IgPackageScanner scanner = new IgPackageScanner(objectMapper);

  /**
   * Generates one Java constant class per non-skipped dependency in {@code packageJsonFile}.
   *
   * @param packageJsonFile the FHIR package manifest listing the IG packages to generate from
   * @param fhirPackagesDir where restored FHIR packages live, e.g. the Firely Terminal cache
   *     ({@code ~/.fhir/packages}) or an npm {@code node_modules}
   * @param outputDir the Java source root to write generated classes into
   */
  public List<Path> generate(Path packageJsonFile, Path fhirPackagesDir, Path outputDir)
      throws IOException {
    PackageManifest manifest = PackageManifest.read(packageJsonFile, objectMapper);

    List<Path> generatedFiles = new ArrayList<>();
    for (Map.Entry<String, String> dependency : manifest.dependencies().entrySet()) {
      String packageName = dependency.getKey();
      if (SKIPPED_PACKAGES.contains(packageName)) {
        continue;
      }
      String packageVersion = dependency.getValue();

      Path packageContentDir =
          scanner.resolvePackageContentDir(fhirPackagesDir, packageName, packageVersion);
      IgPackageModel model = scanner.scan(packageContentDir, packageName, packageVersion);

      String className = NameUtils.toPascalCase(lastSegment(packageName));
      generatedFiles.add(JavaConstantsGenerator.writeTo(model, packageName, className, outputDir));
    }
    return List.copyOf(generatedFiles);
  }

  private static String lastSegment(String dotSeparatedName) {
    int lastDot = dotSeparatedName.lastIndexOf('.');
    return lastDot < 0 ? dotSeparatedName : dotSeparatedName.substring(lastDot + 1);
  }

  public static void main(String[] args) throws IOException {
    Path packageJsonFile =
        args.length > 0 ? Path.of(args[0]) : Path.of("ig-codegen", "package.json");
    Path fhirPackagesDir = args.length > 1 ? Path.of(args[1]) : defaultFhirPackagesDir();
    Path outputDir =
        args.length > 2 ? Path.of(args[2]) : Path.of("build", "generated", "sources", "ig-codegen");

    List<Path> generatedFiles =
        new IgCodegen().generate(packageJsonFile, fhirPackagesDir, outputDir);
    generatedFiles.forEach(file -> System.out.println("Generated " + file));
  }

  /**
   * Tries, in order: the Firely Terminal global package cache ({@code ~/.fhir/packages}); a
   * project-local {@code ./.fhir/packages}, e.g. when a local Firely Terminal config restores
   * packages relative to the current directory instead; and finally {@code ./node_modules}, for
   * FHIR packages installed via {@code npm install} rather than {@code fhir restore}.
   *
   * <p>Resolves the home directory from the {@code HOME} environment variable rather than the
   * {@code user.home} system property: the JVM derives {@code user.home} from the OS user database,
   * which in some containers (e.g. GitHub Actions container jobs, where {@code HOME} is overridden
   * to {@code /github/home}) doesn't match the directory tools like Firely Terminal actually
   * restore into.
   */
  static Path defaultFhirPackagesDir() {
    String home = System.getenv("HOME");
    if (home == null || home.isEmpty()) {
      home = System.getProperty("user.home");
    }
    return defaultFhirPackagesDir(home, Path.of(""));
  }

  static Path defaultFhirPackagesDir(String homeDir, Path cwd) {
    Path homeFhirPackagesDir = Path.of(homeDir, ".fhir", "packages");
    if (Files.isDirectory(homeFhirPackagesDir)) {
      return homeFhirPackagesDir;
    }
    Path cwdFhirPackagesDir = cwd.resolve(".fhir").resolve("packages");
    if (Files.isDirectory(cwdFhirPackagesDir)) {
      return cwdFhirPackagesDir;
    }
    return cwd.resolve("node_modules");
  }
}

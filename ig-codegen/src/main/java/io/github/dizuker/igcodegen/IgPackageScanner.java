package io.github.dizuker.igcodegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Scans a restored FHIR package directory ({@code ~/.fhir/packages/<name>#<version>/package/}) and
 * classifies its resources into CodeSystem/Profile/Extension canonical URL constants.
 */
public final class IgPackageScanner {

  private final ObjectMapper objectMapper;

  public IgPackageScanner(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /** Resolves {@code <fhirPackagesDir>/<packageName>#<packageVersion>/package}. */
  public Path resolvePackageContentDir(
      Path fhirPackagesDir, String packageName, String packageVersion) {
    return fhirPackagesDir.resolve(packageName + "#" + packageVersion).resolve("package");
  }

  public IgPackageModel scan(Path packageContentDir, String packageName, String packageVersion) {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    TreeMap<String, String> profiles = new TreeMap<>();
    TreeMap<String, String> extensions = new TreeMap<>();

    try (DirectoryStream<Path> files = Files.newDirectoryStream(packageContentDir, "*.json")) {
      for (Path file : files) {
        classify(file, codeSystems, profiles, extensions);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Failed to scan FHIR package directory: " + packageContentDir, e);
    }

    return new IgPackageModel(packageName, packageVersion, codeSystems, profiles, extensions);
  }

  private void classify(
      Path file,
      Map<String, String> codeSystems,
      Map<String, String> profiles,
      Map<String, String> extensions) {
    FhirResourceSummary resource;
    try {
      resource = objectMapper.readValue(file.toFile(), FhirResourceSummary.class);
    } catch (IOException e) {
      // Not every *.json file in a package directory is a FHIR resource (e.g. .index.json);
      // skip anything that doesn't parse as one.
      return;
    }

    String resourceType = resource.resourceType();
    String id = resource.id();
    String url = resource.url();
    if (resourceType == null || id == null || url == null) {
      return;
    }

    String constantName = NameUtils.toConstantName(id);

    if ("CodeSystem".equals(resourceType)) {
      codeSystems.put(constantName, url);
      return;
    }

    if (!"StructureDefinition".equals(resourceType)) {
      return;
    }

    String kind = resource.kind();
    String derivation = resource.derivation();
    if ("logical".equals(kind) || "specialization".equals(derivation)) {
      return;
    }

    if ("complex-type".equals(kind)
        && "constraint".equals(derivation)
        && "Extension".equals(resource.type())) {
      extensions.put(constantName, url);
      return;
    }

    if ("resource".equals(kind) && "constraint".equals(derivation)) {
      String version = resource.version();
      profiles.put(constantName, version == null ? url : url + "|" + version);
    }
  }
}

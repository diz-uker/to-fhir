package io.github.dizuker.igcodegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    Map<String, List<ConceptConstant>> codeSystemConcepts = new HashMap<>();

    try (DirectoryStream<Path> files = Files.newDirectoryStream(packageContentDir, "*.json")) {
      for (Path file : files) {
        classify(file, codeSystems, profiles, extensions, codeSystemConcepts);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Failed to scan FHIR package directory: " + packageContentDir, e);
    }

    return new IgPackageModel(
        packageName, packageVersion, codeSystems, profiles, extensions, codeSystemConcepts);
  }

  private void classify(
      Path file,
      Map<String, String> codeSystems,
      Map<String, String> profiles,
      Map<String, String> extensions,
      Map<String, List<ConceptConstant>> codeSystemConcepts) {
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
      classifyCodeSystem(resource, constantName, url, codeSystems, codeSystemConcepts);
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

  private static void classifyCodeSystem(
      FhirResourceSummary resource,
      String constantName,
      String url,
      Map<String, String> codeSystems,
      Map<String, List<ConceptConstant>> codeSystemConcepts) {
    codeSystems.put(constantName, url);
    if (!"complete".equals(resource.content()) || resource.concept() == null) {
      return;
    }
    List<ConceptConstant> concepts = flattenConcepts(resource.concept());
    if (!concepts.isEmpty()) {
      codeSystemConcepts.put(constantName, concepts);
    }
  }

  /**
   * Flattens a CodeSystem's concept hierarchy (including both group/parent and leaf concepts) into
   * a flat, name-collision-free list, in document order.
   */
  private static List<ConceptConstant> flattenConcepts(List<FhirResourceSummary.Concept> concepts) {
    List<ConceptConstant> result = new ArrayList<>();
    Set<String> usedNames = new HashSet<>();
    flattenConcepts(concepts, result, usedNames);
    return result;
  }

  private static void flattenConcepts(
      List<FhirResourceSummary.Concept> concepts,
      List<ConceptConstant> result,
      Set<String> usedNames) {
    for (FhirResourceSummary.Concept concept : concepts) {
      String code = concept.code();
      if (code != null) {
        result.add(
            new ConceptConstant(uniqueEnumConstantName(code, usedNames), code, concept.display()));
      }
      List<FhirResourceSummary.Concept> children = concept.concept();
      if (children != null) {
        flattenConcepts(children, result, usedNames);
      }
    }
  }

  private static String uniqueEnumConstantName(String code, Set<String> usedNames) {
    String baseName = NameUtils.toEnumConstantName(code);
    String name = baseName;
    int suffix = 2;
    while (!usedNames.add(name)) {
      name = baseName + "_" + suffix;
      suffix++;
    }
    return name;
  }
}

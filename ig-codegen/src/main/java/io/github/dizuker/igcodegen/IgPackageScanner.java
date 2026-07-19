package io.github.dizuker.igcodegen;

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
import tools.jackson.databind.ObjectMapper;

/**
 * Scans a restored FHIR package directory ({@code ~/.fhir/packages/<name>#<version>/package/}, or
 * {@code node_modules/<name>/} if installed via npm) and classifies its resources into
 * CodeSystem/Profile/Extension canonical URL constants.
 */
public final class IgPackageScanner {

  private final ObjectMapper objectMapper;

  public IgPackageScanner(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Resolves the directory holding a package's resource JSON files. Tries the Firely Terminal cache
   * layout ({@code <fhirPackagesDir>/<packageName>#<packageVersion>/package}) first, then falls
   * back to the flat npm layout ({@code <fhirPackagesDir>/<packageName>}, no version segment, no
   * nested {@code package} directory) used when FHIR packages are installed via {@code npm install}
   * instead of Firely Terminal's {@code fhir restore}.
   */
  public Path resolvePackageContentDir(
      Path fhirPackagesDir, String packageName, String packageVersion) {
    Path firelyLayout =
        fhirPackagesDir.resolve(packageName + "#" + packageVersion).resolve("package");
    if (Files.isDirectory(firelyLayout)) {
      return firelyLayout;
    }
    return fhirPackagesDir.resolve(packageName);
  }

  public IgPackageModel scan(Path packageContentDir, String packageName, String packageVersion) {
    TreeMap<String, String> codeSystems = new TreeMap<>();
    TreeMap<String, String> profiles = new TreeMap<>();
    TreeMap<String, String> extensions = new TreeMap<>();
    Map<String, List<ConceptConstant>> codeSystemConcepts = new HashMap<>();
    Map<String, ExtensionValueType> extensionValueTypes = new HashMap<>();

    try (DirectoryStream<Path> files = Files.newDirectoryStream(packageContentDir, "*.json")) {
      for (Path file : files) {
        classify(file, codeSystems, profiles, extensions, codeSystemConcepts, extensionValueTypes);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Failed to scan FHIR package directory: " + packageContentDir, e);
    }

    return new IgPackageModel(
        packageName,
        packageVersion,
        codeSystems,
        profiles,
        extensions,
        codeSystemConcepts,
        extensionValueTypes);
  }

  private void classify(
      Path file,
      Map<String, String> codeSystems,
      Map<String, String> profiles,
      Map<String, String> extensions,
      Map<String, List<ConceptConstant>> codeSystemConcepts,
      Map<String, ExtensionValueType> extensionValueTypes) {
    var resource = objectMapper.readValue(file.toFile(), FhirResourceSummary.class);

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
      extensionValueTypes.put(constantName, extensionValueType(resource));
      return;
    }

    if ("resource".equals(kind) && "constraint".equals(derivation)) {
      String version = resource.version();
      profiles.put(constantName, version == null ? url : url + "|" + version);
    }
  }

  /**
   * Inspects an extension's {@code snapshot.element} for {@code Extension.value[x]} to determine
   * the shape its generated factory method's value parameter should take. See {@link
   * ExtensionValueType}.
   */
  private static ExtensionValueType extensionValueType(FhirResourceSummary resource) {
    if (resource.snapshot() == null || resource.snapshot().element() == null) {
      return ExtensionValueType.NONE;
    }
    for (FhirResourceSummary.Element element : resource.snapshot().element()) {
      if (!"Extension.value[x]".equals(element.path())) {
        continue;
      }
      List<FhirResourceSummary.ElementType> types = element.type();
      if ("0".equals(element.max()) || types == null || types.isEmpty()) {
        return ExtensionValueType.NONE;
      }
      if (types.size() > 1) {
        return ExtensionValueType.CHOICE;
      }
      String code = types.get(0).code();
      return code == null ? ExtensionValueType.NONE : ExtensionValueType.fixed(code);
    }
    return ExtensionValueType.NONE;
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

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
import org.jspecify.annotations.Nullable;
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
    List<FhirResourceSummary> resources = readResources(packageContentDir);

    TreeMap<String, String> codeSystems = new TreeMap<>();
    TreeMap<String, String> profiles = new TreeMap<>();
    TreeMap<String, String> extensions = new TreeMap<>();
    Map<String, List<ConceptConstant>> codeSystemConcepts = new HashMap<>();
    Map<String, ExtensionValueType> extensionValueTypes = new HashMap<>();

    for (FhirResourceSummary resource : resources) {
      if (!"CodeSystem".equals(resource.resourceType())) {
        continue;
      }
      String id = resource.id();
      String url = resource.url();
      if (id == null || url == null) {
        continue;
      }
      classifyCodeSystem(
          resource, NameUtils.toConstantName(id), url, codeSystems, codeSystemConcepts);
    }

    // Built up front (a second pass over `resources`) so an extension's `binding.valueSet` can be
    // resolved to a CodeSystem regardless of which file the ValueSet happens to be defined in.
    Map<String, String> codeSystemUrlByValueSetUrl = indexSingleCodeSystemValueSets(resources);

    for (FhirResourceSummary resource : resources) {
      if (!"StructureDefinition".equals(resource.resourceType())) {
        continue;
      }
      String id = resource.id();
      String url = resource.url();
      if (id == null || url == null) {
        continue;
      }
      classifyStructureDefinition(
          resource,
          NameUtils.toConstantName(id),
          url,
          profiles,
          extensions,
          extensionValueTypes,
          codeSystemUrlByValueSetUrl);
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

  private List<FhirResourceSummary> readResources(Path packageContentDir) {
    List<FhirResourceSummary> resources = new ArrayList<>();
    try (DirectoryStream<Path> files = Files.newDirectoryStream(packageContentDir, "*.json")) {
      for (Path file : files) {
        resources.add(objectMapper.readValue(file.toFile(), FhirResourceSummary.class));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Failed to scan FHIR package directory: " + packageContentDir, e);
    }
    return resources;
  }

  private static void classifyStructureDefinition(
      FhirResourceSummary resource,
      String constantName,
      String url,
      Map<String, String> profiles,
      Map<String, String> extensions,
      Map<String, ExtensionValueType> extensionValueTypes,
      Map<String, String> codeSystemUrlByValueSetUrl) {
    String kind = resource.kind();
    String derivation = resource.derivation();
    if ("logical".equals(kind) || "specialization".equals(derivation)) {
      return;
    }

    if ("complex-type".equals(kind)
        && "constraint".equals(derivation)
        && "Extension".equals(resource.type())) {
      extensions.put(constantName, url);
      extensionValueTypes.put(
          constantName, extensionValueType(resource, codeSystemUrlByValueSetUrl));
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
  private static ExtensionValueType extensionValueType(
      FhirResourceSummary resource, Map<String, String> codeSystemUrlByValueSetUrl) {
    if (resource.snapshot() == null || resource.snapshot().element() == null) {
      return ExtensionValueType.NONE;
    }
    List<FhirResourceSummary.Element> elements = resource.snapshot().element();
    for (FhirResourceSummary.Element element : elements) {
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
      if (code == null) {
        return ExtensionValueType.NONE;
      }
      String boundCodeSystemUrl =
          boundCodeSystemUrl(elements, element, code, codeSystemUrlByValueSetUrl);
      return boundCodeSystemUrl == null
          ? ExtensionValueType.fixed(code)
          : ExtensionValueType.boundCoding(code, boundCodeSystemUrl);
    }
    return ExtensionValueType.NONE;
  }

  /**
   * For a {@code CodeableConcept}- or {@code Coding}-typed {@code value[x]}, determines whether its
   * {@code Coding.system} is pinned to one CodeSystem - either directly, via a {@code fixedUri} on
   * its (only, unsliced) nested {@code Coding.system} element, or indirectly, via a {@code
   * required}-strength {@code binding} to a ValueSet that itself draws from exactly one CodeSystem
   * (see {@link #indexSingleCodeSystemValueSets}). Returns {@code null} for any other value type,
   * or if neither pattern applies (e.g. an {@code extensible} binding, or a ValueSet composed from
   * more than one CodeSystem).
   */
  private static @Nullable String boundCodeSystemUrl(
      List<FhirResourceSummary.Element> elements,
      FhirResourceSummary.Element valueElement,
      String valueTypeCode,
      Map<String, String> codeSystemUrlByValueSetUrl) {
    if (!"CodeableConcept".equals(valueTypeCode) && !"Coding".equals(valueTypeCode)) {
      return null;
    }
    String systemPath =
        "CodeableConcept".equals(valueTypeCode)
            ? "Extension.value[x].coding.system"
            : "Extension.value[x].system";
    for (FhirResourceSummary.Element element : elements) {
      if (systemPath.equals(element.path()) && element.fixedUri() != null) {
        return element.fixedUri();
      }
    }

    FhirResourceSummary.Binding binding = valueElement.binding();
    if (binding == null || !"required".equals(binding.strength()) || binding.valueSet() == null) {
      return null;
    }
    // Canonical references may carry a `|version` suffix; the ValueSet index is keyed by bare url.
    String valueSetUrl = binding.valueSet();
    int versionSeparator = valueSetUrl.indexOf('|');
    String bareValueSetUrl =
        versionSeparator < 0 ? valueSetUrl : valueSetUrl.substring(0, versionSeparator);
    return codeSystemUrlByValueSetUrl.get(bareValueSetUrl);
  }

  /**
   * Maps each ValueSet URL that draws from exactly one CodeSystem - one {@code compose.include}
   * entry with a {@code system} and no nested {@code valueSet} imports, and no {@code
   * compose.exclude} - to that CodeSystem's URL. A {@code concept}/{@code filter} restriction on
   * the include is ignored: the ValueSet may only permit a subset of that CodeSystem's codes, but
   * every code it does permit still comes from the one system, which is all that's needed to safely
   * type a bound extension value to that CodeSystem's generated enum (itself a superset).
   */
  private static Map<String, String> indexSingleCodeSystemValueSets(
      List<FhirResourceSummary> resources) {
    Map<String, String> codeSystemUrlByValueSetUrl = new HashMap<>();
    for (FhirResourceSummary resource : resources) {
      if (!"ValueSet".equals(resource.resourceType())) {
        continue;
      }
      String valueSetUrl = resource.url();
      String codeSystemUrl = singleCodeSystemUrl(resource.compose());
      if (valueSetUrl != null && codeSystemUrl != null) {
        codeSystemUrlByValueSetUrl.put(valueSetUrl, codeSystemUrl);
      }
    }
    return codeSystemUrlByValueSetUrl;
  }

  private static @Nullable String singleCodeSystemUrl(
      FhirResourceSummary.@Nullable Compose compose) {
    if (compose == null) {
      return null;
    }
    List<FhirResourceSummary.ComposeInclude> excludes = compose.exclude();
    if (excludes != null && !excludes.isEmpty()) {
      return null;
    }
    List<FhirResourceSummary.ComposeInclude> includes = compose.include();
    if (includes == null || includes.size() != 1) {
      return null;
    }
    FhirResourceSummary.ComposeInclude onlyInclude = includes.get(0);
    List<String> nestedValueSets = onlyInclude.valueSet();
    if (nestedValueSets != null && !nestedValueSets.isEmpty()) {
      return null;
    }
    return onlyInclude.system();
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

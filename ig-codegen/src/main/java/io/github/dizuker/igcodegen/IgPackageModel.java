package io.github.dizuker.igcodegen;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Classified canonical URLs of a single FHIR IG package, keyed by the generated Java constant name
 * (e.g. {@code MII_CS_ONKO_INTENTION}).
 *
 * <p>{@code codeSystemConcepts} holds, for each entry in {@code codeSystems} that is a locally
 * defined ({@code content == "complete"}) CodeSystem, the flattened list of its concepts -
 * everything needed to additionally render a Java enum. Entries absent from this map (e.g. external
 * terminologies like SNOMED CT or LOINC, which FHIR doesn't redistribute inline) only get the plain
 * URL constant.
 *
 * <p>{@code extensionValueTypes} holds, for each entry in {@code extensions}, the shape of its
 * {@code value[x]} - everything needed to render a factory method that returns a HAPI {@code
 * Extension} missing only the value. See {@link ExtensionValueType}.
 */
public record IgPackageModel(
    String packageName,
    String packageVersion,
    SortedMap<String, String> codeSystems,
    SortedMap<String, String> profiles,
    SortedMap<String, String> extensions,
    Map<String, List<ConceptConstant>> codeSystemConcepts,
    Map<String, ExtensionValueType> extensionValueTypes) {

  public IgPackageModel {
    codeSystems = Collections.unmodifiableSortedMap(codeSystems);
    profiles = Collections.unmodifiableSortedMap(profiles);
    extensions = Collections.unmodifiableSortedMap(extensions);
    codeSystemConcepts = Map.copyOf(codeSystemConcepts);
    extensionValueTypes = Map.copyOf(extensionValueTypes);
  }
}

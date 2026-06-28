package io.github.dizuker.igcodegen;

import java.util.Collections;
import java.util.SortedMap;

/**
 * Classified canonical URLs of a single FHIR IG package, keyed by the generated Java constant name
 * (e.g. {@code MII_CS_ONKO_INTENTION}).
 */
public record IgPackageModel(
    String packageName,
    String packageVersion,
    SortedMap<String, String> codeSystems,
    SortedMap<String, String> profiles,
    SortedMap<String, String> extensions) {

  public IgPackageModel {
    codeSystems = Collections.unmodifiableSortedMap(codeSystems);
    profiles = Collections.unmodifiableSortedMap(profiles);
    extensions = Collections.unmodifiableSortedMap(extensions);
  }
}

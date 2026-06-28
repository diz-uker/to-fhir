package io.github.dizuker.igcodegen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** The subset of a FHIR resource JSON file's fields needed to classify it. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FhirResourceSummary(
    @Nullable String resourceType,
    @Nullable String id,
    @Nullable String url,
    @Nullable String version,
    @Nullable String kind,
    @Nullable String derivation,
    @Nullable String type,
    @Nullable String content,
    @Nullable List<Concept> concept) {

  /** A CodeSystem.concept entry; {@code concept} holds nested child concepts, if any. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Concept(
      @Nullable String code, @Nullable String display, @Nullable List<Concept> concept) {}
}

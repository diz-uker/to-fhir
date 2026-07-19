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
    @Nullable List<Concept> concept,
    @Nullable Snapshot snapshot) {

  /** A CodeSystem.concept entry; {@code concept} holds nested child concepts, if any. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Concept(
      @Nullable String code, @Nullable String display, @Nullable List<Concept> concept) {}

  /** A StructureDefinition.snapshot, reduced to what's needed to inspect {@code value[x]}. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Snapshot(@Nullable List<Element> element) {}

  /** A snapshot element, reduced to what's needed to inspect {@code Extension.value[x]}. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Element(
      @Nullable String path, @Nullable String max, @Nullable List<ElementType> type) {}

  /** One entry of an element's {@code type} array, e.g. {@code {"code": "CodeableConcept"}}. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ElementType(@Nullable String code) {}
}

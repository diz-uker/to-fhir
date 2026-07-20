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
    @Nullable Snapshot snapshot,
    @Nullable Compose compose) {

  /** A CodeSystem.concept entry; {@code concept} holds nested child concepts, if any. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Concept(
      @Nullable String code, @Nullable String display, @Nullable List<Concept> concept) {}

  /** A StructureDefinition.snapshot, reduced to what's needed to inspect {@code value[x]}. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Snapshot(@Nullable List<Element> element) {}

  /**
   * A snapshot element, reduced to what's needed to inspect {@code Extension.value[x]} and, for a
   * {@code CodeableConcept}/{@code Coding}-typed value, whether its {@code system} is pinned to one
   * fixed CodeSystem, either directly ({@code fixedUri}) or via a {@code required} {@code binding}
   * to a ValueSet that itself draws from a single CodeSystem.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Element(
      @Nullable String path,
      @Nullable String max,
      @Nullable List<ElementType> type,
      @Nullable String fixedUri,
      @Nullable Binding binding) {}

  /** One entry of an element's {@code type} array, e.g. {@code {"code": "CodeableConcept"}}. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ElementType(@Nullable String code) {}

  /** An element's terminology binding, e.g. {@code {"strength": "required", "valueSet": "..."}}. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Binding(@Nullable String strength, @Nullable String valueSet) {}

  /** A ValueSet.compose, reduced to what's needed to tell whether it draws from one CodeSystem. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Compose(
      @Nullable List<ComposeInclude> include, @Nullable List<ComposeInclude> exclude) {}

  /**
   * One entry of {@code ValueSet.compose.include} (or {@code .exclude}). {@code valueSet} holds
   * canonical references to other ValueSets being imported wholesale, as opposed to {@code system}
   * pulling concepts directly from one CodeSystem.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ComposeInclude(
      @Nullable String system,
      @Nullable List<Object> concept,
      @Nullable List<Object> filter,
      @Nullable List<String> valueSet) {}
}

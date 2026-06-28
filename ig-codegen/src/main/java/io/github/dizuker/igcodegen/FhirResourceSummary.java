package io.github.dizuker.igcodegen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @Nullable String type) {}

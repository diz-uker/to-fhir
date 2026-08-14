package io.github.dizuker.igcodegen;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** The classified unique identifiers of a single FHIR NamingSystem, grouped by their type. */
public record NamingSystemUniqueIds(
    @Nullable String description, Map<String, List<String>> byType) {}

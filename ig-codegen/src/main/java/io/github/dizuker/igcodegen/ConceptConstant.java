package io.github.dizuker.igcodegen;

import org.jspecify.annotations.Nullable;

/** A single, already name-sanitized CodeSystem concept, ready to render as an enum constant. */
public record ConceptConstant(String constantName, String code, @Nullable String display) {}

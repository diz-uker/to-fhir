package io.github.dizuker.igcodegen;

import org.jspecify.annotations.Nullable;

/**
 * The {@code Extension.value[x]} shape of a FHIR extension's {@code StructureDefinition},
 * determining the parameter type of its generated factory method.
 *
 * <ul>
 *   <li>{@code fhirTypeCode} set, {@code choice == false}: a simple extension with exactly one
 *       {@code value[x]} type (e.g. {@code "CodeableConcept"}) - the factory method takes that
 *       type.
 *   <li>{@code fhirTypeCode == null}, {@code choice == true}: a simple extension whose {@code
 *       value[x]} allows more than one type - the factory method falls back to the generic HAPI
 *       {@code Type}.
 *   <li>{@code fhirTypeCode == null}, {@code choice == false}: a complex extension with no {@code
 *       value[x]} of its own (only nested sub-extensions) - the factory method takes no value.
 * </ul>
 */
public record ExtensionValueType(@Nullable String fhirTypeCode, boolean choice) {

  public static final ExtensionValueType NONE = new ExtensionValueType(null, false);
  public static final ExtensionValueType CHOICE = new ExtensionValueType(null, true);

  public static ExtensionValueType fixed(String fhirTypeCode) {
    return new ExtensionValueType(fhirTypeCode, false);
  }
}

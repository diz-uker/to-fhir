package io.github.dizuker.igcodegen;

import org.jspecify.annotations.Nullable;

/**
 * The {@code Extension.value[x]} shape of a FHIR extension's {@code StructureDefinition},
 * determining the parameter type of its generated factory method.
 *
 * <ul>
 *   <li>{@code fhirTypeCode} set, {@code choice == false}, {@code boundCodeSystemUrl == null}: a
 *       simple extension with exactly one {@code value[x]} type (e.g. {@code "CodeableConcept"}) -
 *       the factory method takes that type.
 *   <li>{@code fhirTypeCode} either {@code "CodeableConcept"} or {@code "Coding"}, {@code
 *       boundCodeSystemUrl} set: additionally, the (only) {@code Coding.system} nested under {@code
 *       value[x]} is pinned to one fixed CodeSystem URL via {@code fixedUri} - if that CodeSystem
 *       has a generated concept enum in the same package, the factory method takes the enum type
 *       directly instead of the generic {@code CodeableConcept}/{@code Coding}.
 *   <li>{@code fhirTypeCode == null}, {@code choice == true}: a simple extension whose {@code
 *       value[x]} allows more than one type - the factory method falls back to the generic HAPI
 *       {@code Type}.
 *   <li>{@code fhirTypeCode == null}, {@code choice == false}: a complex extension with no {@code
 *       value[x]} of its own (only nested sub-extensions) - the factory method takes no value.
 * </ul>
 */
public record ExtensionValueType(
    @Nullable String fhirTypeCode, boolean choice, @Nullable String boundCodeSystemUrl) {

  public static final ExtensionValueType NONE = new ExtensionValueType(null, false, null);
  public static final ExtensionValueType CHOICE = new ExtensionValueType(null, true, null);

  public static ExtensionValueType fixed(String fhirTypeCode) {
    return new ExtensionValueType(fhirTypeCode, false, null);
  }

  public static ExtensionValueType boundCoding(String fhirTypeCode, String codeSystemUrl) {
    return new ExtensionValueType(fhirTypeCode, false, codeSystemUrl);
  }
}

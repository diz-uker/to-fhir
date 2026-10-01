package io.github.dizuker.igcodegen;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Reads a JSON string, and yields {@code null} for any other shape rather than failing.
 *
 * <p>FHIR reuses the same field name across resource types with incompatible shapes, so a field
 * that is a plain {@code code} on the resource type {@link FhirResourceSummary} is modelled for can
 * be an object on another resource type that happens to share the field name - e.g. {@code
 * StructureDefinition.type} is a {@code code} while {@code NamingSystem.type} is a {@code
 * CodeableConcept}. Reading the foreign shape as absent keeps one stray resource from failing the
 * scan of an entire package.
 */
final class CodeOrNullDeserializer extends ValueDeserializer<String> {

  @Override
  public @Nullable String deserialize(JsonParser parser, DeserializationContext context) {
    if (parser.currentToken() == JsonToken.VALUE_STRING) {
      return parser.getString();
    }
    parser.skipChildren();
    return null;
  }
}

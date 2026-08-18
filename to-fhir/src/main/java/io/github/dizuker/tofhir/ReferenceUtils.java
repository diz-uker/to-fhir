package io.github.dizuker.tofhir;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

/** Utility class for creating FHIR References to resources. */
public class ReferenceUtils {
  private ReferenceUtils() {
    // Utility class, prevent instantiation
  }

  /**
   * Creates a FHIR Reference to the given resource. The reference will be in the format
   * ResourceType/Id. The resource must have a non-blank ID set.
   *
   * @param resource the FHIR resource to create a reference to
   * @return a Reference pointing to the given resource
   */
  public static Reference createReferenceTo(Resource resource) {
    Validate.notBlank(resource.getId());
    return new Reference(
        resource.getResourceType().name() + "/" + resource.getIdElement().getIdPart());
  }

  /**
   * Creates a FHIR Reference to a resource with the given identifier. The reference will be in the
   * format ResourceType/sha256(identifier.value). The identifier must have a non-blank system and
   * value set.
   *
   * @param identifier an identifier of the FHIR resource to create a reference to
   * @param resourceType the FHIR ResourceType to include in the reference
   * @return a Reference pointing to the given resource
   */
  public static Reference createReferenceTo(Identifier identifier, ResourceType resourceType) {
    return new Reference(IdUtils.fromIdentifier(identifier, resourceType));
  }
}

package io.github.dizuker.tofhir;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

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
}

package io.github.dizuker.tofhir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Resource;

/**
 * Builder for creating FHIR transaction bundles. By default, using the update-as-create approach.
 */
public class TransactionBuilder {
  private BundleType bundleType = BundleType.TRANSACTION;
  private List<Resource> resources = new ArrayList<>();
  private boolean useFirstEntryResourceIdAsBundleId = false;
  private Optional<String> id = Optional.empty();
  private boolean failOnDuplicateEntries = false;

  /** Creates a new TransactionBuilder with the default type of TRANSACTION. */
  public TransactionBuilder() {}

  /**
   * Sets the bundle type.
   *
   * @param bundleType the type of bundle to build
   * @return this builder instance for chaining
   */
  public TransactionBuilder withType(BundleType bundleType) {
    this.bundleType = bundleType;
    return this;
  }

  /**
   * Configures the builder to use the ID of the first entry's resource as the bundle ID.
   *
   * @return this builder instance for chaining
   */
  public TransactionBuilder withUseFirstEntryResourceIdAsBundleId() {
    this.useFirstEntryResourceIdAsBundleId = true;
    return this;
  }

  /**
   * Configures the builder to throw an exception if multiple resources with the same ID are added
   * when the transaction is built.
   *
   * @return this builder instance for chaining
   */
  public TransactionBuilder failOnDuplicateEntries() {
    this.failOnDuplicateEntries = true;
    return this;
  }

  /**
   * Adds a FHIR resource to the transaction bundle.
   *
   * @param resource the FHIR resource to add to the bundle
   * @return this builder instance for chaining
   */
  public TransactionBuilder addEntry(Resource resource) {
    this.resources.add(resource);
    return this;
  }

  /**
   * Adds a list of FHIR resources to the transaction bundle.
   *
   * @param resources the list of FHIR resources to add to the bundle
   * @return this builder instance for chaining
   */
  public TransactionBuilder addEntries(List<? extends Resource> resources) {
    this.resources.addAll(resources);
    return this;
  }

  /**
   * Sets the ID of the bundle. Takes precedence over `useFirstEntryResourceIdAsBundleId` if both
   * are set.
   *
   * @param id the ID to set for the bundle
   * @return this builder instance for chaining
   */
  public TransactionBuilder withId(IIdType id) {
    return this.withId(id.getIdPart());
  }

  /**
   * Sets the ID of the bundle. Takes precedence over `useFirstEntryResourceIdAsBundleId` if both
   * are set.
   *
   * @param id the ID to set for the bundle
   * @return this builder instance for chaining
   */
  public TransactionBuilder withId(String id) {
    this.id = Optional.of(id);
    return this;
  }

  /**
   * Builds and returns a FHIR Bundle with the configured type.
   *
   * @return a new Bundle instance with the configured type
   * @throws IllegalArgumentException if failOnDuplicateEntries is enabled and duplicate resource
   *     IDs are found
   */
  public Bundle build() {
    var bundle = new Bundle();
    bundle.setType(bundleType);

    if (useFirstEntryResourceIdAsBundleId && !resources.isEmpty()) {
      var firstResourceId = resources.getFirst().getIdElement().getIdPart();
      if (firstResourceId != null && !firstResourceId.isEmpty()) {
        bundle.setId(firstResourceId);
      } else {
        throw new IllegalArgumentException(
            "First resource must have an ID for useFirstEntryResourceIdAsBundleId option");
      }
    }

    if (this.id.isPresent()) {
      bundle.setId(this.id.get());
    }

    var seen = new HashSet<String>();

    for (var resource : resources) {
      var resourceType = resource.getResourceType().name();
      var id = resource.getIdElement().getIdPart();

      if (id == null || id.isEmpty()) {
        throw new IllegalArgumentException(
            "Resource must have an ID for update-as-create (PUT) method");
      }

      var url = resourceType + "/" + id;

      if (failOnDuplicateEntries && !seen.add(url)) {
        throw new IllegalArgumentException("Duplicate resource added:  " + url);
      }

      var entry = bundle.addEntry();
      entry.setResource(resource);
      entry.setFullUrl(url);
      entry.getRequest().setMethod(HTTPVerb.PUT).setUrl(url);
    }

    return bundle;
  }
}

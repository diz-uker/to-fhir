package io.github.dizuker.tofhir;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Resource;

/** Builder for creating FHIR transaction bundles. */
public class TransactionBuilder {
  private BundleType bundleType = BundleType.TRANSACTION;
  private boolean updateAsCreate = true;
  private List<Resource> resources = new ArrayList<>();
  private boolean useFirstEntryResourceIdAsBundleId = false;
  private String id = null;

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
   * Sets whether resources should be added as update-as-create (PUT) or conditional creates (POST
   * with If-None-Exist). Defaults to conditional creates.
   *
   * @param updateAsCreate true for update-as-create, false for conditional creates
   * @return this builder instance for chaining
   */
  public TransactionBuilder withUpdateAsCreate(boolean updateAsCreate) {
    this.updateAsCreate = updateAsCreate;
    return this;
  }

  /**
   * Sets whether the first entry's resource ID should be used as the Bundle ID. Defaults to false.
   *
   * @param useFirstEntryResourceIdAsBundleId true to use the first entry's resource ID as the
   *     Bundle ID, false to not set a Bundle ID
   * @return this builder instance for chaining
   */
  public TransactionBuilder withUseFirstEntryResourceIdAsBundleId(
      boolean useFirstEntryResourceIdAsBundleId) {
    this.useFirstEntryResourceIdAsBundleId = useFirstEntryResourceIdAsBundleId;
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
    this.id = id;
    return this;
  }

  /**
   * Builds and returns a FHIR Bundle with the configured type.
   *
   * @return a new Bundle instance with the configured type
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

    if (this.id != null) {
      bundle.setId(this.id);
    }

    for (var resource : resources) {
      var entry = bundle.addEntry();
      entry.setResource(resource);

      var resourceType = resource.getResourceType().name();
      var id = resource.getIdElement().getIdPart();
      if (updateAsCreate) {
        if (id == null || id.isEmpty()) {
          throw new IllegalArgumentException(
              "Resource must have an ID for update-as-create (PUT) method");
        }
        var url = resourceType + "/" + id;
        entry.setFullUrl(url);
        entry.getRequest().setMethod(HTTPVerb.PUT).setUrl(url);
      } else {
        // XXX: we could set a deterministic urn:uuid: (128 bit) fullUrl
        // based on resource.id (256 bit)
        entry.getRequest().setMethod(HTTPVerb.POST).setUrl(resourceType);
      }
    }

    return bundle;
  }
}

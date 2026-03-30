package io.github.dizuker.tofhir;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.jspecify.annotations.NonNull;

/**
 * Builder for creating FHIR transaction bundles. By default, using the update-as-create approach.
 */
public class TransactionBuilder {
  private BundleType bundleType = BundleType.TRANSACTION;
  private List<Resource> resources = new ArrayList<>();
  private List<Reference> resourcesToDelete = new ArrayList<>();
  private Optional<String> bundleId = Optional.empty();
  private boolean failOnDuplicateEntries = false;
  private boolean isProvenanceEnabled = false;
  private Reference provenanceWho = null;
  private Reference provenanceWhat = null;

  /** Creates a new TransactionBuilder with the default type of TRANSACTION. */
  public TransactionBuilder() {
    // Default constructor with default bundle type
  }

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
   * Adds a reference to a resource that should be deleted as part of the transaction.
   *
   * @param resource a reference to the resource to delete
   * @return this builder instance for chaining
   */
  public TransactionBuilder addDeleteEntry(Reference resource) {
    this.resourcesToDelete.add(resource);
    return this;
  }

  /**
   * Adds a list of references to resources that should be deleted as part of the transaction.
   *
   * @param resources a list of references to the resources to delete
   * @return this builder instance for chaining
   */
  public TransactionBuilder addDeleteEntries(List<? extends Reference> resources) {
    for (var r : resources) {
      this.addDeleteEntry(r);
    }
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
    this.bundleId = Optional.of(id);
    return this;
  }

  /**
   * Enables the inclusion of a Provenance resource in the transaction bundle, with the specified
   * `Provenance.entity.what` and `Provenance.agent.who` references. If the bundle contains both
   * delete and update/create entries, two Provenance resources wil be included.
   *
   * <p>The `Provenance.id` is built from the hash of the `who` and `what` references.
   *
   * @param who a reference to the resource that are the source of the transformation. For delete
   *     entries, this is automatically set to the resource being deleted instead.
   * @param what a reference to the agent responsible for the transformation or deletion. This is
   *     typically the transformation service itself.
   * @return
   */
  public TransactionBuilder withProvenance(@NonNull Reference who, @NonNull Reference what) {
    this.isProvenanceEnabled = true;
    this.provenanceWho = who;
    this.provenanceWhat = what;
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

    if (this.bundleId.isPresent()) {
      bundle.setId(this.bundleId.get());
    }

    var seen = new HashSet<String>();

    for (var resource : resources) {
      var resourceId = resource.getIdElement().getIdPart();

      Validate.notBlank(resourceId);

      var url = ReferenceUtils.createReferenceTo(resource).getReference();

      if (failOnDuplicateEntries && !seen.add(url)) {
        throw new IllegalArgumentException("Duplicate resource added:  " + url);
      }

      bundle
          .addEntry()
          .setResource(resource)
          .setFullUrl(url)
          .getRequest()
          .setMethod(HTTPVerb.PUT)
          .setUrl(url);
    }

    for (var toDeleteReference : resourcesToDelete) {
      var entry = bundle.addEntry();
      entry.getRequest().setMethod(HTTPVerb.DELETE).setUrl(toDeleteReference.getReference());
    }

    if (this.isProvenanceEnabled) {
      if (!resources.isEmpty()) {
        var provenance = buildCreateProvenance();
        var url = ReferenceUtils.createReferenceTo(provenance).getReference();
        bundle
            .addEntry()
            .setResource(provenance)
            .setFullUrl(url)
            .getRequest()
            .setMethod(HTTPVerb.PUT)
            .setUrl(url);
      }

      if (!resourcesToDelete.isEmpty()) {
        var provenance = buildDeleteProvenance();
        var url = ReferenceUtils.createReferenceTo(provenance).getReference();
        bundle
            .addEntry()
            .setResource(provenance)
            .setFullUrl(url)
            .getRequest()
            .setMethod(HTTPVerb.PUT)
            .setUrl(url);
      }
    }

    return bundle;
  }

  private Provenance buildCreateProvenance() {
    var provenance = new Provenance();

    var id = DigestUtils.sha256Hex("create-" + getProvenanceIdString());
    provenance.setId(id);

    var now = Date.from(Instant.now());
    provenance.setOccurred(new DateTimeType(now));
    provenance.setRecorded(now);

    var targets = this.resources.stream().map(ReferenceUtils::createReferenceTo).toList();

    provenance.setTarget(targets);

    provenance.setActivity(
        new CodeableConcept(
            new Coding(
                "http://terminology.hl7.org/CodeSystem/v3-DataOperation", "CREATE", "create")));

    provenance
        .addAgent()
        .setType(
            new CodeableConcept(
                new Coding(
                    "http://terminology.hl7.org/CodeSystem/provenance-participant-type",
                    "assembler",
                    "Assembler")))
        .addRole(
            new CodeableConcept(
                new Coding(
                    "http://terminology.hl7.org/CodeSystem/v3-ParticipationType", "AUT", "author")))
        .setWho(this.provenanceWho);

    provenance.addEntity().setRole(ProvenanceEntityRole.SOURCE).setWhat(provenanceWhat);

    return provenance;
  }

  private Provenance buildDeleteProvenance() {
    var provenance = new Provenance();

    var id = DigestUtils.sha256Hex("delete-" + getProvenanceIdString());
    provenance.setId(id);

    var now = Date.from(Instant.now());
    provenance.setOccurred(new DateTimeType(now));
    provenance.setRecorded(now);

    provenance.setTarget(this.resourcesToDelete);

    provenance.setActivity(
        new CodeableConcept(
            new Coding(
                "http://terminology.hl7.org/CodeSystem/v3-DataOperation", "DELETE", "delete")));

    provenance
        .addAgent()
        .setType(
            new CodeableConcept(
                new Coding(
                    "http://terminology.hl7.org/CodeSystem/provenance-participant-type",
                    "performer",
                    "Performer")))
        .setWho(this.provenanceWho);

    for (var toDelete : resourcesToDelete) {
      provenance.addEntity().setRole(ProvenanceEntityRole.REMOVAL).setWhat(toDelete);
    }

    return provenance;
  }

  private String getProvenanceIdString() {
    var who = "";
    if (!StringUtils.isBlank(this.provenanceWho.getReference())) {
      who = this.provenanceWho.getReference();
    } else if (this.provenanceWho.getIdentifier() != null
        && this.provenanceWho.getIdentifier().getValue() != null) {
      who = this.provenanceWho.getIdentifier().getValue();
    } else if (this.provenanceWho.getDisplay() != null) {
      who = this.provenanceWho.getDisplay();
    } else {
      throw new IllegalArgumentException(
          "Invalid provenanceWho reference. Either reference, identifier or display must be provided.");
    }

    var what = "";
    if (!StringUtils.isBlank(this.provenanceWhat.getReference())) {
      what = this.provenanceWhat.getReference();
    } else if (this.provenanceWhat.getIdentifier() != null
        && this.provenanceWhat.getIdentifier().getValue() != null) {
      what = this.provenanceWhat.getIdentifier().getValue();
    } else if (this.provenanceWhat.getDisplay() != null) {
      what = this.provenanceWhat.getDisplay();
    } else {
      throw new IllegalArgumentException(
          "Invalid provenanceWhat reference. Either reference, identifier or display must be provided.");
    }

    return who + "-" + what;
  }
}

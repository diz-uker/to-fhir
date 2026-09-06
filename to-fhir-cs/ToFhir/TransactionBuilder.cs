using System.Security.Cryptography;
using System.Text;
using Hl7.Fhir.Model;

namespace ToFhir;

/// <summary>
/// Builder for creating FHIR transaction bundles. By default, using the update-as-create approach.
/// </summary>
public sealed class TransactionBuilder
{
    private const string DataOperationSystem =
        "http://terminology.hl7.org/CodeSystem/v3-DataOperation";
    private const string ParticipantTypeSystem =
        "http://terminology.hl7.org/CodeSystem/provenance-participant-type";
    private const string ParticipationTypeSystem =
        "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";

    private readonly List<Resource> _resources = [];
    private readonly List<ResourceReference> _resourcesToDelete = [];

    private Bundle.BundleType _bundleType = Bundle.BundleType.Transaction;
    private string? _bundleId;
    private string? _fullUrlBase;
    private bool _failOnDuplicateEntries;
    private ResourceReference? _provenanceWho;
    private ResourceReference? _provenanceWhat;
    private Device? _provenanceDevice;

    private bool IsProvenanceEnabled => _provenanceWho is not null && _provenanceWhat is not null;

    /// <summary>Sets the bundle type.</summary>
    /// <param name="bundleType">The type of bundle to build.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder WithType(Bundle.BundleType bundleType)
    {
        _bundleType = bundleType;
        return this;
    }

    /// <summary>
    /// Configures the builder to throw if multiple resources with the same id are added when the
    /// transaction is built.
    /// </summary>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder FailOnDuplicateEntries()
    {
        _failOnDuplicateEntries = true;
        return this;
    }

    /// <summary>Adds a FHIR resource to the transaction bundle.</summary>
    /// <param name="resource">The FHIR resource to add to the bundle.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder AddEntry(Resource resource)
    {
        ArgumentNullException.ThrowIfNull(resource);
        _resources.Add(resource);
        return this;
    }

    /// <summary>Adds FHIR resources to the transaction bundle.</summary>
    /// <param name="resources">The FHIR resources to add to the bundle.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder AddEntries(params Resource[] resources) =>
        AddEntries((IEnumerable<Resource>)resources);

    /// <summary>Adds a sequence of FHIR resources to the transaction bundle.</summary>
    /// <param name="resources">The FHIR resources to add to the bundle.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder AddEntries(IEnumerable<Resource> resources)
    {
        ArgumentNullException.ThrowIfNull(resources);
        _resources.AddRange(resources);
        return this;
    }

    /// <summary>Adds a reference to a resource that should be deleted as part of the transaction.</summary>
    /// <param name="resource">A reference to the resource to delete.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder AddDeleteEntry(ResourceReference resource)
    {
        ArgumentNullException.ThrowIfNull(resource);
        _resourcesToDelete.Add(resource);
        return this;
    }

    /// <summary>Adds references to resources that should be deleted as part of the transaction.</summary>
    /// <param name="resources">References to the resources to delete.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder AddDeleteEntries(params ResourceReference[] resources) =>
        AddDeleteEntries((IEnumerable<ResourceReference>)resources);

    /// <summary>Adds a sequence of references to resources that should be deleted.</summary>
    /// <param name="resources">References to the resources to delete.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder AddDeleteEntries(IEnumerable<ResourceReference> resources)
    {
        ArgumentNullException.ThrowIfNull(resources);
        _resourcesToDelete.AddRange(resources);
        return this;
    }

    /// <summary>Sets the id of the bundle.</summary>
    /// <param name="id">The id to set for the bundle.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder WithId(string id)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(id);
        _bundleId = id;
        return this;
    }

    /// <summary>
    /// Sets an absolute base URL used to build each entry's <c>fullUrl</c> as
    /// <c>&lt;baseUrl&gt;/&lt;ResourceType&gt;/&lt;id&gt;</c>, making the fullUrl absolute as FHIR
    /// requires. Plain <c>ResourceType/id</c> references used elsewhere in the bundle (e.g. in a
    /// <c>Provenance.target</c>) still resolve correctly against it, since those match by the tail
    /// of a hierarchical fullUrl. The base URL does not need to be a real, dereferenceable server
    /// endpoint.
    /// </summary>
    /// <param name="baseUrl">
    /// An absolute base URL, e.g. <c>https://example.org/fhir</c> (a trailing slash is optional).
    /// </param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder WithFullUrlBase(string baseUrl)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(baseUrl);
        _fullUrlBase = baseUrl.TrimEnd('/');
        return this;
    }

    /// <summary>
    /// Enables the inclusion of a Provenance resource in the transaction bundle, with the specified
    /// <c>Provenance.agent.who</c> and <c>Provenance.entity.what</c> references. If the bundle
    /// contains both delete and update/create entries, two Provenance resources will be included.
    ///
    /// <para>The <c>Provenance.id</c> is built from the hash of the <paramref name="who"/> and
    /// <paramref name="what"/> references.</para>
    /// </summary>
    /// <param name="who">
    /// A reference to the agent responsible for the transformation or deletion. This is typically
    /// the transformation service itself.
    /// </param>
    /// <param name="what">A reference to the resource that is the source of the transformation.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder WithProvenance(ResourceReference who, ResourceReference what)
    {
        ArgumentNullException.ThrowIfNull(who);
        ArgumentNullException.ThrowIfNull(what);
        _provenanceWho = who;
        _provenanceWhat = what;
        return this;
    }

    /// <summary>
    /// Enables the inclusion of a Provenance resource in the transaction bundle, using the given
    /// Device as the <c>Provenance.agent.who</c>.
    /// </summary>
    /// <param name="device">
    /// The Device resource representing the agent responsible for the transformation or deletion.
    /// The resource is automatically added to the bundle and referenced in the
    /// <c>Provenance.agent.who</c> element.
    /// </param>
    /// <param name="what">A reference to the resource that is the source of the transformation.</param>
    /// <returns>This builder instance for chaining.</returns>
    public TransactionBuilder WithProvenance(Device device, ResourceReference what)
    {
        ArgumentNullException.ThrowIfNull(device);
        ArgumentNullException.ThrowIfNull(what);
        _provenanceDevice = device;
        _provenanceWho = ReferenceUtils.CreateReferenceTo(device);
        _provenanceWhat = what;
        return this;
    }

    /// <summary>Builds and returns a FHIR Bundle with the configured type.</summary>
    /// <returns>A new Bundle instance with the configured type.</returns>
    /// <exception cref="InvalidOperationException">
    /// <see cref="FailOnDuplicateEntries"/> is enabled and duplicate resource ids were found.
    /// </exception>
    public Bundle Build()
    {
        var bundle = CreateBundle(_bundleType, _bundleId);

        AddDataEntries(bundle);

        if (IsProvenanceEnabled)
        {
            if (_provenanceDevice is not null && !_resources.Contains(_provenanceDevice))
            {
                AddPutEntry(bundle, _provenanceDevice);
            }

            AddProvenanceEntries(bundle);
        }

        return bundle;
    }

    /// <summary>
    /// Builds and returns two FHIR Bundles: a data bundle containing only the data resources and
    /// delete entries, and a provenance bundle containing the Provenance resource(s) and optionally
    /// the Device resource (if configured via <see cref="WithProvenance(Device, ResourceReference)"/>).
    /// The provenance bundle always uses <see cref="Bundle.BundleType.Transaction"/>.
    /// </summary>
    /// <returns>The data and provenance bundles.</returns>
    /// <exception cref="InvalidOperationException">
    /// Provenance has not been enabled via <see cref="WithProvenance(ResourceReference, ResourceReference)"/>
    /// or <see cref="WithProvenance(Device, ResourceReference)"/>, or
    /// <see cref="FailOnDuplicateEntries"/> is enabled and duplicate resource ids were found.
    /// </exception>
    public DataAndProvenanceBundles BuildWithSeparateProvenance()
    {
        if (!IsProvenanceEnabled)
        {
            throw new InvalidOperationException(
                "Provenance must be enabled via WithProvenance() before calling BuildWithSeparateProvenance()"
            );
        }

        var dataBundle = CreateBundle(_bundleType, _bundleId);
        AddDataEntries(dataBundle);

        // Use a SHA-256 hash of "provenance-" + the data bundle id for the provenance bundle id.
        // This keeps the id unique while ensuring it stays within FHIR id length/character
        // constraints.
        // We could also just use the data bundle id, but if both bundles are written to the same
        // topic in Kafka and that id is used as the key, it would cause issues on compaction.
        var provenanceBundle = CreateBundle(
            Bundle.BundleType.Transaction,
            _bundleId is null ? null : Sha256Hex($"provenance-{_bundleId}")
        );

        if (_provenanceDevice is not null)
        {
            AddPutEntry(provenanceBundle, _provenanceDevice);
        }

        AddProvenanceEntries(provenanceBundle);

        return new DataAndProvenanceBundles(dataBundle, provenanceBundle);
    }

    private static Bundle CreateBundle(Bundle.BundleType type, string? id) =>
        new() { Type = type, Id = id };

    /// <summary>
    /// Builds the <c>fullUrl</c> for a bundle entry from the resource's relative reference (e.g.
    /// <c>Patient/123</c>).
    ///
    /// <para>If a base URL has been configured via <see cref="WithFullUrlBase"/>, the fullUrl is
    /// <c>&lt;baseUrl&gt;/&lt;reference&gt;</c> - an absolute, hierarchical URL, as required by the
    /// FHIR spec. Other resources in the bundle can still reference this entry using the plain
    /// relative reference, since Bundle reference resolution rules match a relative reference
    /// against the tail of such a fullUrl.</para>
    ///
    /// <para>Otherwise, the relative reference itself is used, unchanged.</para>
    /// </summary>
    private string BuildFullUrl(string reference) =>
        _fullUrlBase is null ? reference : $"{_fullUrlBase}/{reference}";

    private void AddDataEntries(Bundle bundle)
    {
        var seen = new HashSet<string>(StringComparer.Ordinal);

        foreach (var resource in _resources)
        {
            var url = ReferenceUtils.CreateReferenceTo(resource).Reference!;

            if (_failOnDuplicateEntries && !seen.Add(url))
            {
                throw new InvalidOperationException($"Duplicate resource added: {url}");
            }

            AddPutEntry(bundle, resource, url);
        }

        foreach (var toDeleteReference in _resourcesToDelete)
        {
            bundle.Entry.Add(
                new Bundle.EntryComponent
                {
                    Request = new Bundle.RequestComponent
                    {
                        Method = Bundle.HTTPVerb.DELETE,
                        Url = toDeleteReference.Reference,
                    },
                }
            );
        }
    }

    private void AddProvenanceEntries(Bundle bundle)
    {
        if (_resources.Count > 0)
        {
            AddPutEntry(bundle, BuildCreateProvenance());
        }

        if (_resourcesToDelete.Count > 0)
        {
            AddPutEntry(bundle, BuildDeleteProvenance());
        }
    }

    private void AddPutEntry(Bundle bundle, Resource resource) =>
        AddPutEntry(bundle, resource, ReferenceUtils.CreateReferenceTo(resource).Reference!);

    private void AddPutEntry(Bundle bundle, Resource resource, string url) =>
        bundle.Entry.Add(
            new Bundle.EntryComponent
            {
                FullUrl = BuildFullUrl(url),
                Resource = resource,
                Request = new Bundle.RequestComponent { Method = Bundle.HTTPVerb.PUT, Url = url },
            }
        );

    private Provenance BuildCreateProvenance()
    {
        var (who, what) = RequireProvenance();
        var now = DateTimeOffset.UtcNow;

        var targets = _resources.Select(ReferenceUtils.CreateReferenceTo).ToList();

        if (_provenanceDevice is not null)
        {
            // If a device is configured as the provenance agent, we also want to include a
            // reference to it in the targets list.
            var deviceReference = ReferenceUtils.CreateReferenceTo(_provenanceDevice);
            if (!targets.Exists(target => target.Reference == deviceReference.Reference))
            {
                targets.Add(deviceReference);
            }
        }

        return new Provenance
        {
            Id = Sha256Hex($"create-{BuildProvenanceIdString(who, what)}"),
            Occurred = new FhirDateTime(now),
            Recorded = now,
            Target = targets,
            Activity = Concept(DataOperationSystem, "CREATE", "create"),
            Agent =
            {
                new Provenance.AgentComponent
                {
                    Type = Concept(ParticipantTypeSystem, "assembler", "Assembler"),
                    Role = { Concept(ParticipationTypeSystem, "AUT", "author") },
                    Who = who,
                },
            },
            Entity =
            {
                new Provenance.EntityComponent
                {
                    Role = Provenance.ProvenanceEntityRole.Source,
                    What = what,
                },
            },
        };
    }

    private Provenance BuildDeleteProvenance()
    {
        var (who, what) = RequireProvenance();
        var now = DateTimeOffset.UtcNow;

        var provenance = new Provenance
        {
            Id = Sha256Hex($"delete-{BuildProvenanceIdString(who, what)}"),
            Occurred = new FhirDateTime(now),
            Recorded = now,
            Target = [.. _resourcesToDelete],
            Activity = Concept(DataOperationSystem, "DELETE", "delete"),
            Agent =
            {
                new Provenance.AgentComponent
                {
                    Type = Concept(ParticipantTypeSystem, "performer", "Performer"),
                    Who = who,
                },
            },
        };

        foreach (var toDelete in _resourcesToDelete)
        {
            provenance.Entity.Add(
                new Provenance.EntityComponent
                {
                    Role = Provenance.ProvenanceEntityRole.Removal,
                    What = toDelete,
                }
            );
        }

        return provenance;
    }

    private (ResourceReference Who, ResourceReference What) RequireProvenance() =>
        _provenanceWho is { } who && _provenanceWhat is { } what
            ? (who, what)
            : throw new InvalidOperationException(
                "Provenance must be enabled via WithProvenance() before building a Provenance resource"
            );

    private static string BuildProvenanceIdString(ResourceReference who, ResourceReference what) =>
        $"{Describe(who, "who")}-{Describe(what, "what")}";

    /// <summary>
    /// Reduces a reference to the most specific stable string available - its literal reference,
    /// else its identifier value, else its display - for use in a deterministic Provenance id.
    /// </summary>
    private static string Describe(ResourceReference reference, string name)
    {
        if (!string.IsNullOrWhiteSpace(reference.Reference))
        {
            return reference.Reference;
        }

        if (reference.Identifier?.Value is { } identifierValue)
        {
            return identifierValue;
        }

        if (reference.Display is { } display)
        {
            return display;
        }

        throw new InvalidOperationException(
            $"Invalid provenance {name} reference. Either reference, identifier or display must be provided."
        );
    }

    private static CodeableConcept Concept(string system, string code, string display) =>
        new()
        {
            Coding =
            {
                new Coding
                {
                    System = system,
                    Code = code,
                    Display = display,
                },
            },
        };

    private static string Sha256Hex(string value) =>
        Convert.ToHexStringLower(SHA256.HashData(Encoding.UTF8.GetBytes(value)));
}

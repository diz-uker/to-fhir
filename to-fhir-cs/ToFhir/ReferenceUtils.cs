using Hl7.Fhir.Model;

namespace ToFhir;

/// <summary>Utility class for creating FHIR references to resources.</summary>
public static class ReferenceUtils
{
    /// <summary>
    /// Creates a FHIR reference to the given resource, in the form <c>ResourceType/id</c>. The
    /// resource must have a non-blank <see cref="Resource.Id"/>.
    /// </summary>
    /// <param name="resource">The FHIR resource to create a reference to.</param>
    /// <returns>A reference pointing to the given resource.</returns>
    /// <exception cref="ArgumentException">The resource's id is <c>null</c>, empty or whitespace.</exception>
    public static ResourceReference CreateReferenceTo(Resource resource)
    {
        ArgumentNullException.ThrowIfNull(resource);

        if (string.IsNullOrWhiteSpace(resource.Id))
        {
            throw new ArgumentException(
                $"Resource of type {resource.TypeName} must have a non-blank id to be referenced.",
                nameof(resource)
            );
        }

        return new ResourceReference($"{resource.TypeName}/{resource.Id}");
    }

    /// <summary>
    /// Creates a FHIR reference to the resource identified by the given identifier, in the form
    /// <c>ResourceType/sha256(system|value)</c>. The identifier must have a non-blank system and
    /// value.
    /// </summary>
    /// <param name="identifier">An identifier of the FHIR resource to create a reference to.</param>
    /// <param name="resourceType">The resource type to include in the reference.</param>
    /// <returns>A reference pointing to the identified resource.</returns>
    /// <exception cref="ArgumentException">
    /// The identifier's system or value is <c>null</c>, empty or whitespace.
    /// </exception>
    public static ResourceReference CreateReferenceTo(
        Identifier identifier,
        ResourceType resourceType
    ) => new(IdUtils.FromIdentifier(identifier, resourceType));
}

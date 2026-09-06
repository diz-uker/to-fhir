using System.Security.Cryptography;
using System.Text;
using Hl7.Fhir.Model;
using Hl7.Fhir.Utility;

namespace ToFhir;

/// <summary>Utility class for FHIR resource ids.</summary>
public static class IdUtils
{
    /// <summary>
    /// Computes a deterministic id from a FHIR <see cref="Identifier"/> by hashing its system and
    /// value with SHA-256.
    /// </summary>
    /// <param name="identifier">The FHIR identifier to compute the id from.</param>
    /// <returns>A deterministic id derived from the identifier's system and value.</returns>
    /// <exception cref="ArgumentException">
    /// The identifier's system or value is <c>null</c>, empty or whitespace.
    /// </exception>
    public static string FromIdentifier(Identifier identifier) =>
        FromIdentifier(identifier, HashAlgorithmName.SHA256);

    /// <summary>
    /// Computes a deterministic id from a FHIR <see cref="Identifier"/> using the given hash
    /// algorithm.
    /// </summary>
    /// <param name="identifier">The FHIR identifier to compute the id from.</param>
    /// <param name="hashAlgorithm">The hash algorithm to use, e.g. <see cref="HashAlgorithmName.SHA256"/>.</param>
    /// <returns>A deterministic id derived from the identifier's system and value.</returns>
    /// <exception cref="ArgumentException">
    /// The identifier's system or value is <c>null</c>, empty or whitespace.
    /// </exception>
    public static string FromIdentifier(Identifier identifier, HashAlgorithmName hashAlgorithm)
    {
        ArgumentNullException.ThrowIfNull(identifier);

        if (string.IsNullOrWhiteSpace(identifier.System))
        {
            throw new ArgumentException("Identifier system must not be blank.", nameof(identifier));
        }

        if (string.IsNullOrWhiteSpace(identifier.Value))
        {
            throw new ArgumentException(
                $"Identifier value must not be blank. System: {identifier.System}",
                nameof(identifier)
            );
        }

        var hash = CryptographicOperations.HashData(
            hashAlgorithm,
            Encoding.UTF8.GetBytes($"{identifier.System}|{identifier.Value}")
        );

        return Convert.ToHexStringLower(hash);
    }

    /// <summary>
    /// Computes a deterministic, type-qualified id from a FHIR <see cref="Identifier"/> by hashing
    /// its system and value with SHA-256.
    /// </summary>
    /// <param name="identifier">The FHIR identifier to compute the id from.</param>
    /// <param name="resourceType">The resource type to qualify the id with.</param>
    /// <returns>A relative reference of the form <c>ResourceType/id</c>.</returns>
    /// <exception cref="ArgumentException">
    /// The identifier's system or value is <c>null</c>, empty or whitespace.
    /// </exception>
    public static string FromIdentifier(Identifier identifier, ResourceType resourceType) =>
        FromIdentifier(identifier, resourceType, HashAlgorithmName.SHA256);

    /// <summary>
    /// Computes a deterministic, type-qualified id from a FHIR <see cref="Identifier"/> using the
    /// given hash algorithm.
    /// </summary>
    /// <param name="identifier">The FHIR identifier to compute the id from.</param>
    /// <param name="resourceType">The resource type to qualify the id with.</param>
    /// <param name="hashAlgorithm">The hash algorithm to use, e.g. <see cref="HashAlgorithmName.SHA256"/>.</param>
    /// <returns>A relative reference of the form <c>ResourceType/id</c>.</returns>
    /// <exception cref="ArgumentException">
    /// The identifier's system or value is <c>null</c>, empty or whitespace.
    /// </exception>
    public static string FromIdentifier(
        Identifier identifier,
        ResourceType resourceType,
        HashAlgorithmName hashAlgorithm
    ) => $"{resourceType.GetLiteral()}/{FromIdentifier(identifier, hashAlgorithm)}";
}

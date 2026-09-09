using Hl7.Fhir.Model;

namespace ToFhir;

/// <summary>
/// A pair of FHIR bundles: one containing data resources and one containing provenance resources.
/// </summary>
/// <param name="DataBundle">The bundle containing data resources (e.g. Patient, Observation).</param>
/// <param name="ProvenanceBundle">
/// The bundle containing Provenance and optionally Device resources.
/// </param>
public sealed record DataAndProvenanceBundles(Bundle DataBundle, Bundle ProvenanceBundle);

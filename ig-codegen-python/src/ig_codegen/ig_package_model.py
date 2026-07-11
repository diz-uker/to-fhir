"""Classified canonical URLs of a single FHIR IG package, keyed by the generated Python constant
name (e.g. ``MII_CS_ONKO_INTENTION``).

``code_system_concepts`` holds, for each entry in ``code_systems`` that is a locally defined
(``content == "complete"``) CodeSystem, the flattened list of its concepts - everything needed to
additionally render a Python enum. Entries absent from this dict (e.g. external terminologies like
SNOMED CT or LOINC, which FHIR doesn't redistribute inline) only get the plain URL constant.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from types import MappingProxyType

from ig_codegen.concept_constant import ConceptConstant


@dataclass(frozen=True)
class IgPackageModel:
    package_name: str
    package_version: str
    code_systems: MappingProxyType[str, str] = field(default_factory=lambda: MappingProxyType({}))
    profiles: MappingProxyType[str, str] = field(default_factory=lambda: MappingProxyType({}))
    extensions: MappingProxyType[str, str] = field(default_factory=lambda: MappingProxyType({}))
    code_system_concepts: MappingProxyType[str, list[ConceptConstant]] = field(
        default_factory=lambda: MappingProxyType({})
    )

    def __post_init__(self) -> None:
        object.__setattr__(self, "code_systems", MappingProxyType(dict(self.code_systems)))
        object.__setattr__(self, "profiles", MappingProxyType(dict(self.profiles)))
        object.__setattr__(self, "extensions", MappingProxyType(dict(self.extensions)))
        object.__setattr__(
            self, "code_system_concepts", MappingProxyType(dict(self.code_system_concepts))
        )

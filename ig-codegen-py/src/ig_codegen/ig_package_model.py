"""Data model for a scanned FHIR IG package."""
from __future__ import annotations

from dataclasses import dataclass
from typing import ClassVar


@dataclass(frozen=True)
class ConceptConstant:
    constant_name: str
    code: str
    display: str | None


@dataclass(frozen=True)
class ExtensionValueType:
    fhir_type_code: str | None
    choice: bool
    bound_code_system_url: str | None

    NONE: ClassVar[ExtensionValueType]
    CHOICE: ClassVar[ExtensionValueType]

    @staticmethod
    def fixed(fhir_type_code: str) -> ExtensionValueType:
        return ExtensionValueType(
            fhir_type_code=fhir_type_code, choice=False, bound_code_system_url=None
        )

    @staticmethod
    def bound_coding(fhir_type_code: str, code_system_url: str) -> ExtensionValueType:
        return ExtensionValueType(
            fhir_type_code=fhir_type_code, choice=False, bound_code_system_url=code_system_url
        )


ExtensionValueType.NONE = ExtensionValueType(
    fhir_type_code=None, choice=False, bound_code_system_url=None
)
ExtensionValueType.CHOICE = ExtensionValueType(
    fhir_type_code=None, choice=True, bound_code_system_url=None
)


@dataclass(frozen=True)
class NamingSystemEntry:
    description: str | None
    by_type: dict[str, list[str]]


@dataclass
class IgPackageModel:
    package_name: str
    package_version: str
    code_systems: dict[str, str]
    profiles: dict[str, str]
    extensions: dict[str, str]
    code_system_concepts: dict[str, list[ConceptConstant]]
    extension_value_types: dict[str, ExtensionValueType]
    naming_systems: dict[str, NamingSystemEntry]

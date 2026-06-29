"""Generates Python constant classes (CodeSystem/Profile/Extension canonical URLs) from FHIR
Implementation Guide packages.
"""

from ig_codegen.ig_codegen import IgCodegen
from ig_codegen.ig_package_model import IgPackageModel
from ig_codegen.ig_package_scanner import IgPackageScanner

__all__ = ["IgCodegen", "IgPackageModel", "IgPackageScanner"]

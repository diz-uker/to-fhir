#!/usr/bin/env python3
"""Regenerates the constant modules in src/fhir_ig_constants from package.json.

Manually-invoked "generate, review, commit" dev tool, not a build-graph participant - mirrors
``fhir-ig-constants/build.gradle``'s ``generateIgConstants`` task. Run via:

    uv run --group dev python scripts/generate_constants.py

then review the diff and commit.
"""

from pathlib import Path

from ig_codegen.ig_codegen import IgCodegen

ROOT = Path(__file__).resolve().parent.parent


def main() -> None:
    package_json_file = ROOT / "package.json"
    fhir_packages_dir = IgCodegen.default_fhir_packages_dir(cwd=ROOT)
    output_dir = ROOT / "src" / "fhir_ig_constants"

    for generated_file in IgCodegen().generate(package_json_file, fhir_packages_dir, output_dir):
        print(f"Generated {generated_file}")


if __name__ == "__main__":
    main()

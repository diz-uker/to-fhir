"""Command-line entry point: ``ig-codegen [package_json_file] [fhir_packages_dir] [output_dir]``."""

from __future__ import annotations

import sys
from pathlib import Path

from ig_codegen.ig_codegen import IgCodegen


def main(argv: list[str] | None = None) -> None:
    args = sys.argv[1:] if argv is None else argv

    package_json_file = Path(args[0]) if len(args) > 0 else Path("ig-codegen-python/package.json")
    fhir_packages_dir = Path(args[1]) if len(args) > 1 else IgCodegen.default_fhir_packages_dir()
    output_dir = (
        Path(args[2]) if len(args) > 2 else Path("build", "generated", "sources", "ig-codegen")
    )

    generated_files = IgCodegen().generate(package_json_file, fhir_packages_dir, output_dir)
    for file in generated_files:
        print(f"Generated {file}")


if __name__ == "__main__":
    main()

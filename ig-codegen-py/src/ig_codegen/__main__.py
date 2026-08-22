"""CLI entry point: generates Python FHIR IG constants from a package.json manifest."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .ig_package_scanner import resolve_package_content_dir, scan
from .name_utils import to_pascal_case
from .python_constants_generator import write_to

_SKIPPED_PACKAGES: frozenset[str] = frozenset({"hl7.fhir.r4.core"})


def main() -> None:
    parser = argparse.ArgumentParser(
        prog="ig-codegen",
        description="Generate Python FHIR IG constants from a package.json manifest.",
    )
    parser.add_argument(
        "package_json",
        type=Path,
        help="Path to the package.json with FHIR IG dependencies.",
    )
    parser.add_argument(
        "fhir_packages_dir",
        type=Path,
        help=(
            "Directory containing the resolved FHIR packages "
            "(Firely Terminal cache or npm node_modules)."
        ),
    )
    parser.add_argument(
        "output_dir",
        type=Path,
        help="Output directory for the generated Python source files.",
    )
    args = parser.parse_args()

    try:
        manifest = json.loads(args.package_json.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as e:
        print(f"error: could not read {args.package_json}: {e}", file=sys.stderr)
        sys.exit(1)

    dependencies: dict[str, str] = manifest.get("dependencies", {})
    args.output_dir.mkdir(parents=True, exist_ok=True)

    for package_name, package_version in dependencies.items():
        if package_name in _SKIPPED_PACKAGES:
            continue
        content_dir = resolve_package_content_dir(
            args.fhir_packages_dir, package_name, package_version
        )
        if not content_dir.is_dir():
            print(
                f"warning: package directory not found for {package_name}@{package_version}:"
                f" {content_dir}",
                file=sys.stderr,
            )
            continue

        model = scan(content_dir, package_name, package_version)
        last_segment = package_name.split(".")[-1]
        class_name = to_pascal_case(last_segment)
        path = write_to(model, class_name, args.output_dir)
        print(f"Generated: {path}")


if __name__ == "__main__":
    main()

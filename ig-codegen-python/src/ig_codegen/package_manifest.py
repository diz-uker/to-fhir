"""A FHIR package manifest (``package.json``, restored by Firely Terminal's ``fhir restore``).
Despite the filename, this is the FHIR package ecosystem's manifest shape, not npm's.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class PackageManifest:
    name: str
    version: str
    dependencies: dict[str, str]

    @staticmethod
    def read(package_json_file: Path) -> PackageManifest:
        data = json.loads(package_json_file.read_text())
        return PackageManifest(
            name=data["name"],
            version=data["version"],
            dependencies=data.get("dependencies", {}),
        )

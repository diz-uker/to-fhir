"""Generates one Python constant module per FHIR IG package declared in a package manifest's
``dependencies``.

Each generated module resides in a Python package with the same name as the FHIR package (e.g.
the FHIR package ``de.medizininformatikinitiative.kerndatensatz.onkologie`` produces the Python
package ``de.medizininformatikinitiative.kerndatensatz.onkologie``), so canonical URLs don't have
to be hand-transcribed into application config.
"""

from __future__ import annotations

import os
from pathlib import Path

from ig_codegen import name_utils, python_constants_generator
from ig_codegen.ig_package_scanner import IgPackageScanner
from ig_codegen.package_manifest import PackageManifest

# FHIR packages with no IG-specific canonical prefix of their own; nothing to generate.
_SKIPPED_PACKAGES = frozenset({"hl7.fhir.r4.core"})


class IgCodegen:
    def __init__(self) -> None:
        self._scanner = IgPackageScanner()

    def generate(
        self, package_json_file: Path, fhir_packages_dir: Path, output_dir: Path
    ) -> list[Path]:
        """Generates one Python constant module per non-skipped dependency in
        ``package_json_file``.

        Args:
            package_json_file: the FHIR package manifest listing the IG packages to generate from.
            fhir_packages_dir: where restored FHIR packages live, e.g. the Firely Terminal cache
                (``~/.fhir/packages``) or an npm ``node_modules``.
            output_dir: the Python source root to write generated packages into.
        """
        manifest = PackageManifest.read(package_json_file)

        generated_files: list[Path] = []
        for package_name, package_version in manifest.dependencies.items():
            if package_name in _SKIPPED_PACKAGES:
                continue

            package_content_dir = self._scanner.resolve_package_content_dir(
                fhir_packages_dir, package_name, package_version
            )
            model = self._scanner.scan(package_content_dir, package_name, package_version)

            class_name = name_utils.to_pascal_case(_last_segment(package_name))
            module_dir = output_dir.joinpath(*package_name.split("."))
            _ensure_package_dirs(output_dir, module_dir)
            generated_files.append(
                python_constants_generator.write_to(model, class_name, module_dir / "__init__.py")
            )
        return generated_files

    @staticmethod
    def default_fhir_packages_dir(home_dir: str | None = None, cwd: Path | None = None) -> Path:
        """Tries, in order: the Firely Terminal global package cache (``~/.fhir/packages``); a
        project-local ``./.fhir/packages``, e.g. when a local Firely Terminal config restores
        packages relative to the current directory instead; and finally ``./node_modules``, for
        FHIR packages installed via ``npm install`` rather than ``fhir restore``.

        Resolves the home directory from the ``HOME`` environment variable rather than
        :func:`pathlib.Path.home`, which in some containers (e.g. GitHub Actions container jobs,
        where ``HOME`` is overridden to ``/github/home``) doesn't match the directory tools like
        Firely Terminal actually restore into.
        """
        if home_dir is None:
            home_dir = os.environ.get("HOME") or str(Path.home())
        if cwd is None:
            cwd = Path()

        home_fhir_packages_dir = Path(home_dir, ".fhir", "packages")
        if home_fhir_packages_dir.is_dir():
            return home_fhir_packages_dir
        cwd_fhir_packages_dir = cwd / ".fhir" / "packages"
        if cwd_fhir_packages_dir.is_dir():
            return cwd_fhir_packages_dir
        return cwd / "node_modules"


def _last_segment(dot_separated_name: str) -> str:
    return dot_separated_name.rsplit(".", 1)[-1]


def _ensure_package_dirs(output_dir: Path, module_dir: Path) -> None:
    """Creates an empty ``__init__.py`` for every ancestor directory between ``output_dir`` and
    ``module_dir`` (exclusive), so the generated module is reachable as a regular
    (non-namespace) package. ``module_dir``'s own ``__init__.py`` is left for the caller to write
    the actual generated content into.
    """
    module_dir.mkdir(parents=True, exist_ok=True)
    current = output_dir
    for part in module_dir.relative_to(output_dir).parts[:-1]:
        current = current / part
        init_file = current / "__init__.py"
        if not init_file.exists():
            init_file.write_text("")

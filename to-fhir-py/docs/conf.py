"""Sphinx configuration for the to-fhir Python API reference."""

from __future__ import annotations

from importlib.metadata import version as pkg_version

project = "to-fhir"
copyright = "diz-uker contributors"
author = "diz-uker contributors"
release = pkg_version("to-fhir")
version = release

extensions = [
    "sphinx.ext.autodoc",
    "sphinx.ext.napoleon",
    "sphinx.ext.viewcode",
    "sphinx.ext.intersphinx",
]

napoleon_google_docstring = True
napoleon_numpy_docstring = False

autodoc_member_order = "bysource"
autodoc_default_options = {
    "members": True,
    "undoc-members": True,
    "show-inheritance": True,
}
add_module_names = False

intersphinx_mapping = {
    "python": ("https://docs.python.org/3", None),
}

templates_path: list[str] = []
exclude_patterns: list[str] = ["_build"]

html_theme = "furo"
html_title = "to-fhir (Python)"

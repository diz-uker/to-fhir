"""Lightweight indentation-tracking string builder for Python code generation."""
from __future__ import annotations

from collections.abc import Callable


class CodeWriter:
    def __init__(self) -> None:
        self._lines: list[str] = []
        self._indent: int = 0

    def line(self, text: str = "") -> None:
        if text:
            self._lines.append("    " * self._indent + text)
        else:
            self._lines.append("")

    def block(self, header: str, body: Callable[[], None]) -> None:
        """Emit `header:`, indent, call body(), then unindent."""
        self.line(header + ":")
        self._indent += 1
        body()
        self._indent -= 1

    def __str__(self) -> str:
        return "\n".join(self._lines) + "\n"

#!/usr/bin/env python3
"""Run the deterministic texture generator twice and compare PNG outputs."""
from __future__ import annotations

import hashlib
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GENERATOR = ROOT / "tools/generate_salur_textures.py"


def texture_hashes() -> dict[str, str]:
    return {
        path.relative_to(ROOT).as_posix(): hashlib.sha256(path.read_bytes()).hexdigest()
        for path in sorted((ROOT / "src/main/resources/assets/create_kiday/textures").rglob("*.png"))
    }


def run_generator() -> None:
    subprocess.run([sys.executable, str(GENERATOR)], cwd=ROOT, check=True)


run_generator()
first = texture_hashes()
run_generator()
second = texture_hashes()
if first != second:
    changed = sorted(path for path in first.keys() | second.keys() if first.get(path) != second.get(path))
    print("Texture generation is nondeterministic:", *changed, sep="\n - ")
    sys.exit(1)
print(f"Verified deterministic output for {len(second)} PNG textures across two generator runs.")

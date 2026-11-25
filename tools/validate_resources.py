#!/usr/bin/env python3
"""Validate packaged JSON and statically resolvable Create: Kiday resource links."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/create_kiday"
errors: list[str] = []


def fail(message: str) -> None:
    errors.append(message)


json_files = sorted(RES.rglob("*.json"))
parsed: dict[Path, object] = {}
for path in json_files:
    try:
        parsed[path] = json.loads(path.read_text(encoding="utf-8"))
    except (UnicodeError, json.JSONDecodeError) as exc:
        fail(f"invalid JSON: {path.relative_to(ROOT)}: {exc}")


def registered_ids(java_file: Path, patterns: tuple[str, ...]) -> set[str]:
    source = java_file.read_text(encoding="utf-8")
    ids: set[str] = set()
    for pattern in patterns:
        ids.update(re.findall(pattern, source))
    return ids


item_ids = registered_ids(ROOT / "src/main/java/io/github/createkiday/registry/ModItems.java", (
    r'\b(?:register|registerSimpleItem)\("([a-z0-9_]+)"',
    r'\b(?:ingredient|equipment|hot|mold)\("([a-z0-9_]+)"',
))
block_ids = registered_ids(ROOT / "src/main/java/io/github/createkiday/registry/ModBlocks.java", (
    r'\b(?:register|registerSimpleBlock)\("([a-z0-9_]+)"',
))
entity_ids = registered_ids(ROOT / "src/main/java/io/github/createkiday/registry/ModEntities.java", (
    r'TYPES\.register\("([a-z0-9_]+)"',
))
registry_ids = item_ids | block_ids | entity_ids

for path, data in parsed.items():
    rel = path.relative_to(RES).as_posix()
    if "/recipe/" in rel or "/loot_table/" in rel:
        def walk(value: object, key: str = "") -> None:
            if isinstance(value, dict):
                for child_key, child in value.items():
                    walk(child, child_key)
            elif isinstance(value, list):
                for child in value:
                    walk(child, key)
            elif isinstance(value, str) and key in {"item", "name", "id"}:
                namespace, separator, path_part = value.partition(":")
                if separator and namespace == "create_kiday" and path_part not in registry_ids:
                    fail(f"unregistered item/block reference {value} in {rel}")
        walk(data)
    if "/advancement/" in rel and isinstance(data, dict):
        parent = data.get("display", {}).get("parent") if isinstance(data.get("display"), dict) else None
        if parent:
            namespace, separator, parent_id = parent.partition(":")
            if separator and namespace == "create_kiday" and not (RES / "data/create_kiday/advancement" / f"{parent_id}.json").exists():
                fail(f"missing advancement parent {parent} in {rel}")
    if "/tags/" in rel and isinstance(data, dict):
        for entry in data.get("values", []):
            value = entry.get("id") if isinstance(entry, dict) else entry
            if isinstance(value, str) and value.startswith("create_kiday:") and not value.startswith("#"):
                tag_id = value.split(":", 1)[1]
                candidates = [RES / "data/create_kiday" / rel.split("/tags/", 1)[1].replace(".json", "")]
                if not any(candidate.exists() for candidate in candidates):
                    # Direct entry IDs are verified by the data loader; nested tags are checked here.
                    nested = value.split(":", 1)[1]
                    if nested.startswith("tags/") and not (RES / "data/create_kiday" / (nested + ".json")).exists():
                        fail(f"missing nested tag {value} in {rel}")

# Resolve mod-owned models and their direct texture references; external vanilla/Create assets stay native.
for path, data in parsed.items():
    rel = path.relative_to(RES).as_posix()
    if not rel.startswith("assets/create_kiday/models/") or not isinstance(data, dict):
        continue
    parent = data.get("parent")
    if isinstance(parent, str) and parent.startswith("create_kiday:"):
        model_path = parent.split(":", 1)[1]
        if not (ASSETS / "models" / f"{model_path}.json").exists():
            fail(f"missing parent model {parent} in {rel}")
    textures = data.get("textures", {})
    if isinstance(textures, dict):
        for texture in textures.values():
            if isinstance(texture, str) and texture.startswith("create_kiday:"):
                texture_path = texture.split(":", 1)[1]
                if not (ASSETS / "textures" / f"{texture_path}.png").exists():
                    fail(f"missing texture {texture} in {rel}")

for path, data in parsed.items():
    rel = path.relative_to(RES).as_posix()
    if not rel.startswith("assets/create_kiday/blockstates/"):
        continue
    text = json.dumps(data)
    for model in re.findall(r'"model"\s*:\s*"create_kiday:([^\"]+)"', text):
        if not (ASSETS / "models" / f"{model}.json").exists():
            fail(f"missing blockstate model create_kiday:{model} in {rel}")

languages = {}
for lang in ("en_us", "tr_tr"):
    path = ASSETS / "lang" / f"{lang}.json"
    try:
        languages[lang] = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        fail(f"invalid or missing language file {path}: {exc}")
for item_id in sorted(item_ids):
    key = f"item.create_kiday.{item_id}"
    for lang, values in languages.items():
        if key not in values:
            fail(f"missing {lang} translation: {key}")

all_source = "\n".join(p.read_text(errors="ignore") for p in ROOT.joinpath("src/main").rglob("*") if p.is_file() and p.suffix != ".png")
for removed in ("hunter_bracelet", "bracelet_core", "bracelet_frame", "hunter_disk", "hot_kiday_", "hot_salur_"):
    if removed in all_source:
        fail(f"stale removed-content reference remains: {removed}")

for path in sorted((ASSETS / "textures").rglob("*.png")):
    texture_rel = path.relative_to(ASSETS).as_posix()
    texture_model_ref = texture_rel.removeprefix("textures/").removesuffix(".png")
    if texture_rel in all_source or texture_model_ref in all_source or path.stem in all_source:
        continue
    if texture_rel.startswith("textures/models/armor/"):
        continue  # ArmorMaterial.Layer resolves these names dynamically at runtime.
    fail(f"texture has no model or code reference: {texture_rel}")

if errors:
    print("Resource validation failed:")
    for error in errors:
        print(f" - {error}")
    sys.exit(1)
print(f"Validated {len(json_files)} JSON files, {len(item_ids)} item IDs, {len(block_ids)} block IDs, and all model texture links.")

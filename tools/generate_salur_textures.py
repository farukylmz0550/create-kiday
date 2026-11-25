#!/usr/bin/env python3
"""Deterministically build Kiday/Salur pixel art and a review atlas."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/create_kiday"
PALETTE = ((35, 55, 43), (57, 83, 65), (91, 126, 99), (141, 170, 145), (190, 211, 192))
WOOD_HANDLE = {
    (52, 34, 3), (82, 62, 37), (102, 79, 39), (128, 99, 49),
}

def recolor(source: Path, target: Path, preserve: set[tuple[int, int, int]] = frozenset()) -> None:
    image = Image.open(source).convert("RGBA")
    colors = []
    for pixel in image.get_flattened_data():
        r, g, b, a = pixel
        if a == 0:
            colors.append((0, 0, 0, 0))
            continue
        if (r, g, b) in preserve:
            colors.append((r, g, b, a))
            continue
        luminance = (r * 299 + g * 587 + b * 114) // 1000
        index = min(4, luminance * 5 // 256)
        pr, pg, pb = PALETTE[index]
        colors.append((pr, pg, pb, a))
    image.putdata(colors)
    target.parent.mkdir(parents=True, exist_ok=True)
    image.save(target, optimize=False)

ITEMS = {
    "raw_salur": "raw_kiday", "salur_ingot": "kiday_ingot", "salur_sheet": "kiday_sheet",
    "salur_sword": "kiday_sword", "salur_pickaxe": "kiday_pickaxe", "salur_axe": "kiday_axe",
    "salur_shovel": "kiday_shovel", "salur_hoe": "kiday_hoe", "salur_helmet": "kiday_helmet",
    "salur_chestplate": "kiday_chestplate", "salur_leggings": "kiday_leggings", "salur_boots": "kiday_boots",
    "salur_sword_head": "kiday_sword_head", "salur_pickaxe_head": "kiday_pickaxe_head",
    "salur_axe_head": "kiday_axe_head", "salur_shovel_head": "kiday_shovel_head", "salur_hoe_head": "kiday_hoe_head",
    "molten_salur_bucket_contents": "molten_kiday_bucket_contents",
}
BLOCKS = {
    "salur_ore_vein": "kiday_ore_vein", "salur_block": "kiday_block",
    "molten_salur_still": "molten_kiday_still", "molten_salur_flow": "molten_kiday_flow",
}
ARMOR = {"salur_layer_1": "kiday_layer_1", "salur_layer_2": "kiday_layer_2"}

generated = []
# Generic hot castings reuse the original Kiday hot artwork under the shared item names.
# They are deliberately copied byte-for-byte during the rename, not recolored for Salur.
for name in (
    "hot_ingot", "hot_sword_head", "hot_pickaxe_head", "hot_axe_head", "hot_shovel_head",
    "hot_hoe_head", "hot_helmet", "hot_chestplate", "hot_leggings", "hot_boots", "hot_trapdoor",
):
    generated.append((name, ASSETS / "textures/item" / f"{name}.png"))
for target_name, source_name in ITEMS.items():
    target = ASSETS / "textures/item" / f"{target_name}.png"
    preserve = WOOD_HANDLE if target_name in {"salur_sword", "salur_pickaxe", "salur_axe", "salur_shovel", "salur_hoe"} else frozenset()
    source = ASSETS / "textures/item" / f"{source_name}.png"
    if target_name == "salur_sheet":
        # Retain the Kiday Sheet's hand-placed facets, translating each metal
        # shade directly instead of flattening several highlights by luminance.
        sheet_shades = {
            (0, 0, 0): PALETTE[0],
            (166, 92, 99): PALETTE[1],
            (201, 115, 117): PALETTE[2],
            (219, 145, 146): PALETTE[3],
            (234, 179, 176): PALETTE[4],
            (243, 209, 201): PALETTE[4],
        }
        image = Image.open(source).convert("RGBA")
        image.putdata([
            (0, 0, 0, 0) if pixel[3] < 32 else (*sheet_shades.get(pixel[:3], PALETTE[2]), 255)
            for pixel in image.get_flattened_data()
        ])
        image.save(target, optimize=False)
    else:
        recolor(source, target, preserve)
    generated.append((target_name, target))
for target_name, source_name in BLOCKS.items():
    target = ASSETS / "textures/block" / f"{target_name}.png"
    recolor(ASSETS / "textures/block" / f"{source_name}.png", target)
    generated.append((target_name, target))
for target_name, source_name in ARMOR.items():
    target = ASSETS / "textures/models/armor" / f"{target_name}.png"
    recolor(ASSETS / "textures/models/armor" / f"{source_name}.png", target)
    generated.append((target_name, target))

def ascii_sprite(name: str, rows: tuple[str, ...], colors: dict[str, tuple[int, int, int, int]]) -> None:
    if len(rows) != 16 or any(len(row) != 16 for row in rows):
        raise ValueError(f"{name} sprite must be exactly 16x16")
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    image.putdata([colors[pixel] for row in rows for pixel in row])
    path = ASSETS / "textures/item" / f"{name}.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, optimize=False)
    generated.append((name, path))

def center(left: int, pixels: str) -> str:
    return "." * left + pixels + "." * left

def save_texture(name: str, path: Path, draw) -> None:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw(ImageDraw.Draw(image))
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, optimize=False)
    generated.append((name, path))

# The sequence starts with a Kiday Panel, deploys Create's precision mechanism,
# adds a lightning rod, then presses the assembly. Both icons show that recipe:
# the unfinished one has exposed metalwork; the finished one has a charged core.
mechanism_colors = {
    "D": (37, 30, 25, 255), "B": (105, 69, 34, 255),
    "G": (194, 140, 61, 255), "Y": (239, 197, 105, 255),
    "H": (255, 228, 142, 255), "O": (125, 79, 37, 255),
    "P": (35, 55, 43, 255), "L": (91, 126, 99, 255),
    "I": (144, 155, 152, 255), "S": (76, 87, 82, 255),
    "C": (84, 199, 210, 255),
    "W": (196, 240, 230, 255),
}

def storm_item(name: str, completed: bool) -> None:
    # Hand-authored 16x16 pixels. The finished item has a closed, shaded gear
    # around the Kiday inset. The unfinished item is an open gear frame with a
    # missing right quadrant and a loose steel spindle outside the assembly.
    if completed:
        rows = [
            ".....DDDDDD.....",
            "...DDGBBBBGDD...",
            "..DGBHYYYYYBGD..",
            ".DGBYYGGGGYYOBD.",
            "DGBYYGDDDDGOYBGD",
            "DBYYGDPPPPDGYYBD",
            "DBYYGDPLCLPDYYBD",
            "DBYYGDPLWLPDYYBD",
            "DBYYGDPLCLPDYYBD",
            "DBYYGDPPPPDGYYBD",
            "DGBYYGDDDDGOYBGD",
            ".DGBYYGGGGYOOBD.",
            "..DGBYYYYYYBGD..",
            "...DDGBBBBGDD...",
            ".....DDDDDD.....",
            ".......DD.......",
        ]
    else:
        rows = [
            ".....DDDDDD.....",
            "...DDGBBBBGD....",
            "..DGBHYYYYYBG...",
            ".DGBYYGGGGYYB...",
            "DGBYYGDDDDGYYB..",
            "DBYYGDPPPPDG....",
            "DBYYGDPLLPD..I..",
            "DBYYGDPIIPD..S..",
            "DBYYGDPLLPD..I..",
            "DGBYYGDDDD......",
            ".DGBYYGDDDO.....",
            "..DGBYYGGGO.....",
            "...DGBYYYYY.....",
            "....DDGBBB......",
            "......DDD.......",
            ".......D........",
        ]
    ascii_sprite(name, tuple(rows), {".": (0, 0, 0, 0), **mechanism_colors})

storm_item("incomplete_storm_mechanism", False)
storm_item("storm_mechanism", True)

# Compact machine faces echo the Create casing and Kiday Panel: dark iron edges,
# Salur-green inset plates, brass fasteners, and readable vents/bearing details.
MACHINE = {
    "D": (37, 39, 35, 255), "M": (94, 99, 91, 255),
    "m": (133, 140, 128, 255), "S": (57, 83, 65, 255),
    "T": (91, 126, 99, 255), "L": (141, 170, 145, 255), "H": (190, 211, 192, 255),
    "B": (194, 140, 61, 255), "b": (116, 76, 37, 255), "Y": (239, 197, 105, 255),
    "G": (104, 153, 151, 255), "g": (44, 78, 78, 255),
    "V": (43, 51, 46, 255), "W": (210, 225, 215, 255),
}

def machine_face(kind: str) -> None:
    def draw(d):
        # Structural casing and bolted frame.
        d.rectangle((0, 0, 15, 15), fill=MACHINE["D"])
        d.rectangle((1, 1, 14, 14), fill=MACHINE["M"])
        d.rectangle((2, 2, 13, 13), fill=MACHINE["m"])
        d.rectangle((3, 3, 12, 12), fill=MACHINE["D"])
        # Directional bevel: the light catches the upper and left edges while
        # the right and bottom edges fall into the casing shadow.
        d.line((1, 1, 14, 1), fill=MACHINE["W"])
        d.line((1, 1, 1, 14), fill=MACHINE["m"])
        d.line((14, 1, 14, 14), fill=MACHINE["D"])
        d.line((1, 14, 14, 14), fill=MACHINE["D"])
        d.line((2, 2, 13, 2), fill=MACHINE["M"])
        d.line((2, 2, 2, 13), fill=MACHINE["M"])
        d.line((13, 2, 13, 13), fill=MACHINE["D"])
        d.line((2, 13, 13, 13), fill=MACHINE["D"])
        d.point((2, 13), fill=MACHINE["b"])
        d.point((13, 13), fill=MACHINE["b"])
        if kind == "front":
            # A Salur-green square panel, recessed dark rim, and brass shaft bearing.
            d.rectangle((4, 4, 11, 11), fill=MACHINE["L"])
            d.rectangle((5, 5, 10, 10), fill=MACHINE["S"])
            d.line((4, 4, 10, 4), fill=MACHINE["H"])
            d.line((4, 4, 4, 10), fill=MACHINE["T"])
            d.line((11, 4, 11, 11), fill=MACHINE["S"])
            d.line((4, 11, 11, 11), fill=MACHINE["T"])
            for x, y, shade in (
                    (7, 4, "W"), (8, 4, "H"), (6, 5, "M"), (9, 5, "B"),
                    (5, 6, "M"), (10, 6, "b"), (4, 7, "M"), (11, 7, "b"),
                    (4, 8, "m"), (11, 8, "b"), (5, 9, "b"), (10, 9, "b"),
                    (6, 10, "b"), (9, 10, "b"), (7, 11, "b"), (8, 11, "b")):
                d.point((x, y), fill=MACHINE[shade])
            d.rectangle((6, 6, 9, 9), fill=MACHINE["B"])
            d.line((6, 6, 8, 6), fill=MACHINE["Y"])
            d.line((9, 6, 9, 9), fill=MACHINE["b"])
            d.line((6, 9, 8, 9), fill=MACHINE["b"])
            d.rectangle((7, 7, 8, 8), fill=MACHINE["D"])
            d.point((7, 7), fill=MACHINE["W"])
        elif kind == "side":
            # Beveled Salur-green inset and four deep ventilation slots.
            d.rectangle((3, 3, 12, 12), fill=MACHINE["S"])
            d.rectangle((4, 4, 11, 11), fill=MACHINE["L"])
            d.line((4, 4, 10, 4), fill=MACHINE["H"])
            d.line((4, 4, 4, 10), fill=MACHINE["T"])
            d.line((11, 4, 11, 11), fill=MACHINE["S"])
            d.line((4, 11, 11, 11), fill=MACHINE["T"])
            d.rectangle((5, 5, 10, 6), fill=MACHINE["L"])
            for x in (5, 7, 9):
                d.rectangle((x, 8, x, 11), fill=MACHINE["V"])
                d.line((x, 8, x, 8), fill=MACHINE["M"])
            d.point((4, 4), fill=MACHINE["W"])
            d.point((11, 4), fill=MACHINE["B"])
            d.point((4, 11), fill=MACHINE["B"])
            d.point((11, 11), fill=MACHINE["b"])
        else:
            # Top inspection grille, framed by the same green panel material.
            d.rectangle((3, 3, 12, 12), fill=MACHINE["S"])
            d.rectangle((4, 4, 11, 11), fill=MACHINE["L"])
            d.rectangle((5, 5, 10, 10), fill=MACHINE["V"])
            d.line((4, 4, 10, 4), fill=MACHINE["H"])
            d.line((4, 4, 4, 10), fill=MACHINE["T"])
            d.line((11, 4, 11, 11), fill=MACHINE["S"])
            d.line((4, 11, 11, 11), fill=MACHINE["T"])
            for y in (6, 8, 10):
                d.line((6, y, 8, y), fill=MACHINE["m"])
                d.point((9, y), fill=MACHINE["D"])
            d.point((4, 4), fill=MACHINE["H"])
            d.point((11, 4), fill=MACHINE["B"])
            d.point((4, 11), fill=MACHINE["B"])
            d.point((11, 11), fill=MACHINE["b"])
    save_texture(f"biomass_generator_{kind}", ASSETS / "textures/block" / f"biomass_generator_{kind}.png", draw)

for face in ("front", "side", "top"):
    machine_face(face)

salur_rows = (
    "................", center(6,"DDDD"), center(4,"DDSSSSDD"), center(2,"DSSSSSSSSSSD"),
    center(1,"DSSSSSSSSSSSSD"), "D"+"S"*14+"D", "D"+"S"*5+"H"*4+"S"*5+"D",
    "D"+"S"*4+"H"*6+"S"*4+"D", "D"+"S"*4+"H"*6+"S"*4+"D",
    "D"+"S"*5+"H"*4+"S"*5+"D", "D"+"S"*14+"D", center(1,"DSSSSSSSSSSSSD"),
    center(2,"DSSSSSSSSSSD"), center(4,"DDSSSSDD"), center(6,"DDDD"), "................",
)
ascii_sprite("salur_disc", salur_rows,
    {".":(0,0,0,0),"D":(*PALETTE[0],255),"S":(*PALETTE[3],255),"H":(*PALETTE[4],255)})

hot_rows = (
    "................", center(6,"DDDD"), center(4,"DDRRRRDD"), center(2,"D"+"R"*10+"D"),
    center(1,"DRRRRRRRRRRRRD"), "D"+"R"*14+"D", "D"+"R"*5+"O"*4+"R"*5+"D",
    "D"+"R"*4+"O"*6+"R"*4+"D", "D"+"R"*4+"O"*2+"H"*2+"O"*2+"R"*4+"D",
    "D"+"R"*4+"O"*2+"H"*2+"O"*2+"R"*4+"D", "D"+"R"*5+"O"*4+"R"*5+"D",
    "D"+"R"*14+"D", center(1,"DRRRRRRRRRRRRD"), center(2,"D"+"R"*10+"D"),
    center(4,"DDRRRRDD"), center(6,"DDDD"),
)
ascii_sprite("hot_disk", hot_rows,
    {".":(0,0,0,0),"D":(72,27,12,255),"R":(192,49,20,255),"O":(238,103,32,255),"H":(255,207,65,255)})

core_rows = (
    "................", "......DDDD......", ".....DYYHHD.....", "....DYYHHYYD....",
    "...DYYHIIHYYD...", "..DYYHICCIHYYD..", ".DYYHICCCCIHYYD.",
    ".DYYHICCCCIHYYD.", ".DYYHICCCCIHYYD.", "..DYYHICCIHYYD..",
    "...DYYHIIHYYD...", "....DYYHHYYD....", ".....DYYHHD.....",
    "......DDDD......", "................", "................",
)
ascii_sprite("lei_gong_core", core_rows,
    {".":(0,0,0,0),"D":(12,25,42,255),"C":(31,99,123,255),"I":(84,199,210,255),
     "Y":(246,190,57,255),"H":(255,223,108,255)})

staff_head_rows = (
    "................", "......DDDD......", ".....DYYHHD.....", "....DYYHHYYD....",
    "....DYYHIIYYD...", ".....DYYIIYD....", "......DYYD......", ".......DD.......",
    ".......DD.......", "......DWWD......", ".....DWWWWD.....", "......DWWD......",
    ".......DD.......", "................", "................", "................",
)
ascii_sprite("lightning_staff_head", staff_head_rows,
    {".":(0,0,0,0),"D":(12,25,42,255),"W":(141,170,145,255),
     "I":(84,199,210,255),"Y":(246,190,57,255),"H":(255,223,108,255)})

staff_rows = (
    "...........Y....", "..........YI....", ".........YI.....", "........YI......",
    ".......YI.......", "......YI........", ".....YI.........", "....YI..........",
    "...YI...........", "..YI............", ".YI.............", "YI..............",
    "WI..............", "WWI.............", "WWW.............", "................",
)
ascii_sprite("lightning_staff", staff_rows,
    {".":(0,0,0,0),"W":(82,62,37,255),"I":(141,170,145,255),
     "Y":(246,190,57,255)})

# Start from the established Blank Pattern texture and replace only its yellow cavity
# with a hand-placed round recess; the outer silhouette, outline and light direction match.
blank = Image.open(ASSETS / "textures/item/blank_mold.png").convert("RGBA")
for y, xs in {5:(7,8),6:(6,9),7:(5,10),8:(5,10),9:(5,10),10:(6,9),11:(7,8)}.items():
    for x in range(xs[0], xs[1] + 1):
        edge = (x in (xs[0], xs[1]) or y in (5,11))
        blank.putpixel((x,y), (118,118,118,255) if edge else (68,68,68,255))
blank_path = ASSETS / "textures/item/disk_mold.png"
blank.save(blank_path, optimize=False)
generated.append(("disk_mold", blank_path))

# TNT Disc is a direct Salur Disc texture variant: keep the exact round metal rim
# and place a hand-authored TNT emblem over its center, pixel by pixel.
tnt = Image.open(ASSETS / "textures/item/salur_disc.png").convert("RGBA")
TNT_EMBLEM = (
    "..RRRR..", ".RRHHHRR", "RRHHHHHR", "RHHHXXHR",
    "RHHHXXHR", "RRHHHHHR", ".RRHHHRR", "..RRRR..",
)
TNT_COLORS = {"R":(167,47,33,255), "H":(225,169,67,255), "X":(54,43,34,255)}
for row_index, row in enumerate(TNT_EMBLEM):
    for column_index, pixel in enumerate(row):
        if pixel != ".": tnt.putpixel((4 + column_index, 4 + row_index), TNT_COLORS[pixel])
tnt_path = ASSETS / "textures/item/tnt_disc.png"
tnt.save(tnt_path, optimize=False)
generated.append(("tnt_disc", tnt_path))

# Storm Spirit UV texture fully redrawn in the native 64x32 Blaze layout.
# Only mapped head and rod UVs are painted; every unused model UV remains transparent.
storm = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
storm_colors = {
    ".": (0, 0, 0, 0), "N": (12, 25, 42, 255), "D": (16, 43, 65, 255),
    "T": (23, 67, 90, 255), "B": (31, 99, 123, 255), "C": (45, 143, 163, 255),
    "I": (84, 199, 210, 255), "G": (215, 142, 35, 255), "Y": (246, 190, 57, 255),
    "H": (255, 223, 108, 255),
}

def paint_uv(x: int, y: int, rows: tuple[str, ...]) -> None:
    width = len(rows[0])
    if any(len(row) != width for row in rows):
        raise ValueError("inconsistent hand-drawn UV region")
    for row_index, row in enumerate(rows):
        for column_index, pixel in enumerate(row):
            if pixel != ".": storm.putpixel((x + column_index, y + row_index), storm_colors[pixel])

# New face: a readable mask, two separated cyan eyes, and a bright lightning crest.
head_front = ("NNDDDDNN", "NNTYYTNN", "NTBHHBTN", "TBHNNHBT",
              "TBIHHIBT", "DTBYYBTD", "NDTBBTDN", "NNDDDDNN")
head_top = ("NNDDDDNN", "NDTBBTDN", "DTBHHBTD", "TBCHHCTB",
            "TBCIICBT", "DTBTTBTD", "NDTBBTDN", "NNDDDDNN")
head_bottom = ("NNDDDDNN", "NDTBBTDN", "DTBTTBTD", "TBCTTCTB",
               "TBCBBCTB", "DTBTTBTD", "NDTBBTDN", "NNDDDDNN")
paint_uv(0, 8, head_front)
paint_uv(8, 8, head_front)
paint_uv(16, 8, head_front)
paint_uv(24, 8, head_front)
paint_uv(8, 0, head_top)
paint_uv(16, 0, head_bottom)

# Blaze rods now read as teal lightning columns with bright cyan cores.
rod_rows = (
    "NNDDCCDN", "NNDCICDN", "DDNCICND", "NNDCICDN",
    "NNDCICDN", "DDNCICND", "NNDCICDN", "NNDDCCDN",
)
if any(len(row) != 8 for row in rod_rows):
    raise ValueError("Blaze rod texture must be exactly 8x8")
paint_uv(0, 18, rod_rows)
paint_uv(2, 16, ("TTII", "CIIY"))
storm_path = ASSETS / "textures/entity/storm_spirit.png"
storm_path.parent.mkdir(parents=True, exist_ok=True)
storm.save(storm_path, optimize=False)
generated.append(("storm_spirit", storm_path))

# Give LeiGong's PlayerModel head a readable front face. Its head front UV is
# x=8..15, y=8..15; all edits use colors already present in the original sprite.
lei_path = ASSETS / "textures/entity/lei_gong.png"
lei = Image.open(lei_path).convert("RGBA")
face_pixels = {
    (9, 10): (255, 223, 108, 255), (10, 10): (246, 190, 57, 255),
    (13, 10): (246, 190, 57, 255), (14, 10): (255, 223, 108, 255),
    (10, 11): (12, 25, 42, 255), (11, 11): (84, 199, 210, 255),
    (13, 11): (12, 25, 42, 255), (14, 11): (84, 199, 210, 255),
    (10, 12): (45, 143, 163, 255), (11, 12): (31, 99, 123, 255),
    (13, 12): (31, 99, 123, 255), (14, 12): (45, 143, 163, 255),
    (11, 13): (215, 142, 35, 255), (12, 13): (246, 190, 57, 255),
    (13, 13): (215, 142, 35, 255),
    (10, 14): (16, 43, 65, 255), (11, 14): (12, 25, 42, 255),
    (12, 14): (12, 25, 42, 255), (13, 14): (16, 43, 65, 255),
}
for point, color in face_pixels.items():
    lei.putpixel(point, color)
lei.save(lei_path, optimize=False)
generated.append(("lei_gong_face", lei_path))

thumbs = []
for name, path in generated:
    tex = Image.open(path).convert("RGBA")
    scale = max(1, min(4, 60 // max(tex.width, tex.height)))
    tex = tex.resize((tex.width * scale, tex.height * scale), Image.Resampling.NEAREST)
    tile = Image.new("RGBA", (100, 88), (27, 31, 29, 255))
    tile.alpha_composite(tex, ((100 - tex.width)//2, 4 + (64 - tex.height)//2))
    ImageDraw.Draw(tile).text((4, 70), name[:16], fill=(238, 242, 238, 255))
    thumbs.append(tile)
columns = 6
rows = (len(thumbs) + columns - 1) // columns
atlas = Image.new("RGBA", (columns * 100, rows * 88), (18, 21, 19, 255))
for i, tile in enumerate(thumbs):
    atlas.alpha_composite(tile, ((i % columns) * 100, (i // columns) * 88))
atlas.save(ROOT / "build/salur-texture-atlas.png", optimize=False)
print(f"Generated {len(generated)} Salur textures; atlas: build/salur-texture-atlas.png")

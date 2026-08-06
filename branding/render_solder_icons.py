# -*- coding: utf-8 -*-
"""Solder icon — dovetail, margin round.

Grammar is fixed: Solder is a Sodium addon, and Sodium addons have an
established seat on the shelf (LOGO_PLAYBOOK "Sodium アドオンの席は #96E062"):
flat #96E062 field, one pure-white glyph, no gradient, shadow, outline or
second colour. Iris is not encoded by colour — a second hue would break the
grammar, and the point reads as a shape.

kura picked the dovetail (2026-08-05) and asked for a smaller glyph with more
air. This round holds the shape and varies only the margin: 20 / 24 / 28% of
the frame per side, against the round-1 drawing's 15%. The playbook's measured
reference band for this grammar is 20-25% margin, so 20 and 24 sit inside it
and 28 is one step past, in case the 15% version read as heavy for the shape
rather than for the margin.

Everything inside the square scales with it — the seam path, its width and the
corner radius are all in unit-square coordinates — so the three variants are
the same drawing at three sizes, not three drawings.

On the playbook's "グリフ占有 30-45%": that column could not be reproduced from
the five Modrinth icons it names, under white-pixel coverage, a looser
luminance threshold, or glyph-bbox area (measured 2026-08-05). Treat the band
as unverified. White-pixel coverage is reproducible, and the five references
sit at 10.6-25.4% on it (Sodium itself 14.4%); coverage is printed per variant
so the choice is made against measured references.

The four round-1 shapes kura did not pick are still rendered, suffixed
_NOT_SELECTED, so a later session cannot mistake them for live candidates.

SS=4 -> LANCZOS, per LOGO_PLAYBOOK.
"""

import os

import numpy as np
from PIL import Image, ImageDraw

SS, OUT = 4, 512
N = OUT * SS
HERE = os.path.dirname(__file__)
OUTDIR = os.path.join(HERE, "icon-candidates")
os.makedirs(OUTDIR, exist_ok=True)

BG_COLOR = (150, 224, 98)  # #96E062 — the seat three Sodium addons already share
WHITE = (255, 255, 255)


def px(v):
    """Fraction of the frame -> pixels."""
    return v * N


def rounded_bg():
    big = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    d.rounded_rectangle(
        [0, 0, N - 1, N - 1], radius=int(N * 0.20), fill=BG_COLOR + (255,)
    )
    return big


def bar(d, x0, y0, x1, y1, radius=None, fill=WHITE):
    box = [px(x0), px(y0), px(x1), px(y1)]
    if radius:
        d.rounded_rectangle(box, radius=int(px(radius)), fill=fill + (255,))
    else:
        d.rectangle(box, fill=fill + (255,))


def carved_square(canvas, seam_unit, margin, seam_w_unit=0.079, radius_unit=0.179):
    """One white rounded square with a seam cut out of it.

    seam_unit is a polyline in unit-square coordinates, so the whole drawing —
    path, seam width, corner radius — scales with the margin.
    """
    d = ImageDraw.Draw(canvas)
    a = margin
    s = 1.0 - 2.0 * margin

    d.rounded_rectangle(
        [px(a), px(a), px(a + s), px(a + s)],
        radius=int(px(radius_unit * s)),
        fill=WHITE + (255,),
    )
    d.line(
        [(px(a + x * s), px(a + y * s)) for x, y in seam_unit],
        fill=BG_COLOR + (255,),
        width=int(px(seam_w_unit * s)),
        joint="curve",
    )
    return canvas


# The seam runs past both edges of the square so its ends are never rounded off
# inside the glyph.
DOVETAIL = [
    (0.457, -0.05),
    (0.457, 0.271),
    (0.671, 0.354),
    (0.671, 0.646),
    (0.457, 0.729),
    (0.457, 1.05),
]

FINGERS = [
    (0.500, -0.05),
    (0.500, 0.214),
    (0.700, 0.214),
    (0.700, 0.400),
    (0.300, 0.400),
    (0.300, 0.600),
    (0.700, 0.600),
    (0.700, 0.786),
    (0.500, 0.786),
    (0.500, 1.05),
]

SCARF = [
    (0.279, -0.05),
    (0.279, 0.236),
    (0.721, 0.764),
    (0.721, 1.05),
]

MARGINS = [("solder_B20", 0.20), ("solder_B24", 0.24), ("solder_B28", 0.28)]


def draw_dovetail(margin):
    return lambda canvas: carved_square(canvas, DOVETAIL, margin)


# --- round-1 shapes kura did not pick, kept only as a record ------------------
def draw_parted(canvas):
    return carved_square(canvas, DOVETAIL, 0.150, seam_w_unit=0.150)


def draw_fingers(canvas):
    return carved_square(canvas, FINGERS, 0.150, seam_w_unit=0.074)


def draw_scarf(canvas):
    return carved_square(canvas, SCARF, 0.150, seam_w_unit=0.074)


def draw_mesh(canvas):
    d = ImageDraw.Draw(canvas)
    t, r = 0.115, 0.040
    top, bot = 0.150, 0.850
    bar(d, 0.100, top, 0.100 + t, bot, radius=r)
    bar(d, 0.900 - t, top, 0.900, bot, radius=r)
    for y in (0.215, 0.735 - t):
        bar(d, 0.100, y, 0.620, y + t, radius=r)
    bar(d, 0.380, 0.5 - t / 2, 0.900, 0.5 + t / 2, radius=r)
    return canvas


NOT_SELECTED = [
    ("solder_A_parted", draw_parted),
    ("solder_C_fingers", draw_fingers),
    ("solder_D_scarf", draw_scarf),
    ("solder_E_mesh", draw_mesh),
]


def metrics(img):
    """(white-pixel coverage, glyph bbox area) — both as a share of the frame."""
    a = np.asarray(
        img.convert("RGB").resize((192, 192), Image.LANCZOS), dtype=np.float64
    )
    m = np.sqrt(((a - 255) ** 2).sum(axis=2)) < 60
    ys, xs = np.nonzero(m)
    if len(xs) == 0:
        return 0.0, 0.0
    bbox = ((xs.max() - xs.min() + 1) / 192) * ((ys.max() - ys.min() + 1) / 192)
    return m.mean(), bbox


def render(draw_fn):
    return draw_fn(rounded_bg()).resize((OUT, OUT), Image.LANCZOS)


finals = {}
for name, margin in MARGINS:
    img = render(draw_dovetail(margin))
    img.save(os.path.join(OUTDIR, f"{name}.png"))
    finals[name] = img
    cover, bbox = metrics(img)
    print(f"saved {name}  margin {margin:.0%}  coverage {cover:5.1%}  bbox {bbox:5.1%}")

print(
    "\nreferences, coverage/bbox (Modrinth icons, same measurement): "
    "sodium 14.4/27.3, dynamic-lights 10.6/52.7, options-api 22.7/44.8, "
    "extras 11.9/45.5, options-mod-compat 25.4/53.2"
)

for name, draw_fn in NOT_SELECTED:
    render(draw_fn).save(os.path.join(OUTDIR, f"{name}_NOT_SELECTED.png"))
    old = os.path.join(OUTDIR, f"{name}.png")
    if os.path.exists(old):
        os.remove(old)

# --- contact sheet: the three margins, big tile + 96/48 legibility insets ---
TILE, PAD, LABEL = 320, 24, 26
cols = len(MARGINS)
sw = PAD + cols * (TILE + PAD)
sh = PAD + TILE + LABEL + PAD
sheet = Image.new("RGBA", (sw, sh), (52, 54, 60, 255))
sd = ImageDraw.Draw(sheet)

for i, (name, margin) in enumerate(MARGINS):
    t = finals[name]
    cx = PAD + i * (TILE + PAD)
    cy = PAD + LABEL
    sheet.alpha_composite(t.resize((TILE, TILE), Image.LANCZOS), (cx, cy))
    i96 = t.resize((96, 96), Image.LANCZOS)
    i48 = t.resize((48, 48), Image.LANCZOS)
    sheet.alpha_composite(i96, (cx + TILE - 96 - 6, cy + TILE - 96 - 6))
    sheet.alpha_composite(i48, (cx + TILE - 96 - 6 - 48 - 8, cy + TILE - 48 - 6))
    sd.rectangle(
        [cx + TILE - 96 - 6, cy + TILE - 96 - 6, cx + TILE - 6, cy + TILE - 6],
        outline=(150, 150, 150),
    )
    sd.rectangle(
        [
            cx + TILE - 96 - 6 - 48 - 8,
            cy + TILE - 48 - 6,
            cx + TILE - 96 - 6 - 8,
            cy + TILE - 6,
        ],
        outline=(150, 150, 150),
    )
    cover, _ = metrics(t)
    sd.text(
        (cx + 2, cy - 20),
        f"{name} — margin {margin:.0%}, coverage {cover:.1%}",
        fill=(230, 230, 230),
    )

sheet.convert("RGB").save(os.path.join(OUTDIR, "_contact_sheet.png"))
print("saved _contact_sheet.png")

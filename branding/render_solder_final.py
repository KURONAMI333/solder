# -*- coding: utf-8 -*-
"""Solder icon — confirmed asset (kura 2026-08-05: dovetail, margin 20%).

Shape and margin were chosen from render_solder_icons.py's contact sheets. This
file emits only the publish assets; the candidate rounds stay in that file with
_NOT_SELECTED suffixes.

    icon 512 / 256 / 128 / 64  -> branding/solder_icon_<size>.png
    in-jar logo (256)          -> src/main/resources/logo.png

SS=4 -> LANCZOS, per LOGO_PLAYBOOK.
"""

import os

from PIL import Image, ImageDraw

SS, OUT = 4, 512
N = OUT * SS
HERE = os.path.dirname(__file__)
RESOURCES = os.path.join(HERE, "..", "common", "src", "main", "resources")

BG_COLOR = (150, 224, 98)  # #96E062
WHITE = (255, 255, 255)

MARGIN = 0.20
SEAM_W = 0.079  # unit-square coords, so everything scales with the margin
CORNER = 0.179
DOVETAIL = [
    (0.457, -0.05),
    (0.457, 0.271),
    (0.671, 0.354),
    (0.671, 0.646),
    (0.457, 0.729),
    (0.457, 1.05),
]


def px(v):
    return v * N


def render():
    img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle(
        [0, 0, N - 1, N - 1], radius=int(N * 0.20), fill=BG_COLOR + (255,)
    )

    a = MARGIN
    s = 1.0 - 2.0 * MARGIN
    d.rounded_rectangle(
        [px(a), px(a), px(a + s), px(a + s)],
        radius=int(px(CORNER * s)),
        fill=WHITE + (255,),
    )
    d.line(
        [(px(a + x * s), px(a + y * s)) for x, y in DOVETAIL],
        fill=BG_COLOR + (255,),
        width=int(px(SEAM_W * s)),
        joint="curve",
    )
    return img.resize((OUT, OUT), Image.LANCZOS)


master = render()
for size in (512, 256, 128, 64):
    out = master if size == OUT else master.resize((size, size), Image.LANCZOS)
    path = os.path.join(HERE, f"solder_icon_{size}.png")
    out.convert("RGB").save(path)
    print("saved", os.path.basename(path))

logo = os.path.join(RESOURCES, "solder.png")
master.resize((256, 256), Image.LANCZOS).convert("RGB").save(logo)
print("saved common/src/main/resources/solder.png")

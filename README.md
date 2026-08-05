# Solder: Sodium & Iris Optimization Fix

Sodium ships a fast path for the fog and sky colours. With Iris installed, it
does not run. Solder puts it back.

## What is broken

Iris replaces one of Sodium's three sky mixins — `LevelRendererMixin`, which
suppresses the sky underwater and under blindness — and switches it off with a
package-level rule:

```
[Sodium] Force-disabling mixin 'features.render.world.sky.FogRendererMixin'
as rule 'mixin.features.render.world.sky' (added by mods [iris]) disables it and children
```

The rule takes down the whole package, so the other two mixins go with it. Those
two replace vanilla's `CubicSampler.gaussianSampleVec3` — a 6×6×6 sweep that
allocates a `Vec3` per sample point — with Sodium's `FastCubicSampler`, which
skips the blend entirely when every biome in range reports the same colour.

With Iris installed, that sweep runs again: 216 points for the fog colour and
216 for the sky colour, every frame.

## What this does

Re-applies those two redirects from a package Iris's rule does not reach. It
calls Sodium's own `FastCubicSampler` rather than reimplementing the maths, so
the output is whatever Sodium would have produced. **Nothing about the picture
changes.**

`LevelRendererMixin` is left alone. That one Iris disables on purpose.

The mixin config plugin reads the same `sodium:options` metadata Sodium reads, so
the redirects apply only when a mod has actually switched the sky package off.
Installed without Iris — or with a future Iris that drops the rule — it does
nothing, rather than colliding with Sodium's own redirect.

## What it measures

On a 116-mod client, standing still for 120 seconds:

| | without | with |
|---|---|---|
| render thread allocation | 11,938 MB | 9,551 MB (−20.0%) |
| vanilla `CubicSampler` samples | 380 | 0 |

Frame time, 5 runs per condition, alternating, clean client, 1536 MB heap:

| | without | with |
|---|---|---|
| render time (p50) | 223.2 µs | 217.6 µs |

**About 6 µs per frame.** That is a fixed cost, not a percentage: at 60 fps it is
0.04% of a frame, at 144 fps it is 0.09%. It will not move your FPS counter. What
it removes is work that should not have been running at all.

## Requirements

Client only. Sodium and Iris are both optional dependencies; without them the mod
stays inert.

## Building

```bash
./gradlew build
```

Jars land in `neoforge/build/libs/` and `fabric/build/libs/`.

Built from [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template).

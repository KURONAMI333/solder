<!--
Store fields (set separately from this body):

SUMMARY (Modrinth <=256, CurseForge one line):
Restores the fog and sky colour fast path that gets switched off when a shader
loader is installed. Same picture, about 20% less allocation on the render thread.

MODRINTH: categories = optimization; loaders = neoforge, fabric;
          versions = 1.21.1; environment = client_only
CURSEFORGE: main category = Performance; summary = as above (shortened if needed)
-->

# Solder: Sodium & Iris Optimization Fix

> Sodium ships a fast path for the fog and sky colours. With Iris installed, it does not run. This puts it back.

Iris replaces one of Sodium's three sky mixins — the one that hides the sky underwater and under blindness — and switches it off with a rule that covers the whole `features.render.world.sky` package. The other two mixins in that package go down with it. Those two are the ones that replace vanilla's 216-point colour sampler with Sodium's fast version, so with Iris installed that sampler runs again, twice per frame, for as long as you play.

This mod re-applies exactly those two, from a package the rule does not reach.

- Calls Sodium's own sampler rather than reimplementing it, so **the colours do not change**
- Leaves the third mixin alone — that one Iris disables on purpose
- Does nothing at all when the seam is not open (no Iris, or a future Iris that drops the rule)
- No configuration, no commands, no keybinds

## What it changes

Measured on a 116-mod client, standing still for 120 seconds:

| | without | with |
|---|---|---|
| Render thread allocation | 11,938 MB | 9,551 MB (−20.0%) |
| Vanilla `CubicSampler` samples | 380 | 0 |

Frame time, five runs per condition on a clean client:

| | without | with |
|---|---|---|
| Render time (median) | 223.2 µs | 217.6 µs |

That is about **6 µs per frame**, and it is a fixed cost rather than a percentage: at 60 fps it is 0.04% of a frame, at 144 fps it is 0.09%. **It will not move your FPS counter.** What it removes is work that should not have been running in the first place: a 216-point neighbourhood swept twice per frame, each point allocating a vector that is thrown away immediately.

## Supported loaders / versions

| Minecraft | NeoForge | Fabric |
|---|:---:|:---:|
| 1.21.1 | ✅ | ✅ |

Client-side only. It does nothing on a server and does not need to be installed on one.

## Install

1. Install Sodium and Iris for your loader.
2. Drop the Solder jar for the same loader into `mods/`.
3. That is all. There is nothing to configure and nothing to switch on.

## Dependencies

Both are optional in the technical sense — the mod loads without them and stays inert — but it only does something when both are present:

- [Sodium](https://modrinth.com/mod/sodium) 0.6 or newer (tested against 0.6.13 and 0.8.12)
- [Iris](https://modrinth.com/mod/iris) (tested against 1.8.8, 1.8.12 and 1.8.14-beta.1)

No Fabric API requirement.

## Compatibility & scope

The mod only applies when it finds that another mod has actually switched Sodium's sky package off. It reads the same `sodium:options` metadata Sodium reads when deciding which of its own mixins to run, so it cannot end up applying its redirect on top of Sodium's — that would be a mixin conflict, not a double speed-up.

Verified at startup on NeoForge with Sodium 0.6.13 + Iris 1.8.12, on Fabric with Sodium 0.6.13 + Iris 1.8.8, with Sodium 0.8.12 + Iris 1.8.14-beta.1, and with Sodium alone (where it correctly does nothing).

## Known limitations

- **This is not a mod that will make the game feel faster.** The effect is real and repeatable, and it is roughly 6 µs per frame. If you are looking for frames, look at Sodium itself, Entity Culling or ImmediatelyFast.
- The allocation reduction should matter most where memory is tight, but how much it matters there has not been measured.
- 1.21.1 only for now.
- Shaders on or off makes no difference: Iris applies its rule either way, so the seam is open either way.

## License & credits

MIT. The fast path itself is Sodium's work — this mod only makes sure it runs. Sodium is by CaffeineMC, Iris by the Iris Shaders team.

## Links

- Source: https://github.com/KURONAMI333/solder
- Issues: https://github.com/KURONAMI333/solder/issues

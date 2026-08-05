package com.kuronami.solder;

import net.caffeinemc.mods.sodium.client.util.color.FastCubicSampler;
import net.minecraft.util.CubicSampler;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mod's whole safety claim is that the colour does not change: it swaps
 * vanilla's sampler for Sodium's, and Sodium's is supposed to compute the same
 * thing faster. That claim is checkable without the game, because both samplers
 * are pure functions of a position and a colour field.
 *
 * <p>Both paths of Sodium's implementation are exercised: the shortcut it takes
 * when every sampled point reports the same colour (the common case in the open
 * world) and the full weighted blend it falls back to at a biome boundary.
 */
class SamplerEquivalenceTest {

    /** Vanilla builds its result from doubles; equality is up to rounding. */
    private static final double TOLERANCE = 1.0e-9;

    @Test
    void uniformColourFieldMatchesVanilla() {
        int colour = 0x7FA1FF;
        assertMatches(new Vec3(12.5, 64.25, -33.75), (x, y, z) -> colour);
    }

    @Test
    void biomeBoundaryMatchesVanilla() {
        // A plane through the sampled cube: half the points report one colour,
        // half another. This is what standing on a biome border looks like to
        // the sampler, and it is the case Sodium cannot shortcut.
        assertMatches(new Vec3(0.5, 70.5, 0.5), (x, y, z) -> x < 0 ? 0x3F76E4 : 0xC0D8FF);
    }

    @Test
    void randomColourFieldsMatchVanilla() {
        Random random = new Random(20260805L);

        for (int trial = 0; trial < 64; trial++) {
            double px = random.nextDouble() * 512.0 - 256.0;
            double py = random.nextDouble() * 256.0;
            double pz = random.nextDouble() * 512.0 - 256.0;
            long seed = random.nextLong();

            assertMatches(new Vec3(px, py, pz),
                    (x, y, z) -> (int) (mix(seed, x, y, z) & 0xFFFFFF));
        }
    }

    /** A cheap deterministic hash, so every sampled point gets its own colour. */
    private static long mix(long seed, int x, int y, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L);
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        return h;
    }

    private static void assertMatches(Vec3 pos, FastCubicSampler.ColorFetcher fetcher) {
        Vec3 vanilla = CubicSampler.gaussianSampleVec3(pos,
                (x, y, z) -> Vec3.fromRGB24(fetcher.fetch(x, y, z)));
        Vec3 sodium = FastCubicSampler.sampleColor(pos, fetcher, Function.identity());

        assertTrue(vanilla.distanceTo(sodium) <= TOLERANCE,
                () -> "at " + pos + ": vanilla " + vanilla + " vs sodium " + sodium);
    }
}

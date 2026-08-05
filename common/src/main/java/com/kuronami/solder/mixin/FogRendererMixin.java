package com.kuronami.solder.mixin;

import net.caffeinemc.mods.sodium.client.util.color.FastCubicSampler;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Restores Sodium's fog-colour fast path when Iris has switched it off.
 *
 * <p>Iris disables Sodium's whole {@code features.render.world.sky} package
 * through a mixin rule. Its documented reason concerns sky rendering underwater
 * and under blindness, which is one of the three mixins in that package. The
 * other two go down with it, and vanilla's
 * {@link CubicSampler#gaussianSampleVec3} runs again: a 6×6×6 sweep that builds
 * an immutable {@link Vec3} per sample point, every frame, twice.
 *
 * <p>Measured on a 116-mod client standing still, that path was roughly 20% of
 * the render thread's allocation, and {@code Vec3} was the single most-allocated
 * type in the process.
 *
 * <p>This re-applies the same redirect from a package Iris's rule does not
 * reach. It calls Sodium's own {@code FastCubicSampler} rather than
 * reimplementing the maths, so the output is whatever Sodium would have
 * produced — the win is skipping the 216-point blend entirely when every biome
 * in range reports the same fog colour, which is the usual case.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Redirect(
            method = "setupColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3("
                            + "Lnet/minecraft/world/phys/Vec3;"
                            + "Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)"
                            + "Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 solder$fastSampleFogColour(Vec3 pos, CubicSampler.Vec3Fetcher fetcher,
                                                   Camera camera, float tickDelta, ClientLevel level,
                                                   int renderDistance, float bossColorModifier) {
        float brightness = Mth.clamp(
                Mth.cos(level.getTimeOfDay(tickDelta) * 6.2831855F) * 2.0F + 0.5F, 0.0F, 1.0F);

        return FastCubicSampler.sampleColor(
                pos,
                (x, y, z) -> level.getBiomeManager().getNoiseBiomeAtQuart(x, y, z).value().getFogColor(),
                color -> level.effects().getBrightnessDependentFogColor(color, brightness));
    }
}

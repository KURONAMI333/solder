package com.kuronami.solder.mixin;

import net.caffeinemc.mods.sodium.client.util.color.FastCubicSampler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.CubicSampler;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

/**
 * The sky-colour half of the same seam {@link FogRendererMixin} covers.
 *
 * <p>Reading all three of Sodium's sky mixins makes clear that only one is what
 * Iris actually wants gone: {@code LevelRendererMixin} injects into
 * {@code renderSky} to suppress the sky underwater and under blindness, which is
 * precisely the behaviour Iris says it replaces with its own approach. The other
 * two — {@code FogRendererMixin} and this one — only swap vanilla's 216-point
 * {@link CubicSampler} sweep for an equivalent fast path. They are collateral,
 * and this restores the second of them, the same way and for the same reason.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Redirect(
            method = "getSkyColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3("
                            + "Lnet/minecraft/world/phys/Vec3;"
                            + "Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)"
                            + "Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 solder$fastSampleSkyColour(Vec3 pos, CubicSampler.Vec3Fetcher fetcher) {
        Level level = (Level) (Object) this;

        return FastCubicSampler.sampleColor(
                pos,
                (x, y, z) -> level.getNoiseBiome(x, y, z).value().getSkyColor(),
                Function.identity());
    }
}

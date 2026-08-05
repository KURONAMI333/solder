package com.kuronami.solder;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies the redirects only where the seam is actually open.
 *
 * <p>Reads the rules from the same place Sodium's own NeoForge implementation
 * does — {@code ModInfo#getConfigElement("sodium:options")} over the loading mod
 * list. Sodium applies mod overrides after the user's
 * {@code config/sodium-mixins.properties}, so a mod's value is the final word
 * and the user file does not need checking.
 */
public final class NeoForgeSeamGuard implements IMixinConfigPlugin {

    private boolean applicable;

    @Override
    public void onLoad(String mixinPackage) {
        boolean sodium = modPresent("sodium");
        SeamRule.Resolver resolver = new SeamRule.Resolver();

        LoadingModList mods = FMLLoader.getLoadingModList();
        if (mods != null) {
            for (ModInfo mod : mods.getMods()) {
                if (mod.getConfigElement(SeamRule.SODIUM_OPTIONS).orElse(null) instanceof Map<?, ?> rules) {
                    resolver.offerAll(rules);
                }
            }
        }

        // A JVM flag rather than a config option: the A/B measurement needs the
        // two conditions to differ by the redirects and nothing else, and a
        // config screen is one more thing that could differ between runs.
        applicable = sodium && resolver.seamIsOpen() && !Boolean.getBoolean("solder.disabled");

        // One line, because the only question a bug report needs answered about
        // this mod is whether it did anything at all.
        Constants.LOG.info("sodium={} seam={} -> redirects {}",
                sodium, resolver.decidingRule(), applicable ? "applied" : "not applied");
    }

    private static boolean modPresent(String id) {
        LoadingModList mods = FMLLoader.getLoadingModList();
        return mods != null && mods.getModFileById(id) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return applicable;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo info) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo info) {}
}

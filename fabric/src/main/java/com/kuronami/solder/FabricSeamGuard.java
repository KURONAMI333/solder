package com.kuronami.solder;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Fabric half of the guard: same question as the NeoForge one, same source,
 * read through Fabric's metadata API. Mods declare the rules in
 * {@code fabric.mod.json} under {@code custom."sodium:options"}.
 *
 * <p>The two loaders get their own class because reading loader metadata is the
 * one thing that genuinely cannot be shared; the resolution they feed is in
 * {@link SeamRule}.
 */
public final class FabricSeamGuard implements IMixinConfigPlugin {

    private boolean applicable;

    @Override
    public void onLoad(String mixinPackage) {
        FabricLoader loader = FabricLoader.getInstance();
        boolean sodium = loader.isModLoaded("sodium");
        SeamRule.Resolver resolver = new SeamRule.Resolver();

        for (ModContainer mod : loader.getAllMods()) {
            CustomValue declared = mod.getMetadata().getCustomValue(SeamRule.SODIUM_OPTIONS);
            if (declared == null || declared.getType() != CustomValue.CvType.OBJECT) {
                continue;
            }
            for (Map.Entry<String, CustomValue> rule : declared.getAsObject()) {
                if (rule.getValue().getType() == CustomValue.CvType.BOOLEAN) {
                    resolver.offer(rule.getKey(), rule.getValue().getAsBoolean());
                }
            }
        }

        applicable = sodium && resolver.seamIsOpen() && !Boolean.getBoolean("solder.disabled");

        Constants.LOG.info("sodium={} seam={} -> redirects {}",
                sodium, resolver.decidingRule(), applicable ? "applied" : "not applied");
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

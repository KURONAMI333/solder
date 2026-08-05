package com.kuronami.solder;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Entry point. Everything this mod does happens in the mixins and in
 * {@link NeoForgeSeamGuard}, which run before this class is constructed; javafml
 * requires the annotated class to exist, so it exists.
 */
@Mod(Constants.MOD_ID)
public final class Solder {

    public Solder(IEventBus eventBus) {}
}

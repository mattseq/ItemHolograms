package com.mattseq.item_holograms;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class ItemHologramsMod {

    public ItemHologramsMod() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        CommonClass.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ItemHologramsConfig.SPEC, "item_holograms-client.toml");
    }
}
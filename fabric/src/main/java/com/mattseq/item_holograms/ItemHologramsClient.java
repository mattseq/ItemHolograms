package com.mattseq.item_holograms;

import com.mattseq.item_holograms.events.RenderEventHandler;
import net.fabricmc.api.ClientModInitializer;

public class ItemHologramsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemHologramsConfig.load();
        RenderEventHandler.regster();
    }
}

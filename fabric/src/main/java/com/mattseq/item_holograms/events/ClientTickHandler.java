package com.mattseq.item_holograms.events;

import com.mattseq.item_holograms.ItemLabelCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ClientTickHandler {
    private static long lastCleanupTime = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Level world = Minecraft.getInstance().level;

            if (world == null) return;

            long gameTime = world.getGameTime();
            if (gameTime - lastCleanupTime > 600) {
                ItemLabelCache.cache.entrySet().removeIf(entry -> world.getEntity(entry.getKey()) == null);
                lastCleanupTime = gameTime;
            }
        });
    }
}

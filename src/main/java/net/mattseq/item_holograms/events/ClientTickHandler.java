package net.mattseq.item_holograms.events;

import net.mattseq.item_holograms.ItemHolograms;
import net.mattseq.item_holograms.ItemLabelCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickHandler {
    private static long lastCleanupTime = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Level world = net.minecraft.client.Minecraft.getInstance().level;

        if (world == null) return;

        long gameTime = world.getGameTime();
        if (gameTime - lastCleanupTime > 600) {
            cleanupCache(world);
            lastCleanupTime = gameTime;
        }
    }

    private static void cleanupCache(Level world) {
        int oldCacheSize = ItemLabelCache.cache.size();
        ItemLabelCache.cache.entrySet().removeIf(entry -> world.getEntity(entry.getKey()) == null);
        ItemHolograms.LOGGER.debug("Cleaned up {} stale item labels", oldCacheSize - ItemLabelCache.cache.size());
        ItemHolograms.LOGGER.debug("Cache size is now {}", ItemLabelCache.cache.size());
    }
}

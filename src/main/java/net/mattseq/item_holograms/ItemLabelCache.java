package net.mattseq.item_holograms;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemLabelCache {
    public static class CachedLabel {
        public Component label;
        public ItemStack lastStack;
        public boolean visibleLOS;
        public long lastUpdate;

        public CachedLabel(Component label, ItemStack stack, boolean visibleLOS, long lastUpdate) {
            this.label = label;
            this.lastStack = stack.copy();
            this.visibleLOS = visibleLOS;
            this.lastUpdate = lastUpdate;
        }
    }

    public static final Map<Integer, CachedLabel> cache = new HashMap<>();

    public static CachedLabel get(int id) {
        return cache.get(id);
    }

    public static void put(int id, CachedLabel label) {
        cache.put(id, label);
    }

    public static void remove(int id) {
        cache.remove(id);
    }

    public static void clear() {
        cache.clear();
    }
}

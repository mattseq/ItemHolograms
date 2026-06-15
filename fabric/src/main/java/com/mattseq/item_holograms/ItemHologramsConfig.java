package com.mattseq.item_holograms;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ItemHologramsConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("item_holograms.properties");

    private static final Properties PROPS = new Properties();

    public static class ConfigValue<T> {
        private T value;
        public ConfigValue(T initial) { this.value = initial; }
        public T get() { return value; }
        public void set(T v) { this.value = v; }
    }

    public static final ConfigValue<Boolean> ENABLE_TOOLTIPS = new ConfigValue<>(true);
    public static final ConfigValue<Double> LABEL_DISPLAY_DISTANCE = new ConfigValue<>(10.0);
    public static final ConfigValue<Boolean> ENABLE_HOVER_ANIMATION = new ConfigValue<>(true);
    public static final ConfigValue<Boolean> MINIMAL = new ConfigValue<>(false);

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                // populate props from file
                PROPS.load(in);

                // populate ConfigValues
                readValues();
            } catch (IOException e) {
                Constants.LOG.error("Failed to load item_holograms config: {}", e.getMessage());
            }
        } else {
            try {
                // ensure config dir exists and create default file
                Files.createDirectories(CONFIG_PATH.getParent());

                // populate PROPS from default ConfigValues
                writeValues();

                try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                    PROPS.store(out, "Item Holograms Config");
                }
            } catch (IOException e) {
                Constants.LOG.error("Failed to create item_holograms config: {}", e.getMessage());
            }
        }
    }

    private static void readValues() {
        ENABLE_TOOLTIPS.set(Boolean.parseBoolean(PROPS.getProperty("enableTooltips", "true")));
        LABEL_DISPLAY_DISTANCE.set(Double.parseDouble(PROPS.getProperty("labelDisplayDistance", "10.0")));
        ENABLE_HOVER_ANIMATION.set(Boolean.parseBoolean(PROPS.getProperty("enableHoverAnimation", "true")));
        MINIMAL.set(Boolean.parseBoolean(PROPS.getProperty("minimal", "false")));
    }

    private static void writeValues() {
        PROPS.setProperty("enableTooltips", Boolean.toString(ENABLE_TOOLTIPS.get()));
        PROPS.setProperty("labelDisplayDistance", Double.toString(LABEL_DISPLAY_DISTANCE.get()));
        PROPS.setProperty("enableHoverAnimation", Boolean.toString(ENABLE_HOVER_ANIMATION.get()));
        PROPS.setProperty("minimal", Boolean.toString(MINIMAL.get()));
    }
}

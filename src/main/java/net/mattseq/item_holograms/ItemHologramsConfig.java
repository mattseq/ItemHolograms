package net.mattseq.item_holograms;

import net.minecraftforge.common.ForgeConfigSpec;

public class ItemHologramsConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_TOOLTIPS;
    public static final ForgeConfigSpec.ConfigValue<Double> LABEL_DISPLAY_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_HOVER_ANIMATION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MINIMAL;

    static {
        BUILDER.push("Configs for Damage Indicator");

        ENABLE_TOOLTIPS = BUILDER
                .comment("Enable tooltips")
                .define("enableTooltips", true);

        LABEL_DISPLAY_DISTANCE = BUILDER
                .comment("Distance at which item labels are displayed (in blocks)")
                .defineInRange("labelDisplayDistance", 10.0, 1.0, 100.0);

        ENABLE_HOVER_ANIMATION = BUILDER
                .comment("Enable hover animation for item labels")
                .define("enableHoverAnimation", true);

        MINIMAL = BUILDER
                .comment("Show holograms only when looking at item and tooltips only when sneaking")
                .define("minimal", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

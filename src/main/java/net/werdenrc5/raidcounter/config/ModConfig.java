package net.werdenrc5.raidcounter.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.werdenrc5.raidcounter.RaidCounterMod;

@Mod.EventBusSubscriber(modid = RaidCounterMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    
    // Display config entries
    public static final ForgeConfigSpec.BooleanValue SHOW_RAIDER_COUNTER;
    public static final ForgeConfigSpec.BooleanValue SHOW_WAVE_COUNTER;
    public static final ForgeConfigSpec.BooleanValue USE_COLOR_CODING;
    
    // Position config entries
    public static final ForgeConfigSpec.IntValue RAIDER_COUNTER_X;
    public static final ForgeConfigSpec.IntValue RAIDER_COUNTER_Y;
    public static final ForgeConfigSpec.IntValue WAVE_COUNTER_X_OFFSET;
    public static final ForgeConfigSpec.IntValue WAVE_COUNTER_Y;
    
    // Scale config entry
    public static final ForgeConfigSpec.DoubleValue HUD_SCALE;
    
    //color config entries
    public static final ForgeConfigSpec.IntValue ILLAGER_COLOR;
    public static final ForgeConfigSpec.IntValue RAIDER_COLOR;
    public static final ForgeConfigSpec.IntValue SPELLCASTER_COLOR;
    
    static {
        BUILDER.push("Display Settings");
        
        SHOW_RAIDER_COUNTER = BUILDER
                .comment("Enable or disable the raider counter display")
                .define("showRaiderCounter", true);
        
        SHOW_WAVE_COUNTER = BUILDER
                .comment("Enable or disable the wave counter display")
                .define("showWaveCounter", true);
        
        USE_COLOR_CODING = BUILDER
                .comment("Enable or disable color coding for different raider types")
                .define("useColorCoding", true);
        
        BUILDER.pop();
        
        BUILDER.push("Position Settings");
        
        RAIDER_COUNTER_X = BUILDER
                .comment("X position of the raider counter (from left edge of screen)")
                .defineInRange("raiderCounterX", 5, 0, 4000);
        
        RAIDER_COUNTER_Y = BUILDER
                .comment("Y position of the raider counter (from top edge of screen)")
                .defineInRange("raiderCounterY", 5, 0, 4000);
        
        WAVE_COUNTER_X_OFFSET = BUILDER
                .comment("X offset for wave counter from screen center (negative values move left, positive move right)")
                .defineInRange("waveCounterXOffset", 0, -2000, 2000);
        
        WAVE_COUNTER_Y = BUILDER
                .comment("Y position of the wave counter (from top edge of screen)")
                .defineInRange("waveCounterY", 30, 0, 4000);
        
        BUILDER.pop();
        
        BUILDER.push("Scale Settings");
        
        HUD_SCALE = BUILDER
                .comment("Scale factor for both HUD elements (1.0 = normal size)")
                .defineInRange("hudScale", 1.0, 0.5, 2.0);
        
        BUILDER.pop();
        
        BUILDER.push("Color Settings");
        
        ILLAGER_COLOR = BUILDER
                .comment("Color for regular illagers (format: 0xAARRGGBB)")
                .defineInRange("illagerColor", 0xFFFFFFFF, Integer.MIN_VALUE, Integer.MAX_VALUE);
        
        RAIDER_COLOR = BUILDER
                .comment("Color for non-illager raiders (format: 0xAARRGGBB)")
                .defineInRange("raiderColor", 0xFFFFFF00, Integer.MIN_VALUE, Integer.MAX_VALUE);
        
        SPELLCASTER_COLOR = BUILDER
                .comment("Color for spellcaster illagers (format: 0xAARRGGBB)")
                .defineInRange("spellcasterColor", 0xFFFFA500, Integer.MIN_VALUE, Integer.MAX_VALUE);
        
        BUILDER.pop();
    }
    
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        RaidCounterMod.LOGGER.info("Loaded Raid Counter config: {}", event.getConfig().getFileName());
    }
    
    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        RaidCounterMod.LOGGER.info("Raid Counter config reloaded");
    }

    public static ForgeConfigSpec.ConfigValue<Integer> getByTitle(String title) {
    switch (title) {
        case "Illager Color": return ILLAGER_COLOR;
        case "Raider Color": return RAIDER_COLOR;
        case "Spellcaster Color": return SPELLCASTER_COLOR;
        default: throw new IllegalArgumentException("Unknown color title: " + title);
    }
}
}
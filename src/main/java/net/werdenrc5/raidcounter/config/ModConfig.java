package net.werdenrc5.raidcounter.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.werdenrc5.raidcounter.RaidCounterMod;

@Mod.EventBusSubscriber(modid = RaidCounterMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    
    // Config entries
    public static final ForgeConfigSpec.BooleanValue SHOW_RAIDER_COUNTER;
    public static final ForgeConfigSpec.BooleanValue SHOW_WAVE_COUNTER;
    
    static {
        BUILDER.push("Display Settings");
        
        SHOW_RAIDER_COUNTER = BUILDER
                .comment("Enable or disable the raider counter display")
                .define("showRaiderCounter", true);
        
        SHOW_WAVE_COUNTER = BUILDER
                .comment("Enable or disable the wave counter display")
                .define("showWaveCounter", true);
        
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
}
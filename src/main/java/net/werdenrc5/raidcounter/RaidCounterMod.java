package net.werdenrc5.raidcounter;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.werdenrc5.raidcounter.client.ConfigScreen;
import net.werdenrc5.raidcounter.client.HudOverlay;
import net.werdenrc5.raidcounter.config.ModConfig;
import net.werdenrc5.raidcounter.network.RaiderCountMessage;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(RaidCounterMod.MOD_ID)
public class RaidCounterMod {
    public static final String MOD_ID = "raidcounter";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MOD_ID, "main"),
        () -> "1.0",
        "1.0"::equals,
        "1.0"::equals
    );

    public RaidCounterMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        
        // Register config
        ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfig.SPEC);
        
        MinecraftForge.EVENT_BUS.register(this);
        
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerOverlays);
            // Register config screen
            ConfigScreen.register();
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        INSTANCE.registerMessage(0, RaiderCountMessage.class,
                RaiderCountMessage::encode,
                RaiderCountMessage::decode,
                RaiderCountMessage::handle
        );
        
        LOGGER.info("RaidCounter network setup complete");
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase == Phase.START || !(event.player instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;
        if (player.tickCount % 20 == 0) { // Update every second
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            
            // Check if there's an active raid
            Raid vanillaRaid = level.getRaidAt(pos);
            boolean raidActive = vanillaRaid != null && vanillaRaid.isActive();
            
            // Wave tracking
            int waveNumber = 0;
            int totalWaves = 0;
            
            // Only collect raider data if there's an active raid
            Map<String, Integer> raiderMap = new HashMap<>();
            if (raidActive && vanillaRaid != null) {
                checkAllRaiders(level, pos, raiderMap);
                
                // Get wave information - Fixed: Don't add 1 to getGroupsSpawned()
                waveNumber = vanillaRaid.getGroupsSpawned();
                
                // Get total waves based on difficulty
                Difficulty difficulty = level.getDifficulty();
                int numGroups = vanillaRaid.getNumGroups(difficulty);
                if (numGroups <= 0) {
                    // Fallback calculation
                    int badOmenLevel = vanillaRaid.getBadOmenLevel();
                    numGroups = Math.max(1, Math.min(6, badOmenLevel + 1));
                    LOGGER.debug("getNumGroups returned invalid value, using fallback: {}", numGroups);
                }
                totalWaves = numGroups;
                
                LOGGER.debug("Current raid wave: {}/{}", waveNumber, totalWaves);
            }

            // Always send the message to update the client's raid status and raider data
            LOGGER.debug("Sending raid data to player: active={}, raiders={}, wave={}/{}",
                raidActive, raiderMap.size(), waveNumber, totalWaves);
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new RaiderCountMessage(raiderMap, raidActive, waveNumber, totalWaves));
        }
    }

    private void checkAllRaiders(ServerLevel level, BlockPos pos, Map<String, Integer> map) {
        Raid vanillaRaid = level.getRaidAt(pos);
        if (vanillaRaid != null && vanillaRaid.isActive()) {
            vanillaRaid.getAllRaiders().forEach(raider -> {
                String id = EntityType.getKey(raider.getType()).toString();
                map.put(id, map.getOrDefault(id, 0) + 1);
            });
        }

        level.getEntities((Entity) null,
            new AABB(pos).inflate(96),
            e -> e instanceof Raider && isPartOfAnyRaid(e) && 
                 (vanillaRaid == null || !vanillaRaid.getAllRaiders().contains(e))
        ).forEach(entity -> {
            String id = EntityType.getKey(entity.getType()).toString();
            map.put(id, map.getOrDefault(id, 0) + 1);
        });
    }

    private boolean isPartOfAnyRaid(Entity entity) {
        return entity instanceof Raider raider && raider.getCurrentRaid() != null;
    }

    private void registerOverlays(RegisterGuiOverlaysEvent event) {
        LOGGER.info("Registering RaidCounter HUD overlay");
        event.registerAboveAll("raiders", HudOverlay.RAID_COUNTER);
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel 
            && event.getEntity() instanceof Raider raider) {
            Raid raid = serverLevel.getRaidAt(raider.blockPosition());
            if (raid != null && raid.isActive()) {
                Difficulty difficulty = serverLevel.getDifficulty();
                int numGroups = raid.getNumGroups(difficulty);
                if (numGroups <= 0) {
                    int badOmenLevel = raid.getBadOmenLevel();
                    numGroups = Math.max(1, Math.min(6, badOmenLevel + 1));
                }
                
                LOGGER.debug("Detected raider joining an active raid: {}, wave {}/{}",
                    EntityType.getKey(raider.getType()).toString(),
                    raid.getGroupsSpawned(),
                    numGroups);
            }
        }
    }
}
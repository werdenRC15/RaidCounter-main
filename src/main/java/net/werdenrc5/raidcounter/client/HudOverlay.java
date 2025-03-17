package net.werdenrc5.raidcounter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;
import net.werdenrc5.raidcounter.config.ModConfig;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HudOverlay {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final int MARGIN = 5;
    private static final int LINE_HEIGHT = 12;

    public static final IGuiOverlay RAID_COUNTER = (gui, guiGraphics, partialTick, width, height) -> {
        if (mc.options.hideGui || mc.screen != null) return;

        // Only show HUD if raid is active
        if (!ClientRaiderCountData.isRaidActive()) return;

        // Check config settings
        boolean showWaveCounter = ModConfig.SHOW_WAVE_COUNTER.get();
        boolean showRaiderCounter = ModConfig.SHOW_RAIDER_COUNTER.get();

        // If both are disabled, don't show anything
        if (!showWaveCounter && !showRaiderCounter) return;

        Font font = mc.font;
        
        // Position the HUD in the top-center for wave info (under vanilla raid bar)
        if (showWaveCounter) {
            int waveNumber = ClientRaiderCountData.getWaveNumber();
            int totalWaves = ClientRaiderCountData.getTotalWaves();
            String waveText = "Wave: " + waveNumber + "/" + totalWaves;
            
            int centerX = width / 2;
            int waveWidth = font.width(waveText);
            int waveX = centerX - (waveWidth / 2);
            int waveY = 30; // Position below vanilla raid bar
            
            // Draw wave with background for better visibility
            int padding = 2;
            int bgColor = 0x80000000; // Semi-transparent black
            guiGraphics.fill(waveX - padding, waveY - padding, 
                             waveX + waveWidth + padding, waveY + font.lineHeight + padding, 
                             bgColor);
            
            // Draw wave text centered
            guiGraphics.drawString(
                font,
                Component.literal(waveText),
                waveX, waveY, 0xFFFFFF, true
            );
        }
        
        // Only display raider counter if enabled
        if (showRaiderCounter) {
            Map<String, Integer> raiders = ClientRaiderCountData.getRaiderMap();
            
            // Position raider info in the corner
            int x = MARGIN;
            int y = MARGIN;
            
            // Draw raider count header
            guiGraphics.drawString(
                font,
                Component.literal("Active Raiders:"),
                x, y, 0xFFFFFF, true
            );
            y += LINE_HEIGHT;
            
            if (!raiders.isEmpty()) {
                AtomicInteger yPos = new AtomicInteger(y);
                
                raiders.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                    .forEachOrdered(entry -> {
                        String name = getEntityName(entry.getKey());
                        String count = entry.getValue().toString();
                        String text = "• " + name + ": " + count;
                        guiGraphics.drawString(
                            font,
                            Component.literal(text),
                            x, yPos.get(), 0xAAAAAA, true
                        );
                        yPos.addAndGet(LINE_HEIGHT);
                    });
            } else {
                guiGraphics.drawString(
                    font,
                    Component.literal("There are no raiders"),
                    x, y, 0xAAAAAA, true
                );
            }
        }
    };

    private static String getEntityName(String entityId) {
        ResourceLocation res = ResourceLocation.tryParse(entityId);
        if (res != null) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(res);
            if (type != null) return type.getDescription().getString();
            return formatEntityId(res.getPath());
        }
        return formatEntityId(entityId);
    }

    private static String formatEntityId(String path) {
        return Arrays.stream(path.split("_"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
}
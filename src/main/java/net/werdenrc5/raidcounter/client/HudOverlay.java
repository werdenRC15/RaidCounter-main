package net.werdenrc5.raidcounter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;
import net.werdenrc5.raidcounter.config.ModConfig;
import net.werdenrc5.raidcounter.util.RaiderTypeUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HudOverlay {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final int PADDING = 2;
    private static final int BG_COLOR = 0x80000000; // Semi-transparent black

    public static final IGuiOverlay RAID_COUNTER = (gui, guiGraphics, partialTick, width, height) -> {
        if (mc.options.hideGui || mc.screen != null) return;

        // Only show HUD if raid is active
        if (!ClientRaiderCountData.isRaidActive()) return;

        // Check config settings
        boolean showWaveCounter = ModConfig.SHOW_WAVE_COUNTER.get();
        boolean showRaiderCounter = ModConfig.SHOW_RAIDER_COUNTER.get();
        boolean useColorCoding = ModConfig.USE_COLOR_CODING.get();
        
        // Get scale setting
        float scale = (float) ModConfig.HUD_SCALE.get().doubleValue();

        // If both are disabled, don't show anything
        if (!showWaveCounter && !showRaiderCounter) return;

        Font font = mc.font;
        // Scale line height based on config
        int lineHeight = (int) (12 * scale);
        
        // Position the HUD for wave info based on config
        if (showWaveCounter) {
            int waveNumber = ClientRaiderCountData.getWaveNumber();
            int totalWaves = ClientRaiderCountData.getTotalWaves();
            String waveText = "Wave: " + waveNumber + "/" + totalWaves;
            
            int centerX = width / 2;
            int waveWidth = (int)(font.width(waveText) * scale);
            int xOffset = ModConfig.WAVE_COUNTER_X_OFFSET.get();
            int waveX = centerX - (waveWidth / 2) + xOffset;
            int waveY = ModConfig.WAVE_COUNTER_Y.get();
            
            // Apply scaling
            GuiGraphics scaled = applyScaling(guiGraphics, scale, waveX, waveY);
            
            // Draw wave with background for better visibility
            int scaledWidth = font.width(waveText);
            int scaledLineHeight = font.lineHeight;
            
            scaled.fill(-PADDING, -PADDING, 
                       scaledWidth + PADDING, scaledLineHeight + PADDING, 
                       BG_COLOR);
            
            // Draw wave text centered
            scaled.drawString(
                font,
                Component.literal(waveText),
                0, 0, 0xFFFFFF, true
            );
            
            // Restore scaling
            restoreScaling(guiGraphics);
        }
        
        // Only display raider counter if enabled
        if (showRaiderCounter) {
            Map<String, Integer> raiders = ClientRaiderCountData.getRaiderMap();
            
            // Position raider info based on config
            int x = ModConfig.RAIDER_COUNTER_X.get();
            int y = ModConfig.RAIDER_COUNTER_Y.get();
            
            // Calculate dimensions for background
            int headerWidth = font.width("Active Raiders:");
            int maxLineWidth = headerWidth;
            
            // Pre-sort and calculate max width for background
            Map.Entry<String, Integer>[] sortedEntries = raiders.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
                .toArray(Map.Entry[]::new);
            
            for (Map.Entry<String, Integer> entry : sortedEntries) {
                String name = getEntityName(entry.getKey());
                String count = entry.getValue().toString();
                
                //prefix indicator based on raider type
                String prefix = getRaiderTypePrefix(entry.getKey());
                String text = "• " + prefix + name + ": " + count;
                
                int lineWidth = font.width(text);
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
            }
            
            // If no raiders, calculate "no raiders" text width
            if (raiders.isEmpty()) {
                int noRaidersWidth = font.width("There are no raiders");
                maxLineWidth = Math.max(maxLineWidth, noRaidersWidth);
            }
            
            // Calculate background height
            int bgHeight = lineHeight; // Start with header height
            bgHeight += raiders.isEmpty() ? lineHeight : (sortedEntries.length * lineHeight);
            
            // Apply scaling
            GuiGraphics scaled = applyScaling(guiGraphics, scale, x, y);
            
            // Draw background
            int scaledMaxLineWidth = maxLineWidth;
            int scaledBgHeight = (raiders.isEmpty() ? font.lineHeight + font.lineHeight : 
                                 font.lineHeight + sortedEntries.length * font.lineHeight);
            
            scaled.fill(-PADDING, -PADDING, 
                       scaledMaxLineWidth + PADDING, 
                       scaledBgHeight + PADDING, 
                       BG_COLOR);
            
            // Draw raider count header
            scaled.drawString(
                font,
                Component.literal("Active Raiders:"),
                0, 0, 0xFFFFFF, true
            );
            int currentY = font.lineHeight;
            
            if (!raiders.isEmpty()) {
                for (Map.Entry<String, Integer> entry : sortedEntries) {
                    String entityId = entry.getKey();
                    String name = getEntityName(entityId);
                    String count = entry.getValue().toString();
                    
                    //prefix indicator based on raider type
                    String prefix = getRaiderTypePrefix(entityId);
                    String text = "• " + prefix + name + ": " + count;
                    
                    // Get color based on raider type
                    int color = RaiderTypeUtil.getRaiderColor(entityId, useColorCoding);
                    
                    scaled.drawString(
                        font,
                        Component.literal(text),
                        0, currentY, color, true
                    );
                    currentY += font.lineHeight;
                }
            } else {
                scaled.drawString(
                    font,
                    Component.literal("There are no raiders"),
                    0, currentY, 0xAAAAAA, true
                );
            }
            
            // Restore scaling
            restoreScaling(guiGraphics);
        }
    };
    
    /**
     * Applies scaling transformation to GuiGraphics
     */
    private static GuiGraphics applyScaling(GuiGraphics graphics, float scale, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        return graphics;
    }
    
    /**
     * Restores graphics to original state
     */
    private static void restoreScaling(GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    /**
     * Returns a prefix indicator for the raider type
     */
    private static String getRaiderTypePrefix(String entityId) {
        if (!ModConfig.USE_COLOR_CODING.get()) return "";
        
        int raiderType = RaiderTypeUtil.getRaiderType(entityId);

        return switch (raiderType) {
            case RaiderTypeUtil.TYPE_ILLAGER -> "[I] "; // AbstractIllager
            case RaiderTypeUtil.TYPE_RAIDER -> "[R] "; // Raider (non-illager)
            case RaiderTypeUtil.TYPE_SPELLCASTER -> "[S] "; // SpellcasterIllager
            default -> "";
        };
    }

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
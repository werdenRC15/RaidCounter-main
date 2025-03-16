package net.werdenrc5.raidcounter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

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

        Map<String, Integer> raiders = ClientRaiderCountData.getRaiderMap();
        Font font = mc.font;
        int x = MARGIN;
        
        guiGraphics.drawString(
            font,
            Component.literal("Active Raiders:"),
            x, MARGIN, 0xFFFFFF, true
        );
        
        if (!raiders.isEmpty()) {
            AtomicInteger y = new AtomicInteger(MARGIN + LINE_HEIGHT);
            
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
                        x, y.get(), 0xAAAAAA, true
                    );
                    y.addAndGet(LINE_HEIGHT);
                });
        } else {
            guiGraphics.drawString(
                font,
                Component.literal("There is no raiders"),
                x, MARGIN + LINE_HEIGHT, 0xAAAAAA, true
            );
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
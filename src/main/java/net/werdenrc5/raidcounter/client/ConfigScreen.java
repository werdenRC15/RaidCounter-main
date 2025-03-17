package net.werdenrc5.raidcounter.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.werdenrc5.raidcounter.RaidCounterMod;
import net.werdenrc5.raidcounter.config.ModConfig;

public class ConfigScreen extends Screen {
    private final Screen parentScreen;
    private OptionsList optionsList;

    public ConfigScreen(Screen parentScreen) {
        super(Component.literal("Raid Counter Settings"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.optionsList = new OptionsList(
            this.minecraft, this.width, this.height,
            32, this.height - 32, 25
        );

        // Add config options
        this.optionsList.addBig(OptionInstance.createBoolean(
            "Show raider counter",
            OptionInstance.noTooltip(),
            ModConfig.SHOW_RAIDER_COUNTER.get(),
            (value) -> ModConfig.SHOW_RAIDER_COUNTER.set(value)
        ));

        this.optionsList.addBig(OptionInstance.createBoolean(
            "Show wave counter",
            OptionInstance.noTooltip(),
            ModConfig.SHOW_WAVE_COUNTER.get(),
            (value) -> ModConfig.SHOW_WAVE_COUNTER.set(value)
        ));

        this.addRenderableWidget(this.optionsList);

        // Done button
        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
            ModConfig.SPEC.save(); // Explicitly save config
            this.minecraft.setScreen(parentScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        optionsList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        ModConfig.SPEC.save(); // Save on exit
        this.minecraft.setScreen(parentScreen);
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (minecraft, screen) -> new ConfigScreen(screen)
            )
        );
    }
}
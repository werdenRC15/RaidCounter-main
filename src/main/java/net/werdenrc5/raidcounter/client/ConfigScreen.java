package net.werdenrc5.raidcounter.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.minecraftforge.fml.ModLoadingContext;
import net.werdenrc5.raidcounter.config.ModConfig;

import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private final Screen parentScreen;
    private OptionsList optionsList;
    private ColorWheelScreen currentColorScreen = null;

    // Used for color preview
    private int illagerColorPreview;
    private int raiderColorPreview;
    private int spellcasterColorPreview;

    public ConfigScreen(Screen parentScreen) {
        super(Component.literal("Raid Counter Settings"));
        this.parentScreen = parentScreen;
        this.illagerColorPreview = ModConfig.ILLAGER_COLOR.get();
        this.raiderColorPreview = ModConfig.RAIDER_COLOR.get();
        this.spellcasterColorPreview = ModConfig.SPELLCASTER_COLOR.get();
    }

    @Override
    protected void init() {
        this.optionsList = new OptionsList(
            this.minecraft, this.width, this.height,
            32, this.height - 32, 25
        );

        // Display settings
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

        this.optionsList.addBig(OptionInstance.createBoolean(
                "Show raider types",
                OptionInstance.noTooltip(),
                ModConfig.USE_COLOR_CODING.get(),
                (value) -> ModConfig.USE_COLOR_CODING.set(value)
        ));

        // Position settings
        // Raider Counter position
        this.optionsList.addBig(new OptionInstance<>(
                "Raider Counter X",
                OptionInstance.noTooltip(),
                (component, value) -> Component.literal(component.getString() + ": " + value),
                new OptionInstance.IntRange(0, 4000),
                ModConfig.RAIDER_COUNTER_X.get(),
                (value) -> ModConfig.RAIDER_COUNTER_X.set(value)
        ));

        this.optionsList.addBig(new OptionInstance<>(
                "Raider Counter Y",
                OptionInstance.noTooltip(),
                (component, value) -> Component.literal(component.getString() + ": " + value),
                new OptionInstance.IntRange(0, 4000),
                ModConfig.RAIDER_COUNTER_Y.get(),
                (value) -> ModConfig.RAIDER_COUNTER_Y.set(value)
        ));

        // Wave Counter position
        this.optionsList.addBig(new OptionInstance<>(
                "Wave Counter X Offset",
                OptionInstance.noTooltip(),
                (component, value) -> Component.literal(component.getString() + ": " + value),
                new OptionInstance.IntRange(-2000, 2000),
                ModConfig.WAVE_COUNTER_X_OFFSET.get(),
                (value) -> ModConfig.WAVE_COUNTER_X_OFFSET.set(value)
        ));

        this.optionsList.addBig(new OptionInstance<>(
                "Wave Counter Y",
                OptionInstance.noTooltip(),
                (component, value) -> Component.literal(component.getString() + ": " + value),
                new OptionInstance.IntRange(0, 4000),
                ModConfig.WAVE_COUNTER_Y.get(),
                (value) -> ModConfig.WAVE_COUNTER_Y.set(value)
        ));

        // Scale setting
        // Fix for HUD Scale - Use UnitDouble.INSTANCE with xmap to convert between ranges
        this.optionsList.addBig(new OptionInstance<>(
                "HUD Scale",
                OptionInstance.noTooltip(),
                (component, value) -> Component.literal(component.getString() + ": " + String.format("%.1f", value)),
                OptionInstance.UnitDouble.INSTANCE.xmap(
                    unitValue -> 0.5 + unitValue * 1.5,  // Map 0-1 to 0.5-2.0
                    scaleValue -> (scaleValue - 0.5) / 1.5  // Map 0.5-2.0 back to 0-1
                ),
                ModConfig.HUD_SCALE.get(),
                (value) -> ModConfig.HUD_SCALE.set(value)
        ));

        this.addRenderableWidget(this.optionsList);

        Button colorSettingsButton = Button.builder(
                Component.literal("Color Settings"),
                button -> openColorSettingsScreen()
        ).bounds(this.width / 2 - 100, this.height - 27, 90, 20).build();
        this.addRenderableWidget(colorSettingsButton);

        Button resetButton = Button.builder(
                Component.literal("Reset to Defaults"),
                button -> resetToDefaults()
        ).bounds(this.width - 210, this.height - 27, 90, 20).build();
        this.addRenderableWidget(resetButton);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
            ModConfig.SPEC.save(); // Explicitly save config
            this.minecraft.setScreen(parentScreen);
        }).bounds(10, this.height - 27, 90, 20).build());
    }

    private void optionsList(int i) {
        // Empty method
    }

    private Component getColorText(String title, int color) {
        String hex = String.format("#%08X", color);
        return Component.literal(title + ": " + hex);
    }

    private void resetToDefaults() {
        // Reset display settings
        ModConfig.SHOW_RAIDER_COUNTER.set(true);
        ModConfig.SHOW_WAVE_COUNTER.set(true);
        ModConfig.USE_COLOR_CODING.set(true);
        
        // Reset position settings
        ModConfig.RAIDER_COUNTER_X.set(5);
        ModConfig.RAIDER_COUNTER_Y.set(5);
        ModConfig.WAVE_COUNTER_X_OFFSET.set(0);
        ModConfig.WAVE_COUNTER_Y.set(30);
        
        // Reset scale
        ModConfig.HUD_SCALE.set(1.0);
        
        // Reset colors
        ModConfig.ILLAGER_COLOR.set(0xFFFFFFFF);
        ModConfig.RAIDER_COLOR.set(0xFFFFFF00);
        ModConfig.SPELLCASTER_COLOR.set(0xFFFFA500);

        // Update preview colors
        this.illagerColorPreview = ModConfig.ILLAGER_COLOR.get();
        this.raiderColorPreview = ModConfig.RAIDER_COLOR.get();
        this.spellcasterColorPreview = ModConfig.SPELLCASTER_COLOR.get();
        
        // Save changes
        ModConfig.SPEC.save();

        this.minecraft.setScreen(null);        
        // Refresh screen to show updated values
        this.minecraft.setScreen(new ConfigScreen(parentScreen));
    }

    private void updateIllagerColor(int color) {
        this.illagerColorPreview = color;
    }

    private void updateRaiderColor(int color) {
        this.raiderColorPreview = color;
    }

    private void updateSpellcasterColor(int color) {
        this.spellcasterColorPreview = color;
    }

    private void openColorWheel(String title, int initialColor, Consumer<Integer> previewHandler, Consumer<Integer> saveHandler) {
        this.minecraft.setScreen(new ColorWheelScreen(
            this,
            Component.literal(title),
            initialColor,
            previewHandler,
            saveHandler
        ));
    }

    private void openColorSettingsScreen() {
        this.minecraft.setScreen(new ColorSettingsScreen(this));
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

    public static class ColorSettingsScreen extends Screen {
        private final Screen parentScreen;
        private int illagerColorPreview;
        private int raiderColorPreview;
        private int spellcasterColorPreview;

        public ColorSettingsScreen(Screen parentScreen) {
            super(Component.literal("Color Settings"));
            this.parentScreen = parentScreen;
            this.illagerColorPreview = ModConfig.ILLAGER_COLOR.get();
            this.raiderColorPreview = ModConfig.RAIDER_COLOR.get();
            this.spellcasterColorPreview = ModConfig.SPELLCASTER_COLOR.get();
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int buttonWidth = 200;
            int buttonHeight = 20;
            int startY = 50;
            int spacing = 30;

            // Illager Color Button
            this.addRenderableWidget(Button.builder(
                getColorText("Illager Color", illagerColorPreview),
                button -> openColorWheel(
                    "Illager Color",
                    illagerColorPreview,
                    this::updateIllagerColor,
                    color -> {
                        illagerColorPreview = color;
                        button.setMessage(getColorText("Illager Color", color));
                        ModConfig.ILLAGER_COLOR.set(color);
                    }
                )
            ).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

            // Raider Color Button
            this.addRenderableWidget(Button.builder(
                getColorText("Raider Color", raiderColorPreview),
                button -> openColorWheel(
                    "Raider Color",
                    raiderColorPreview,
                    this::updateRaiderColor,
                    color -> {
                        raiderColorPreview = color;
                        button.setMessage(getColorText("Raider Color", color));
                        ModConfig.RAIDER_COLOR.set(color);
                    }
                )
            ).bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

            // Spellcaster Color Button
            this.addRenderableWidget(Button.builder(
                getColorText("Spellcaster Color", spellcasterColorPreview),
                button -> openColorWheel(
                    "Spellcaster Color",
                    spellcasterColorPreview,
                    this::updateSpellcasterColor,
                    color -> {
                        spellcasterColorPreview = color;
                        button.setMessage(getColorText("Spellcaster Color", color));
                        ModConfig.SPELLCASTER_COLOR.set(color);
                    }
                )
            ).bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());

            this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                button -> {
                    ModConfig.SPEC.save(); // Save changes
                    this.minecraft.setScreen(parentScreen);
                }
            ).bounds(centerX - buttonWidth / 2, this.height - 40, buttonWidth, buttonHeight).build());
        }

        private Component getColorText(String title, int color) {
            String hex = String.format("#%08X", color);
            return Component.literal(title + ": " + hex);
        }

        private void updateIllagerColor(int color) {
            this.illagerColorPreview = color;
        }

        private void updateRaiderColor(int color) {
            this.raiderColorPreview = color;
        }

        private void updateSpellcasterColor(int color) {
            this.spellcasterColorPreview = color;
        }

        private void openColorWheel(String title, int initialColor, Consumer<Integer> previewHandler, Consumer<Integer> saveHandler) {
            this.minecraft.setScreen(new ColorWheelScreen(
                this,
                Component.literal(title),
                initialColor,
                previewHandler,
                saveHandler
            ));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(guiGraphics);
            guiGraphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

            // Show color previews
            int previewSize = 20;
            int previewY = 160;
            int spacing = 30;
            int labelOffset = 5;

            // Illager color preview
            int illagerX = width / 4;
            guiGraphics.fill(illagerX - previewSize/2, previewY - previewSize/2, 
                           illagerX + previewSize/2, previewY + previewSize/2, 
                           illagerColorPreview);
            guiGraphics.drawString(font, "Illager", illagerX - 20, previewY + previewSize/2 + labelOffset, 0xFFFFFF);

            // Raider color preview
            int raiderX = width / 2;
            guiGraphics.fill(raiderX - previewSize/2, previewY - previewSize/2, 
                           raiderX + previewSize/2, previewY + previewSize/2, 
                           raiderColorPreview);
            guiGraphics.drawString(font, "Raider", raiderX - 20, previewY + previewSize/2 + labelOffset, 0xFFFFFF);

            // Spellcaster color preview
            int spellcasterX = 3 * width / 4;
            guiGraphics.fill(spellcasterX - previewSize/2, previewY - previewSize/2, 
                           spellcasterX + previewSize/2, previewY + previewSize/2, 
                           spellcasterColorPreview);
            guiGraphics.drawString(font, "Spellcaster", spellcasterX - 30, previewY + previewSize/2 + labelOffset, 0xFFFFFF);
            
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            ModConfig.SPEC.save(); // Save on exit
            this.minecraft.setScreen(parentScreen);
        }
    }

    // Inner class for color wheel screen
    public static class ColorWheelScreen extends Screen {
        private final Screen parentScreen;
        private int currentColor;
        private int hue = 0;
        private int saturation = 0;
        private int value = 100;
        private int alpha = 255;

        private ForgeSlider hueSlider;
        private ForgeSlider saturationSlider;
        private ForgeSlider valueSlider;
        private ForgeSlider alphaSlider;

        private final Consumer<Integer> previewHandler;
        private final Consumer<Integer> saveHandler;
        private final int initialColorValue;

        private static final int WHEEL_SIZE = 150;
        private static final int WHEEL_X_CENTER = 100;
        private static final int WHEEL_Y_CENTER = 100;

        public ColorWheelScreen(Screen parentScreen, Component title, int initialColor,
                                Consumer<Integer> previewHandler, Consumer<Integer> saveHandler) {
            super(title);
            this.parentScreen = parentScreen;
            this.currentColor = initialColor;
            this.previewHandler = previewHandler;
            this.saveHandler = saveHandler;
            this.initialColorValue = initialColor;

            // Extract HSVA values from initial color
            int r = (initialColor >> 16) & 0xFF;
            int g = (initialColor >> 8) & 0xFF;
            int b = initialColor & 0xFF;
            this.alpha = (initialColor >> 24) & 0xFF;

            float[] hsv = rgbToHsv(r, g, b);
            this.hue = (int) (hsv[0] * 360);
            this.saturation = (int) (hsv[1] * 100);
            this.value = (int) (hsv[2] * 100);
        }

        @Override
        protected void init() {
            int sliderWidth = 180;
            int sliderX = this.width / 2 - sliderWidth / 2;
            int startY = this.height / 2 - 50;

            // Hue slider (0-360)
            this.hueSlider = new ForgeSlider(
                    sliderX, startY, sliderWidth, 20,
                    Component.literal("Hue: "), Component.literal(""),
                    0, 360, this.hue, 1, 1, true
            );
            this.addRenderableWidget(this.hueSlider);

            // Saturation slider (0-100)
            this.saturationSlider = new ForgeSlider(
                    sliderX, startY + 25, sliderWidth, 20,
                    Component.literal("Saturation: "), Component.literal("%"),
                    0, 100, this.saturation, 1, 1, true
            );
            this.addRenderableWidget(this.saturationSlider);

            // Value slider (0-100)
            this.valueSlider = new ForgeSlider(
                    sliderX, startY + 50, sliderWidth, 20,
                    Component.literal("Value: "), Component.literal("%"),
                    0, 100, this.value, 1, 1, true
            );
            this.addRenderableWidget(this.valueSlider);

            // Alpha slider (0-255)
            this.alphaSlider = new ForgeSlider(
                    sliderX, startY + 75, sliderWidth, 20,
                    Component.literal("Alpha: "), Component.literal(""),
                    0, 255, this.alpha, 1, 1, true
            );
            this.addRenderableWidget(this.alphaSlider);

            this.addRenderableWidget(Button.builder(
                    Component.literal("Apply"),
                    button -> {
                        this.saveHandler.accept(this.currentColor);
                        this.onClose();
                    }
            ).bounds(this.width / 2 - 100, this.height - 50, 90, 20).build());

            this.addRenderableWidget(Button.builder(
                Component.literal("Cancel"),
                button -> {
                    this.previewHandler.accept(this.initialColorValue); // Revert preview
                    this.onClose();
                }
            ).bounds(this.width / 2 + 10, this.height - 50, 90, 20).build());
        }

        @Override
        public void tick() {
            super.tick();

            // Get updated values from sliders
            int newHue = (int) this.hueSlider.getValue();
            int newSaturation = (int) this.saturationSlider.getValue();
            int newValue = (int) this.valueSlider.getValue();
            int newAlpha = (int) this.alphaSlider.getValue();

            // Check if any value changed
            if (newHue != this.hue || newSaturation != this.saturation ||
                    newValue != this.value || newAlpha != this.alpha) {

                // Update stored values
                this.hue = newHue;
                this.saturation = newSaturation;
                this.value = newValue;
                this.alpha = newAlpha;

                // Convert HSV to RGB
                float h = this.hue / 360.0f;
                float s = this.saturation / 100.0f;
                float v = this.value / 100.0f;

                int rgb = hsvToRgb(h, s, v);
                this.currentColor = (this.alpha << 24) | (rgb & 0x00FFFFFF);

                // Preview the color
                this.previewHandler.accept(this.currentColor);
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(guiGraphics);

            // Draw title
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

            // Draw current color preview
            int previewX = 10;
            int previewY = 40; 
            int previewSize = 60;

            // Color swatch with border
            guiGraphics.fill(
                    previewX - 2, previewY - 2,
                    previewX + previewSize + 2, previewY + previewSize + 2,
                    0xFFFFFFFF  // White border
            );
            guiGraphics.fill(
                    previewX, previewY,
                    previewX + previewSize, previewY + previewSize,
                    this.currentColor
            );

            // Draw color info
            String hexString = String.format("#%08X", this.currentColor);
            guiGraphics.drawString(this.font, "Current Color:", previewX, previewY + previewSize + 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, hexString, previewX, previewY + previewSize + 22, 0xFFFFFF);

            // Draw color wheel visual (circle with gradient)
            int wheelX = this.width / 2 + 90;
            int wheelY = this.height / 2 - 90;
            int wheelRadius = 30;

            // Draw color wheel background
            for (int x = -wheelRadius; x <= wheelRadius; x++) {
                for (int y = -wheelRadius; y <= wheelRadius; y++) {
                    int distSquared = x * x + y * y;
                    if (distSquared <= wheelRadius * wheelRadius) {
                        // Convert position to angle and distance (polar coordinates)
                        double angle = Math.atan2(y, x);
                        double distance = Math.sqrt(distSquared);

                        // Convert to HSV
                        float h = (float) ((angle / (2 * Math.PI) + 0.5) % 1.0); // Hue from angle
                        float s = (float) (distance / wheelRadius); // Saturation from distance

                        // Convert HSV to RGB
                        int color = hsvToRgb(h, s, 1.0f);

                        guiGraphics.fill(
                                wheelX + x, wheelY + y,
                                wheelX + x + 1, wheelY + y + 1,
                                0xFF000000 | color
                        );
                    }
                }
            }

            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(this.parentScreen);
        }

        // Convert RGB to HSV
        private float[] rgbToHsv(int r, int g, int b) {
            float rf = r / 255.0f;
            float gf = g / 255.0f;
            float bf = b / 255.0f;

            float max = Math.max(rf, Math.max(gf, bf));
            float min = Math.min(rf, Math.min(gf, bf));
            float delta = max - min;

            float h = 0;
            float s = 0;
            float v = max;

            if (max != 0) {
                s = delta / max;

                if (rf == max) {
                    h = (gf - bf) / delta;
                } else if (gf == max) {
                    h = 2 + (bf - rf) / delta;
                } else {
                    h = 4 + (rf - gf) / delta;
                }

                h *= 60;
                if (h < 0) h += 360;
            }

            return new float[]{h / 360.0f, s, v};
        }

        // Convert HSV to RGB
        private int hsvToRgb(float h, float s, float v) {
            if (s == 0) {
                // Achromatic (grey)
                int grey = Math.round(v * 255);
                return (grey << 16) | (grey << 8) | grey;
            }

            h = h * 6;
            int i = (int) Math.floor(h);
            float f = h - i;
            float p = v * (1 - s);
            float q = v * (1 - s * f);
            float t = v * (1 - s * (1 - f));

            float r, g, b;
            switch (i % 6) {
                case 0:
                    r = v;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = v;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = v;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = v;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = v;
                    break;
                default:
                    r = v;
                    g = p;
                    b = q;
                    break;
            }

            int red = Math.round(r * 255);
            int green = Math.round(g * 255);
            int blue = Math.round(b * 255);

            return (red << 16) | (green << 8) | blue;
        }
    }
}
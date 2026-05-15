package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.minecraftforge.network.PacketDistributor;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.EOPTagPacket;
import java.util.ArrayList;
import java.util.List;

public class EOPExtrasScreen extends Screen {

    private static final ResourceLocation CLIMB_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/climb.png");
    private static final ResourceLocation CLIMB_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/climb_hovered.png");
    private static final ResourceLocation CLIMB_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/climb_on.png");

    private static final ResourceLocation NIGHT_VISION_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/night_vision.png");
    private static final ResourceLocation NIGHT_VISION_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/night_vision_hovered.png");
    private static final ResourceLocation NIGHT_VISION_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/night_vision_on.png");

    private static final ResourceLocation SMELTING_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/smelt.png");
    private static final ResourceLocation SMELTING_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/smelt_hovered.png");
    private static final ResourceLocation SMELTING_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/smelt_on.png");

    private static final ResourceLocation FIRE_RESISTANCE_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/fire_resistance.png");
    private static final ResourceLocation FIRE_RESISTANCE_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/fire_resistance_hovered.png");
    private static final ResourceLocation FIRE_RESISTANCE_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/fire_resistance_on.png");

    private static final ResourceLocation ENTITY_SENSE_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/entity_sense.png");
    private static final ResourceLocation ENTITY_SENSE_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/entity_sense_hovered.png");
    private static final ResourceLocation ENTITY_SENSE_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/entity_sense_on.png");

    private static final ResourceLocation SUPER_JUMP_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/jump_boost.png");
    private static final ResourceLocation SUPER_JUMP_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/jump_boost_hovered.png");
    private static final ResourceLocation SUPER_JUMP_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/jump_boost_on.png");

    private static final ResourceLocation ERASE_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/erase.png");
    private static final ResourceLocation ERASE_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/erase_hovered.png");
    private static final ResourceLocation ERASE_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/erase_on.png");

    private static final ResourceLocation EXTRA_REACH_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/extra_reach.png");
    private static final ResourceLocation EXTRA_REACH_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/extra_reach_hovered.png");
    private static final ResourceLocation EXTRA_REACH_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/extra_reach_on.png");

    private static final ResourceLocation SLOW_FALL_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/slow_fall.png");
    private static final ResourceLocation SLOW_FALL_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/slow_fall_hovered.png");
    private static final ResourceLocation SLOW_FALL_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/slow_fall_on.png");

    private static final ResourceLocation LIGHT_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/light.png");
    private static final ResourceLocation LIGHT_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/light_hovered.png");
    private static final ResourceLocation LIGHT_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/light_on.png");

    private static final ResourceLocation WATER_BREATHING_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/water_breathing.png");
    private static final ResourceLocation WATER_BREATHING_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/water_breathing_hovered.png");
    private static final ResourceLocation WATER_BREATHING_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/water_breathing_on.png");

    private static final ResourceLocation FROST_WALKER_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/frost_walk.png");
    private static final ResourceLocation FROST_WALKER_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/frost_walk_hovered.png");
    private static final ResourceLocation FROST_WALKER_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/frost_walk_on.png");

    private static boolean climbToggled = false;
    private static boolean nightVisionToggled = false;
    private static boolean smeltingToggled = false;
    private static boolean fireResistanceToggled = false;
    private static boolean entitySenseToggled = false;
    private static boolean superJumpToggled = false;
    private static boolean eraseToggled = false;
    private static boolean extraReachToggled = false;
    private static boolean slowFallToggled = false;
    private static boolean lightToggled = false;
    private static boolean waterBreathingToggled = false;
    private static boolean frostWalkerToggled = false;

    private final int buttonSize = 43;

    public EOPExtrasScreen() {
        super(Component.literal("EOP Extras"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x88000000, 0x88000000);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        List<ExtraButton> buttons = getAvailableButtons();

        if (buttons.isEmpty()) {
            String text = "No Extra Abilities";
            int textWidth = this.font.width(text);

            guiGraphics.drawString(
                    this.font,
                    text,
                    (this.width / 2) - (textWidth / 2),
                    this.height / 2,
                    0xFFFFFFFF,
                    true
            );

            return;
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int radius;

        if (buttons.size() <= 1) {
            radius = 0;
        } else {
            radius = 38 + ((buttons.size() - 2) * 7);
        }

        for (int i = 0; i < buttons.size(); i++) {
            ExtraButton extraButton = buttons.get(i);

            double angle = -Math.PI / 2.0D + ((Math.PI * 2.0D) * i / buttons.size());

            int buttonX = centerX + (int) (Math.cos(angle) * radius) - (buttonSize / 2);
            int buttonY = centerY + (int) (Math.sin(angle) * radius) - (buttonSize / 2);

            drawToggleButton(
                    guiGraphics,
                    mouseX,
                    mouseY,
                    buttonX,
                    buttonY,
                    extraButton.toggled(),
                    extraButton.off(),
                    extraButton.hovered(),
                    extraButton.on()
            );
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<ExtraButton> buttons = getAvailableButtons();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int radius;

        if (buttons.size() <= 1) {
            radius = 0;
        } else {
            radius = 38 + ((buttons.size() - 2) * 7);
        }

        for (int i = 0; i < buttons.size(); i++) {
            ExtraButton extraButton = buttons.get(i);

            double angle = -Math.PI / 2.0D + ((Math.PI * 2.0D) * i / buttons.size());

            int buttonX = centerX + (int) (Math.cos(angle) * radius) - (buttonSize / 2);
            int buttonY = centerY + (int) (Math.sin(angle) * radius) - (buttonSize / 2);

            if (clickedButton(mouseX, mouseY, buttonX, buttonY)) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(
                            SoundEvents.UI_BUTTON_CLICK.get(),
                            1.0F,
                            1.0F
                    );
                }

                toggleButton(extraButton.id());
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private List<ExtraButton> getAvailableButtons() {
        List<ExtraButton> buttons = new ArrayList<>();

        var player = Minecraft.getInstance().player;

        if (player == null) {
            return buttons;
        }

        if (EOPPalladiumProperties.hasClimbExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Climb", climbToggled, CLIMB_OFF, CLIMB_HOVERED, CLIMB_ON));}
        if (EOPPalladiumProperties.hasNightVisionExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Night.Vision", nightVisionToggled, NIGHT_VISION_OFF, NIGHT_VISION_HOVERED, NIGHT_VISION_ON));}
        if (EOPPalladiumProperties.hasSmeltingExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Smelting", smeltingToggled, SMELTING_OFF, SMELTING_HOVERED, SMELTING_ON));}
        if (EOPPalladiumProperties.hasFireResistanceExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Fire.Resistance", fireResistanceToggled, FIRE_RESISTANCE_OFF, FIRE_RESISTANCE_HOVERED, FIRE_RESISTANCE_ON));}
        if (EOPPalladiumProperties.hasEntitySenseExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Entity.Sense", entitySenseToggled, ENTITY_SENSE_OFF, ENTITY_SENSE_HOVERED, ENTITY_SENSE_ON));}
        if (EOPPalladiumProperties.hasSuperJumpExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Super.Jump", superJumpToggled, SUPER_JUMP_OFF, SUPER_JUMP_HOVERED, SUPER_JUMP_ON));}
        if (EOPPalladiumProperties.hasEraseExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Erase", eraseToggled, ERASE_OFF, ERASE_HOVERED, ERASE_ON));}
        if (EOPPalladiumProperties.hasExtraReachExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Extra.Reach", extraReachToggled, EXTRA_REACH_OFF, EXTRA_REACH_HOVERED, EXTRA_REACH_ON));}
        if (EOPPalladiumProperties.hasSlowFallExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Slow.Fall", slowFallToggled, SLOW_FALL_OFF, SLOW_FALL_HOVERED, SLOW_FALL_ON));}
        if (EOPPalladiumProperties.hasLightExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Light", lightToggled, LIGHT_OFF, LIGHT_HOVERED, LIGHT_ON));}
        if (EOPPalladiumProperties.hasWaterBreathingExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Water.Breathing", waterBreathingToggled, WATER_BREATHING_OFF, WATER_BREATHING_HOVERED, WATER_BREATHING_ON));}
        if (EOPPalladiumProperties.hasFrostWalkerExtra(player)) {buttons.add(new ExtraButton("EOP.Extra.Frost.Walker", frostWalkerToggled, FROST_WALKER_OFF, FROST_WALKER_HOVERED, FROST_WALKER_ON));}


        return buttons;
    }

    private void drawToggleButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            boolean toggled,
            ResourceLocation off,
            ResourceLocation hoveredTexture,
            ResourceLocation on
    ) {
        boolean hovered = mouseX >= x
                && mouseX < x + buttonSize
                && mouseY >= y
                && mouseY < y + buttonSize;

        ResourceLocation texture = toggled
                ? on
                : hovered ? hoveredTexture : off;

        guiGraphics.blit(
                texture,
                x,
                y,
                0,
                0,
                buttonSize,
                buttonSize,
                buttonSize,
                buttonSize
        );
    }

    private boolean clickedButton(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x
                && mouseX < x + buttonSize
                && mouseY >= y
                && mouseY < y + buttonSize;
    }

    private void toggleButton(String id) {
        switch (id) {
            case "EOP.Extra.Climb" -> {climbToggled = !climbToggled;updateTag("EOP.Climbing.On", climbToggled);}
            case "EOP.Extra.Night.Vision" -> {nightVisionToggled = !nightVisionToggled;updateTag("EOP.Night.Vision.On", nightVisionToggled);}
            case "EOP.Extra.Smelting" -> {smeltingToggled = !smeltingToggled;updateTag("EOP.Smelting.On", smeltingToggled);}
            case "EOP.Extra.Fire.Resistance" -> {fireResistanceToggled = !fireResistanceToggled;updateTag("EOP.Smelting.On", fireResistanceToggled);}
            case "EOP.Extra.Entity.Sense" -> {entitySenseToggled = !entitySenseToggled;updateTag("EOP.Smelting.On", entitySenseToggled);}
            case "EOP.Extra.Super.Jump" -> {superJumpToggled = !superJumpToggled;updateTag("EOP.Smelting.On", superJumpToggled);}
            case "EOP.Extra.Erase" -> {eraseToggled = !eraseToggled;updateTag("EOP.Smelting.On", eraseToggled);}
            case "EOP.Extra.Extra.Reach" -> {extraReachToggled = !extraReachToggled;updateTag("EOP.Smelting.On", extraReachToggled);}
            case "EOP.Extra.Slow.Fall" -> {slowFallToggled = !slowFallToggled;updateTag("EOP.Smelting.On", slowFallToggled);}
            case "EOP.Extra.Light" -> {lightToggled = !lightToggled;updateTag("EOP.Smelting.On", lightToggled);}
            case "EOP.Extra.Water.Breathing" -> {waterBreathingToggled = !waterBreathingToggled;updateTag("EOP.Smelting.On", waterBreathingToggled);}
            case "EOP.Extra.Frost.Walker" -> {frostWalkerToggled = !frostWalkerToggled;updateTag("EOP.Smelting.On", frostWalkerToggled);}
        }
    }

    private void updateTag(String tag, boolean enabled) {
        EOPNetwork.CHANNEL.sendToServer(new EOPTagPacket(tag, enabled));
    }

    private record ExtraButton(
            String id,
            boolean toggled,
            ResourceLocation off,
            ResourceLocation hovered,
            ResourceLocation on
    ) {}
}
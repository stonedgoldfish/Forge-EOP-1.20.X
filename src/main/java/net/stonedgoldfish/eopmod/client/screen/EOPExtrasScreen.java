package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.EOPTagPacket;

import java.util.ArrayList;
import java.util.List;

public class EOPExtrasScreen extends Screen {

    private static final ResourceLocation BACKGROUND_PANEL =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/eop_border_full.png");

    private static final ResourceLocation NEXT_PAGE =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_panels/plaque.png");

    private static final ResourceLocation NEXT_PAGE_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_panels/plaque_hover.png");

    private static final int PANEL_SIZE = 800;

    private static final ResourceLocation CLIMB_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/climb.png");
    private static final ResourceLocation CLIMB_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/climb_hovered.png");
    private static final ResourceLocation CLIMB_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/climb_on.png");

    private static final ResourceLocation NIGHT_VISION_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/night_vision.png");
    private static final ResourceLocation NIGHT_VISION_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/night_vision_hovered.png");
    private static final ResourceLocation NIGHT_VISION_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/night_vision_on.png");

    private static final ResourceLocation SMELTING_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/smelt.png");
    private static final ResourceLocation SMELTING_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/smelt_hovered.png");
    private static final ResourceLocation SMELTING_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/smelt_on.png");

    private static final ResourceLocation FIRE_RESISTANCE_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/fire_resistance.png");
    private static final ResourceLocation FIRE_RESISTANCE_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/fire_resistance_hovered.png");
    private static final ResourceLocation FIRE_RESISTANCE_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/fire_resistance_on.png");

    private static final ResourceLocation ENTITY_SENSE_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/entity_sense.png");
    private static final ResourceLocation ENTITY_SENSE_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/entity_sense_hovered.png");
    private static final ResourceLocation ENTITY_SENSE_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/entity_sense_on.png");

    private static final ResourceLocation SUPER_JUMP_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/jump_boost.png");
    private static final ResourceLocation SUPER_JUMP_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/jump_boost_hovered.png");
    private static final ResourceLocation SUPER_JUMP_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/jump_boost_on.png");

    private static final ResourceLocation ERASE_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/erase.png");
    private static final ResourceLocation ERASE_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/erase_hovered.png");
    private static final ResourceLocation ERASE_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/erase_on.png");

    private static final ResourceLocation EXTRA_REACH_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/extra_reach.png");
    private static final ResourceLocation EXTRA_REACH_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/extra_reach_hovered.png");
    private static final ResourceLocation EXTRA_REACH_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/extra_reach_on.png");

    private static final ResourceLocation SLOW_FALL_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/slow_fall.png");
    private static final ResourceLocation SLOW_FALL_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/slow_fall_hovered.png");
    private static final ResourceLocation SLOW_FALL_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/slow_fall_on.png");

    private static final ResourceLocation LIGHT_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/light.png");
    private static final ResourceLocation LIGHT_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/light_hovered.png");
    private static final ResourceLocation LIGHT_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/light_on.png");

    private static final ResourceLocation WATER_BREATHING_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/water_breathing.png");
    private static final ResourceLocation WATER_BREATHING_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/water_breathing_hovered.png");
    private static final ResourceLocation WATER_BREATHING_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/water_breathing_on.png");

    private static final ResourceLocation FROST_WALKER_OFF = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/frost_walk.png");
    private static final ResourceLocation FROST_WALKER_HOVERED = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/frost_walk_hovered.png");
    private static final ResourceLocation FROST_WALKER_ON = ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/frost_walk_on.png");

    private static boolean initializedFromTags = false;

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
    private static final int BUTTONS_PER_PAGE = 12;
    private static int currentPage = 0;

    private final int pageButtonWidth = 60;
    private final int pageButtonHeight = 30;

    public EOPExtrasScreen() {
        super(Component.literal("EOP Extras"));
    }

    @Override
    protected void init() {
        super.init();

        if (initializedFromTags) {
            return;
        }

        var player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        climbToggled = player.getTags().contains("EOP.Climbing.On");
        nightVisionToggled = player.getTags().contains("EOP.Night.Vision.On");
        smeltingToggled = player.getTags().contains("EOP.Smelting.On");
        fireResistanceToggled = player.getTags().contains("EOP.Fire.Resistance.On");
        entitySenseToggled = player.getTags().contains("EOP.Entity.Sense.On");
        superJumpToggled = player.getTags().contains("EOP.Super.Jump.On");
        eraseToggled = player.getTags().contains("EOP.Erase.On");
        extraReachToggled = player.getTags().contains("EOP.Extra.Reach.On");
        slowFallToggled = player.getTags().contains("EOP.Slow.Fall.On");
        lightToggled = player.getTags().contains("EOP.Light.On");
        waterBreathingToggled = player.getTags().contains("EOP.Water.Breathing.On");
        frostWalkerToggled = player.getTags().contains("EOP.Frost.Walker.On");

        initializedFromTags = true;
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

        List<ExtraButton> allButtons = getAvailableButtons();
        int totalPages = Math.max(1, (int) Math.ceil(allButtons.size() / (double) BUTTONS_PER_PAGE));

        if (currentPage >= totalPages) {
            currentPage = 0;
        }

        List<ExtraButton> buttons = getButtonsForCurrentPage(allButtons);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int panelX = centerX - (PANEL_SIZE / 2);
        int panelY = centerY - (PANEL_SIZE / 2);

        guiGraphics.blit(
                BACKGROUND_PANEL,
                panelX,
                panelY,
                0,
                0,
                PANEL_SIZE,
                PANEL_SIZE,
                PANEL_SIZE,
                PANEL_SIZE
        );

        drawUtilityTitle(guiGraphics, panelX, panelY);

        if (buttons.isEmpty()) {
            String text = "No Extra Abilities";
            int textWidth = this.font.width(text);

            guiGraphics.drawString(
                    this.font,
                    text,
                    centerX - (textWidth / 2),
                    centerY,
                    0xFFFFFFFF,
                    true
            );

            return;
        }

        int radius = buttons.size() <= 1 ? 0 : 38 + ((buttons.size() - 2) * 7);
        ExtraButton hoveredButton = null;

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

            if (isHoveringButton(mouseX, mouseY, buttonX, buttonY)) {
                hoveredButton = extraButton;
            }
        }

        if (hoveredButton != null) {
            drawHoveredText(guiGraphics, hoveredButton, panelX, panelY);
        }

        if (allButtons.size() > BUTTONS_PER_PAGE) {
            drawPageButton(guiGraphics, mouseX, mouseY, panelX, panelY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawUtilityTitle(GuiGraphics guiGraphics, int panelX, int panelY) {
        Component utilityTitle = Component.literal("Utility Abilities");

        guiGraphics.drawString(
                this.font,
                utilityTitle,
                panelX + 370,
                panelY + 244,
                0xFFFFFFFF,
                true
        );
    }

    private void drawHoveredText(GuiGraphics guiGraphics, ExtraButton button, int panelX, int panelY) {
        int titleBoxX = panelX + 144;
        int titleBoxY = panelY + 355;
        int titleBoxWidth = 150;

        int descriptionBoxX = titleBoxX + 34;
        int descriptionBoxY = titleBoxY + 22;
        int descriptionBoxWidth = 80;

        Component title = Component.translatable(button.titleKey());
        Component description = Component.translatable(button.descriptionKey());

        int centeredTitleX = titleBoxX + ((titleBoxWidth - this.font.width(title)) / 2);

        guiGraphics.drawString(
                this.font,
                title,
                centeredTitleX,
                titleBoxY,
                button.titleColor(),
                true
        );

        var lines = this.font.split(description, descriptionBoxWidth);

        int lineY = descriptionBoxY;

        for (var line : lines) {
            int centeredLineX = descriptionBoxX + ((descriptionBoxWidth - this.font.width(line)) / 2);

            guiGraphics.drawString(
                    this.font,
                    line,
                    centeredLineX,
                    lineY,
                    0xFFAAAAAA,
                    false
            );

            lineY += this.font.lineHeight;
        }
    }

    private List<ExtraButton> getButtonsForCurrentPage(List<ExtraButton> allButtons) {
        int start = currentPage * BUTTONS_PER_PAGE;
        int end = Math.min(start + BUTTONS_PER_PAGE, allButtons.size());

        if (start >= allButtons.size()) {
            currentPage = 0;
            start = 0;
            end = Math.min(BUTTONS_PER_PAGE, allButtons.size());
        }

        return allButtons.subList(start, end);
    }

    private void drawPageButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int panelX, int panelY) {
        int x = getPageButtonX(panelX);
        int y = getPageButtonY(panelY);

        boolean hovered = mouseX >= x
                && mouseX < x + pageButtonWidth
                && mouseY >= y
                && mouseY < y + pageButtonHeight;

        guiGraphics.blit(
                hovered ? NEXT_PAGE_HOVERED : NEXT_PAGE,
                x,
                y,
                0,
                0,
                pageButtonWidth,
                pageButtonHeight,
                pageButtonWidth,
                pageButtonHeight
        );

        Component text = Component.literal("Next Page");
        drawScaledCenteredText(
                guiGraphics,
                text,
                x,
                y + 13,
                pageButtonWidth,
                0.75F,
                0xFFFFFFFF
        );

        int totalPages = Math.max(1, (int) Math.ceil(getAvailableButtons().size() / (double) BUTTONS_PER_PAGE));

        Component pageText = Component.literal((currentPage + 1) + "/" + totalPages);
        drawScaledCenteredText(
                guiGraphics,
                pageText,
                x,
                y + pageButtonHeight - 4,
                pageButtonWidth,
                0.75F,
                0xFFFFFFFF
        );
    }

    private void drawScaledCenteredText(
            GuiGraphics guiGraphics,
            Component text,
            int boxX,
            int y,
            int boxWidth,
            float scale,
            int color
    ) {
        int textWidth = this.font.width(text);
        int centeredX = boxX + ((boxWidth - (int) (textWidth * scale)) / 2);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);

        guiGraphics.drawString(
                this.font,
                text,
                (int) (centeredX / scale),
                (int) (y / scale),
                color,
                true
        );

        guiGraphics.pose().popPose();
    }

    private boolean clickedPageButton(double mouseX, double mouseY, int panelX, int panelY) {
        int x = getPageButtonX(panelX);
        int y = getPageButtonY(panelY);

        return mouseX >= x
                && mouseX < x + pageButtonWidth
                && mouseY >= y
                && mouseY < y + pageButtonHeight;
    }

    private int getPageButtonX(int panelX) {
        return panelX + 530;
    }

    private int getPageButtonY(int panelY) {
        return panelY + 515;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<ExtraButton> allButtons = getAvailableButtons();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int panelX = centerX - (PANEL_SIZE / 2);
        int panelY = centerY - (PANEL_SIZE / 2);

        if (allButtons.size() > BUTTONS_PER_PAGE && clickedPageButton(mouseX, mouseY, panelX, panelY)) {
            int totalPages = Math.max(1, (int) Math.ceil(allButtons.size() / (double) BUTTONS_PER_PAGE));
            currentPage = (currentPage + 1) % totalPages;

            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 1.0F, 1.0F);
            }

            return true;
        }

        List<ExtraButton> buttons = getButtonsForCurrentPage(allButtons);

        int radius = buttons.size() <= 1 ? 0 : 38 + ((buttons.size() - 2) * 7);

        for (int i = 0; i < buttons.size(); i++) {
            ExtraButton extraButton = buttons.get(i);

            double angle = -Math.PI / 2.0D + ((Math.PI * 2.0D) * i / buttons.size());

            int buttonX = centerX + (int) (Math.cos(angle) * radius) - (buttonSize / 2);
            int buttonY = centerY + (int) (Math.sin(angle) * radius) - (buttonSize / 2);

            if (clickedButton(mouseX, mouseY, buttonX, buttonY)) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 1.0F, 1.0F);
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

        if (EOPPalladiumProperties.hasClimbExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Climb", climbToggled, CLIMB_OFF, CLIMB_HOVERED, CLIMB_ON, "eop.extra.climb.title", "eop.extra.climb.description", 0xD99D45));
        if (EOPPalladiumProperties.hasNightVisionExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Night.Vision", nightVisionToggled, NIGHT_VISION_OFF, NIGHT_VISION_HOVERED, NIGHT_VISION_ON, "eop.extra.night.vision.title", "eop.extra.night.vision.description", 0x0071C2));
        if (EOPPalladiumProperties.hasSmeltingExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Smelting", smeltingToggled, SMELTING_OFF, SMELTING_HOVERED, SMELTING_ON, "eop.extra.smelt.title", "eop.extra.smelt.description", 0xFFFF00));
        if (EOPPalladiumProperties.hasFireResistanceExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Fire.Resistance", fireResistanceToggled, FIRE_RESISTANCE_OFF, FIRE_RESISTANCE_HOVERED, FIRE_RESISTANCE_ON, "eop.extra.fire.resistance.title", "eop.extra.fire.resistance.description", 0xFF8C00));
        if (EOPPalladiumProperties.hasEntitySenseExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Entity.Sense", entitySenseToggled, ENTITY_SENSE_OFF, ENTITY_SENSE_HOVERED, ENTITY_SENSE_ON, "eop.extra.entity.sense.title", "eop.extra.entity.sense.description", 0xCCFFCC));
        if (EOPPalladiumProperties.hasSuperJumpExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Super.Jump", superJumpToggled, SUPER_JUMP_OFF, SUPER_JUMP_HOVERED, SUPER_JUMP_ON, "eop.extra.super.jump.title", "eop.extra.super.jump.description", 0xBD60B7));
        if (EOPPalladiumProperties.hasEraseExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Erase", eraseToggled, ERASE_OFF, ERASE_HOVERED, ERASE_ON, "eop.extra.erase.title", "eop.extra.erase.description", 0xC7002B));
        if (EOPPalladiumProperties.hasExtraReachExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Extra.Reach", extraReachToggled, EXTRA_REACH_OFF, EXTRA_REACH_HOVERED, EXTRA_REACH_ON, "eop.extra.reach.title", "eop.extra.reach.description", 0x6397FF));
        if (EOPPalladiumProperties.hasSlowFallExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Slow.Fall", slowFallToggled, SLOW_FALL_OFF, SLOW_FALL_HOVERED, SLOW_FALL_ON, "eop.extra.slow.fall.title", "eop.extra.slow.fall.description", 0x9CD9E6));
        if (EOPPalladiumProperties.hasLightExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Light", lightToggled, LIGHT_OFF, LIGHT_HOVERED, LIGHT_ON, "eop.extra.light.title", "eop.extra.light.description", 0xFFFFFF));
        if (EOPPalladiumProperties.hasWaterBreathingExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Water.Breathing", waterBreathingToggled, WATER_BREATHING_OFF, WATER_BREATHING_HOVERED, WATER_BREATHING_ON, "eop.extra.water.breathing.title", "eop.extra.water.breathing.description", 0x4862D4));
        if (EOPPalladiumProperties.hasFrostWalkerExtra(player)) buttons.add(new ExtraButton("EOP.Extra.Frost.Walker", frostWalkerToggled, FROST_WALKER_OFF, FROST_WALKER_HOVERED, FROST_WALKER_ON, "eop.extra.frost.walker.title", "eop.extra.frost.walker.description", 0x59D4F0));

        return buttons;
    }

    private void drawToggleButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, boolean toggled, ResourceLocation off, ResourceLocation hoveredTexture, ResourceLocation on) {
        boolean hovered = isHoveringButton(mouseX, mouseY, x, y);
        ResourceLocation texture = toggled ? on : hovered ? hoveredTexture : off;
        guiGraphics.blit(texture, x, y, 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
    }

    private boolean isHoveringButton(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + buttonSize && mouseY >= y && mouseY < y + buttonSize;
    }

    private boolean clickedButton(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + buttonSize && mouseY >= y && mouseY < y + buttonSize;
    }

    private void toggleButton(String id) {
        switch (id) {
            case "EOP.Extra.Climb" -> { climbToggled = !climbToggled; updateTag("EOP.Climbing.On", climbToggled); }
            case "EOP.Extra.Night.Vision" -> { nightVisionToggled = !nightVisionToggled; updateTag("EOP.Night.Vision.On", nightVisionToggled); }
            case "EOP.Extra.Smelting" -> { smeltingToggled = !smeltingToggled; updateTag("EOP.Smelting.On", smeltingToggled); }
            case "EOP.Extra.Fire.Resistance" -> { fireResistanceToggled = !fireResistanceToggled; updateTag("EOP.Fire.Resistance.On", fireResistanceToggled); }
            case "EOP.Extra.Entity.Sense" -> { entitySenseToggled = !entitySenseToggled; updateTag("EOP.Entity.Sense.On", entitySenseToggled); }
            case "EOP.Extra.Super.Jump" -> { superJumpToggled = !superJumpToggled; updateTag("EOP.Super.Jump.On", superJumpToggled); }
            case "EOP.Extra.Erase" -> { eraseToggled = !eraseToggled; updateTag("EOP.Erase.On", eraseToggled); }
            case "EOP.Extra.Extra.Reach" -> { extraReachToggled = !extraReachToggled; updateTag("EOP.Extra.Reach.On", extraReachToggled); }
            case "EOP.Extra.Slow.Fall" -> { slowFallToggled = !slowFallToggled; updateTag("EOP.Slow.Fall.On", slowFallToggled); }
            case "EOP.Extra.Light" -> { lightToggled = !lightToggled; updateTag("EOP.Light.On", lightToggled); }
            case "EOP.Extra.Water.Breathing" -> { waterBreathingToggled = !waterBreathingToggled; updateTag("EOP.Water.Breathing.On", waterBreathingToggled); }
            case "EOP.Extra.Frost.Walker" -> { frostWalkerToggled = !frostWalkerToggled; updateTag("EOP.Frost.Walker.On", frostWalkerToggled); }
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
            ResourceLocation on,
            String titleKey,
            String descriptionKey,
            int titleColor
    ) {}
}
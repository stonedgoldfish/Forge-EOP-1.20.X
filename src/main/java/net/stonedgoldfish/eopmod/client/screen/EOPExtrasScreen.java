package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.EOPMod;
import net.minecraft.client.Minecraft;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;

public class EOPExtrasScreen extends Screen {

    private static final ResourceLocation BUTTON_OFF =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/eop_button_climb_disabled.png");

    private static final ResourceLocation BUTTON_HOVERED =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/eop_button_climb.png");

    private static final ResourceLocation BUTTON_ON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/extra_buttons/eop_button_climb_on.png");

    private boolean toggled = false;

    private final int buttonSize = 24;

    private boolean hasClimbAbility() {
        if (Minecraft.getInstance().player == null) {
            return false;
        }

        return EOPPalladiumProperties.hasClimbExtra(Minecraft.getInstance().player);
    }

    public EOPExtrasScreen() {
        super(Component.literal("EOP Extras"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(
                0,
                0,
                this.width,
                this.height,
                0x88000000,
                0x88000000
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int buttonX = (this.width / 2) - (buttonSize / 2);
        int buttonY = (this.height / 2) - (buttonSize / 2);

        if (hasClimbAbility()) {

            boolean hovered = mouseX >= buttonX
                    && mouseX < buttonX + buttonSize
                    && mouseY >= buttonY
                    && mouseY < buttonY + buttonSize;

            ResourceLocation texture = toggled
                    ? BUTTON_ON
                    : hovered ? BUTTON_HOVERED : BUTTON_OFF;

            guiGraphics.blit(
                    texture,
                    buttonX,
                    buttonY,
                    0,
                    0,
                    buttonSize,
                    buttonSize,
                    buttonSize,
                    buttonSize
            );

        } else {

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
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hasClimbAbility()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int buttonX = (this.width / 2) - (buttonSize / 2);
        int buttonY = (this.height / 2) - (buttonSize / 2);

        boolean hovered = mouseX >= buttonX
                && mouseX < buttonX + buttonSize
                && mouseY >= buttonY
                && mouseY < buttonY + buttonSize;

        if (hovered && button == 0) {
            this.toggled = !this.toggled;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
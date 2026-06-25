package net.stonedgoldfish.eopmod.client.screen.customization;

import net.minecraft.client.gui.GuiGraphics;

public interface PowerCustomizationPanel {

    void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int panelX,
            int panelY
    );

    default boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY, int containerId) {
        return false;
    }
}
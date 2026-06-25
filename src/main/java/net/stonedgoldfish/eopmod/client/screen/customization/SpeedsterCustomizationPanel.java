package net.stonedgoldfish.eopmod.client.screen.customization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SpeedsterCustomizationPanel implements PowerCustomizationPanel {

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int panelX, int panelY) {
        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                "Customize Speedster here",
                panelX + 115,
                panelY + 110,
                0xFFFFFF
        );
    }
}
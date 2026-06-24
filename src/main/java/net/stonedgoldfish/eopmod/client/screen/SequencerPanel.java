package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;

public interface SequencerPanel {

    void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int panelX,
            int panelY
    );
}
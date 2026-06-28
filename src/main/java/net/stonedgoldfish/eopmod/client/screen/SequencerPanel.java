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

    default boolean showsChipSlot() {
        return false;
    }

    default boolean showsFusionSlot() {
        return false;
    }

    default boolean showsChimeraSlot() {
        return false;
    }

    default boolean showsCustomization() {
        return false;
    }

    default boolean showsEvolutionSlot() {
        return false;
    }

    default boolean showsInventory() {
        return false;
    }
}
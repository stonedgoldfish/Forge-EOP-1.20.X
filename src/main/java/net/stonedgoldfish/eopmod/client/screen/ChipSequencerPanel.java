package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ChipSequencerPanel implements SequencerPanel {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "eop",
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_chip.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 219;
    private static final int CHIP_PANEL_U = 0;
    private static final int CHIP_PANEL_V = 0;

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int panelX,
            int panelY
    ) {
        guiGraphics.blit(
                TEXTURE,
                panelX,
                panelY,
                CHIP_PANEL_U,
                CHIP_PANEL_V,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }
}
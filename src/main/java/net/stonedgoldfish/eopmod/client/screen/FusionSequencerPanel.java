package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class FusionSequencerPanel implements SequencerPanel {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "eop",
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_chip.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 219;

    private static final int PANEL_U = 0;
    private static final int PANEL_V = 0;

    private static final int APPLY_BUTTON_X = 98;
    private static final int APPLY_BUTTON_Y = 198;

    public static final int APPLY_BUTTON_WIDTH = 35;
    public static final int APPLY_BUTTON_HEIGHT = 11;

    private static final int APPLY_NORMAL_U = 0;
    private static final int APPLY_NORMAL_V = 219;

    private static final int APPLY_HOVER_U = 35;
    private static final int APPLY_HOVER_V = 219;

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
                PANEL_U,
                PANEL_V,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        var font = Minecraft.getInstance().font;
        int centerX = panelX + PANEL_WIDTH / 2;

        drawCenteredScaledText(guiGraphics, "Fusion Catalyst Slot", centerX, panelY + 124, 0.75F, 0xFFFFFF);
        drawCenteredScaledText(guiGraphics, "Insert Fusion Catalyst", centerX, panelY + 158, 0.7F, 0xAAAAAA);
        drawCenteredScaledText(guiGraphics, "Requires two compatible", centerX, panelY + 178, 0.65F, 0xAAAAAA);
        drawCenteredScaledText(guiGraphics, "powers", centerX, panelY + 188, 0.65F, 0xAAAAAA);

        int buttonX = panelX + APPLY_BUTTON_X;
        int buttonY = panelY + APPLY_BUTTON_Y;

        boolean hovered =
                mouseX >= buttonX &&
                        mouseX < buttonX + APPLY_BUTTON_WIDTH &&
                        mouseY >= buttonY &&
                        mouseY < buttonY + APPLY_BUTTON_HEIGHT;

        int u = hovered ? APPLY_HOVER_U : APPLY_NORMAL_U;
        int v = hovered ? APPLY_HOVER_V : APPLY_NORMAL_V;

        guiGraphics.blit(
                TEXTURE,
                buttonX,
                buttonY,
                u,
                v,
                APPLY_BUTTON_WIDTH,
                APPLY_BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        guiGraphics.drawCenteredString(
                font,
                "Apply",
                buttonX + APPLY_BUTTON_WIDTH / 2,
                buttonY + 1,
                0xFFFFFF
        );
    }

    public static boolean isApplyButtonHovered(double mouseX, double mouseY, int panelX, int panelY) {
        int buttonX = panelX + APPLY_BUTTON_X;
        int buttonY = panelY + APPLY_BUTTON_Y;

        return mouseX >= buttonX
                && mouseX < buttonX + APPLY_BUTTON_WIDTH
                && mouseY >= buttonY
                && mouseY < buttonY + APPLY_BUTTON_HEIGHT;
    }

    private void drawCenteredScaledText(
            GuiGraphics guiGraphics,
            String text,
            int centerX,
            int y,
            float scale,
            int color
    ) {
        var font = Minecraft.getInstance().font;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);

        guiGraphics.drawCenteredString(
                font,
                text,
                (int) (centerX / scale),
                (int) (y / scale),
                color
        );

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean showsInventory() {
        return true;
    }

    @Override
    public boolean showsFusionSlot() {
        return true;
    }
}
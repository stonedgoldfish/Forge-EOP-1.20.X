package net.stonedgoldfish.eopmod.client.screen.customization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.menu.GeneticSequencerMenu;

public class MenderCustomizationPanel implements PowerCustomizationPanel {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_icons.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int BUTTON_WIDTH = 37;
    private static final int BUTTON_HEIGHT = 13;
    private static final int BUTTON_SPACING_X = 53;
    private static final int BUTTON_SPACING_Y = 18;

    private static final int BUTTONS_CENTER_X = 108;
    private static final int BUTTONS_START_Y = 43;

    private static final int BUTTON_NORMAL_U = 0;
    private static final int BUTTON_NORMAL_V = 216;

    private static final int BUTTON_HOVER_U = 37;
    private static final int BUTTON_HOVER_V = 216;

    private static final int BUTTON_SELECTED_U = 74;
    private static final int BUTTON_SELECTED_V = 216;

    private int selectedClawType = -1;

    private static final String[] CLAW_NAMES = {
            "Standard",
            "Paired",
            "Classic",
            "Offset",
            "Nails",
            "Rake",
            "Feral",
            "Split",
            "Talon",
            "Crown",
            "Heavy"
    };

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int panelX,
            int panelY
    ) {
        var font = Minecraft.getInstance().font;

        guiGraphics.drawCenteredString(
                font,
                "Bone Claw Type",
                panelX + 115,
                panelY + 32,
                0xFFFFFF
        );

        for (int i = 0; i < 11; i++) {
            int clawType = i + 1;

            int x = getButtonX(panelX, i);
            int y = getButtonY(panelY, i);

            boolean hovered = isMouseOver(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            int currentClawType = getCurrentClawType();

            boolean selected = currentClawType == i;

            int u = selected ? BUTTON_SELECTED_U : hovered ? BUTTON_HOVER_U : BUTTON_NORMAL_U;
            int v = selected ? BUTTON_SELECTED_V : hovered ? BUTTON_HOVER_V : BUTTON_NORMAL_V;

            float scale = 1.2F;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1.0F);

            guiGraphics.blit(
                    TEXTURE,
                    (int)(x / scale),
                    (int)(y / scale),
                    u,
                    v,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    TEXTURE_WIDTH,
                    TEXTURE_HEIGHT
            );

            guiGraphics.pose().popPose();

            float scaleLabel = 0.75F;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scaleLabel, scaleLabel, 1.0F);

            guiGraphics.drawCenteredString(
                    font,
                    CLAW_NAMES[i],
                    (int) ((x + BUTTON_WIDTH / 2 + 4) / scaleLabel),
                    (int) ((y + 3) / scaleLabel),
                    0xFFFFFF
            );

            guiGraphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY, int containerId) {
        for (int i = 0; i < 11; i++) {
            int clawType = i + 1;

            int x = getButtonX(panelX, i);
            int y = getButtonY(panelY, i);

            if (isMouseOver(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                this.selectedClawType = i;

                Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                        containerId,
                        GeneticSequencerMenu.MENDER_CLAW_TYPE_BASE_ID + i
                );

                return true;
            }
        }

        return false;
    }

    private static int getButtonX(int panelX, int index) {
        int buttonsPerRow = 3;
        int rowIndex = index % buttonsPerRow;

        int totalWidth = buttonsPerRow * BUTTON_WIDTH
                + (buttonsPerRow - 1) * (BUTTON_SPACING_X - BUTTON_WIDTH);

        return panelX + BUTTONS_CENTER_X - totalWidth / 2
                + rowIndex * BUTTON_SPACING_X;
    }

    private static int getButtonY(int panelY, int index) {
        int buttonsPerRow = 3;
        int row = index / buttonsPerRow;

        return panelY + BUTTONS_START_Y + row * BUTTON_SPACING_Y;
    }

    private static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    private static int getCurrentClawType() {
        var player = Minecraft.getInstance().player;

        if (player == null || player.getScoreboard() == null) {
            return 0;
        }

        var scoreboard = player.getScoreboard();
        var objective = scoreboard.getObjective("EOP.Claw.Type");

        if (objective == null) {
            return 0;
        }

        var score = scoreboard.getOrCreatePlayerScore(
                player.getScoreboardName(),
                objective
        );

        return score.getScore();
    }
}
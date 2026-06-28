package net.stonedgoldfish.eopmod.client.screen.customization;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.menu.GeneticSequencerMenu;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;

public class SpeedsterCustomizationPanel implements PowerCustomizationPanel {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_icons.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int PANEL_CENTER_X = 115;

    private static final int TARGET_BUTTON_Y = 31;
    private static final int TARGET_BUTTON_WIDTH = 61;
    private static final int TARGET_BUTTON_HEIGHT = 13;
    private static final int TARGET_BUTTON_GAP = 11;

    private static final int TARGET_NORMAL_U = 111;
    private static final int TARGET_NORMAL_V = 216;
    private static final int TARGET_HOVER_U = 76;
    private static final int TARGET_HOVER_V = 229;
    private static final int TARGET_SELECTED_U = 137;
    private static final int TARGET_SELECTED_V = 229;

    private static final int SLIDER_X = 30;
    private static final int SLIDER_Y = 62;
    private static final int SLIDER_WIDTH = 110;
    private static final int SLIDER_HEIGHT = 8;
    private static final int SLIDER_SPACING_Y = 18;

    private static final int SLIDER_OVERLAY_U = 77;
    private static final int SLIDER_OVERLAY_V = 243;
    private static final int SLIDER_OVERLAY_WIDTH = 113;
    private static final int SLIDER_OVERLAY_HEIGHT = 8;

    private static final int KNOB_WIDTH = 10;
    private static final int KNOB_HEIGHT = 12;
    private static final int KNOB_U = 63;
    private static final int KNOB_V = 235;

    private static final int PREVIEW_X = 158;
    private static final int PREVIEW_Y = 65;
    private static final int PREVIEW_SIZE = 22;

    private static final int PREVIEW_OVERLAY_U = 198;
    private static final int PREVIEW_OVERLAY_V = 231;
    private static final int PREVIEW_OVERLAY_WIDTH = 24;
    private static final int PREVIEW_OVERLAY_HEIGHT = 24;

    private static final int BASE_COLOR = 0x0056E3;

    private static final int ACTION_BUTTON_Y = 47;
    private static final int ACTION_BUTTON_WIDTH = 37;
    private static final int ACTION_BUTTON_HEIGHT = 13;
    private static final int ACTION_BUTTON_GAP = 4;

    private static final int ACTION_NORMAL_U = 0;
    private static final int ACTION_NORMAL_V = 216;
    private static final int ACTION_HOVER_U = 37;
    private static final int ACTION_HOVER_V = 216;

    private static final int HEX_BOX_X = 145;
    private static final int HEX_BOX_Y = 95;
    private static final int HEX_BOX_WIDTH = 50;
    private static final int HEX_BOX_HEIGHT = 12;

    private int primaryDraftColor = -1;
    private int secondaryDraftColor = -1;

    private int selectedTarget = 0;
    private int draggingChannel = -1;

    private boolean typingHex = false;
    private String hexInput = "";

    private long lastHexBoxClickTime = 0L;
    private boolean hexInputSelected = false;

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int panelX, int panelY) {
        var font = Minecraft.getInstance().font;
        var player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (this.primaryDraftColor == -1) {
            this.primaryDraftColor = EOPPalladiumProperties.getSpeedsterPrimaryLightningColor(player);
        }

        if (this.secondaryDraftColor == -1) {
            this.secondaryDraftColor = EOPPalladiumProperties.getSpeedsterSecondaryLightningColor(player);
        }

        int color = selectedTarget == 0
                ? this.primaryDraftColor
                : this.secondaryDraftColor;

        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        drawTargetButton(guiGraphics, mouseX, mouseY, panelX, panelY, 0, "Primary");
        drawTargetButton(guiGraphics, mouseX, mouseY, panelX, panelY, 1, "Secondary");
        drawActionButton(guiGraphics, mouseX, mouseY, panelX, panelY, 0, "Apply");
        drawActionButton(guiGraphics, mouseX, mouseY, panelX, panelY, 1, "Reset");
        drawActionButton(guiGraphics, mouseX, mouseY, panelX, panelY, 2, "Copy");

        drawSlider(guiGraphics, "", r, panelX + SLIDER_X, panelY + SLIDER_Y, 0xFFFF5555);
        drawSlider(guiGraphics, "", g, panelX + SLIDER_X, panelY + SLIDER_Y + SLIDER_SPACING_Y, 0xFF55FF55);
        drawSlider(guiGraphics, "", b, panelX + SLIDER_X, panelY + SLIDER_Y + SLIDER_SPACING_Y * 2, 0xFF5555FF);

        int previewColor = 0xFF000000 | color;

        guiGraphics.fill(panelX + PREVIEW_X, panelY + PREVIEW_Y,
                panelX + PREVIEW_X + PREVIEW_SIZE, panelY + PREVIEW_Y + PREVIEW_SIZE,
                previewColor);

        guiGraphics.blit(
                TEXTURE,
                panelX + PREVIEW_X - 1,
                panelY + PREVIEW_Y,
                PREVIEW_OVERLAY_U,
                PREVIEW_OVERLAY_V,
                PREVIEW_OVERLAY_WIDTH,
                PREVIEW_OVERLAY_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        drawHexBox(guiGraphics, panelX, panelY);
    }

    private void drawHexBox(GuiGraphics guiGraphics, int panelX, int panelY) {
        var font = Minecraft.getInstance().font;

        int x = panelX + HEX_BOX_X;
        int y = panelY + HEX_BOX_Y;

        guiGraphics.fill(x, y, x + HEX_BOX_WIDTH, y + HEX_BOX_HEIGHT, 0xFF111111);
        guiGraphics.fill(x - 1, y - 1, x + HEX_BOX_WIDTH + 1, y, typingHex ? 0xFFFFFFFF : 0xFF777777);
        guiGraphics.fill(x - 1, y + HEX_BOX_HEIGHT, x + HEX_BOX_WIDTH + 1, y + HEX_BOX_HEIGHT + 1, typingHex ? 0xFFFFFFFF : 0xFF777777);
        guiGraphics.fill(x - 1, y, x, y + HEX_BOX_HEIGHT, typingHex ? 0xFFFFFFFF : 0xFF777777);
        guiGraphics.fill(x + HEX_BOX_WIDTH, y, x + HEX_BOX_WIDTH + 1, y + HEX_BOX_HEIGHT, typingHex ? 0xFFFFFFFF : 0xFF777777);

        String display = typingHex ? hexInput : String.format("%06X", getSelectedDraftColor());

        if (typingHex && hexInputSelected) {
            guiGraphics.fill(
                    x,
                    y,
                    x + HEX_BOX_WIDTH,
                    y + HEX_BOX_HEIGHT,
                    0xFF3355AA
            );
        }

        guiGraphics.drawString(
                font,
                display,
                x + 4,
                y + 2,
                0xFFFFFF,
                false
        );
    }

    private void drawActionButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            int index,
            String label
    ) {
        var font = Minecraft.getInstance().font;

        int totalWidth = ACTION_BUTTON_WIDTH * 3 + ACTION_BUTTON_GAP * 2;
        int x = panelX + PANEL_CENTER_X - totalWidth / 2
                + index * (ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP);
        int y = panelY + ACTION_BUTTON_Y;

        boolean hovered = isMouseOver(mouseX, mouseY, x, y, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);

        int u = hovered ? ACTION_HOVER_U : ACTION_NORMAL_U;
        int v = hovered ? ACTION_HOVER_V : ACTION_NORMAL_V;

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                u,
                v,
                ACTION_BUTTON_WIDTH,
                ACTION_BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        guiGraphics.drawCenteredString(
                font,
                label,
                x + ACTION_BUTTON_WIDTH / 2,
                y + 2,
                0xFFFFFF
        );
    }

    private void drawTargetButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int panelX, int panelY, int target, String label) {
        var font = Minecraft.getInstance().font;

        int x = panelX + PANEL_CENTER_X - TARGET_BUTTON_WIDTH - TARGET_BUTTON_GAP;

        if (target == 1) {
            x = panelX + PANEL_CENTER_X + TARGET_BUTTON_GAP;
        }

        int y = panelY + TARGET_BUTTON_Y;

        boolean hovered = isMouseOver(mouseX, mouseY, x, y, TARGET_BUTTON_WIDTH, TARGET_BUTTON_HEIGHT);
        boolean selected = selectedTarget == target;

        int u = selected ? TARGET_SELECTED_U : hovered ? TARGET_HOVER_U : TARGET_NORMAL_U;
        int v = selected ? TARGET_SELECTED_V : hovered ? TARGET_HOVER_V : TARGET_NORMAL_V;

        guiGraphics.blit(TEXTURE, x, y, u, v,
                TARGET_BUTTON_WIDTH, TARGET_BUTTON_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        guiGraphics.drawCenteredString(font, label,
                x + TARGET_BUTTON_WIDTH / 2,
                y + 2,
                0xFFFFFF);
    }

    private void drawSlider(GuiGraphics guiGraphics, String label, int value, int x, int y, int fillColor) {
        var font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, label, x - 16, y, 0xFFFFFF, false);

        for (int i = 0; i < SLIDER_WIDTH; i++) {
            float progress = (float) i / (SLIDER_WIDTH - 1);

            int red = (fillColor >> 16) & 255;
            int green = (fillColor >> 8) & 255;
            int blue = fillColor & 255;

            int r = (int) (red * progress);
            int g = (int) (green * progress);
            int b = (int) (blue * progress);

            int gradientColor = 0xFF000000 | (r << 16) | (g << 8) | b;

            guiGraphics.fill(
                    x + i,
                    y + 3,
                    x + i + 1,
                    y + 5,
                    gradientColor
            );
        }

        guiGraphics.blit(TEXTURE, x, y,
                SLIDER_OVERLAY_U, SLIDER_OVERLAY_V,
                SLIDER_OVERLAY_WIDTH, SLIDER_OVERLAY_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int knobX = x + (int) ((value / 255.0F) * (SLIDER_WIDTH - 1));

        guiGraphics.blit(
                TEXTURE,
                knobX - KNOB_WIDTH / 2,
                y - 2,
                KNOB_U,
                KNOB_V,
                KNOB_WIDTH,
                KNOB_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY, int containerId) {
        if (handleTargetClick(mouseX, mouseY, panelX, panelY)) {
            return true;
        }

        if (handleActionButtonClick(mouseX, mouseY, panelX, panelY, containerId)) {
            return true;
        }

        if (handleHexBoxClick(mouseX, mouseY, panelX, panelY)) {
            return true;
        }

        return handleSliderClick(mouseX, mouseY, panelX, panelY, containerId);
    }

    private boolean handleHexBoxClick(double mouseX, double mouseY, int panelX, int panelY) {
        int x = panelX + HEX_BOX_X;
        int y = panelY + HEX_BOX_Y;

        if (isMouseOver(mouseX, mouseY, x, y, HEX_BOX_WIDTH, HEX_BOX_HEIGHT)) {
            long now = System.currentTimeMillis();

            this.typingHex = true;

            if (now - this.lastHexBoxClickTime <= 300L) {
                this.hexInput = String.format("%06X", getSelectedDraftColor());
                this.hexInputSelected = true;
            } else {
                this.hexInput = String.format("%06X", getSelectedDraftColor());
                this.hexInputSelected = false;
            }

            this.lastHexBoxClickTime = now;
            return true;
        }

        return false;
    }

    private boolean handleActionButtonClick(
            double mouseX,
            double mouseY,
            int panelX,
            int panelY,
            int containerId
    ) {
        int totalWidth = ACTION_BUTTON_WIDTH * 3 + ACTION_BUTTON_GAP * 2;
        int y = panelY + ACTION_BUTTON_Y;

        for (int index = 0; index < 3; index++) {
            int x = panelX + PANEL_CENTER_X - totalWidth / 2
                    + index * (ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP);

            if (!isMouseOver(mouseX, mouseY, x, y, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)) {
                continue;
            }

            if (index == 0) {
                if (typingHex) {
                    applyHexInput();
                    typingHex = false;
                    hexInputSelected = false;
                }

                applyDraftColor(containerId);
            } else if (index == 1) {
                typingHex = false;
                hexInputSelected = false;

                setSelectedDraftColor(BASE_COLOR);
                applyDraftColor(containerId);
            } else if (index == 2) {
                typingHex = false;
                hexInputSelected = false;

                int otherColor = selectedTarget == 0
                        ? this.secondaryDraftColor
                        : this.primaryDraftColor;

                setSelectedDraftColor(otherColor);
                applyDraftColor(containerId);
            }

            return true;
        }

        return false;
    }

    private void setSelectedDraftColor(int color) {
        if (selectedTarget == 0) {
            this.primaryDraftColor = color;
        } else {
            this.secondaryDraftColor = color;
        }
    }

    private int getSelectedDraftColor() {
        return selectedTarget == 0
                ? this.primaryDraftColor
                : this.secondaryDraftColor;
    }

    private void applyDraftColor(int containerId) {
        int color = getSelectedDraftColor();

        int id = GeneticSequencerMenu.SPEEDSTER_APPLY_COLOR_BASE_ID
                + selectedTarget * 0x1000000
                + color;

        Minecraft.getInstance().gameMode.handleInventoryButtonClick(containerId, id);
    }

    private boolean handleTargetClick(double mouseX, double mouseY, int panelX, int panelY) {
        for (int target = 0; target < 2; target++) {
            int x = panelX + PANEL_CENTER_X - TARGET_BUTTON_WIDTH - 4;

            if (target == 1) {
                x = panelX + PANEL_CENTER_X + 4;
            }

            int y = panelY + TARGET_BUTTON_Y;

            if (isMouseOver(mouseX, mouseY, x, y, TARGET_BUTTON_WIDTH, TARGET_BUTTON_HEIGHT)) {
                this.selectedTarget = target;
                return true;
            }
        }

        return false;
    }

    private boolean handleSliderClick(double mouseX, double mouseY, int panelX, int panelY, int containerId) {
        for (int channel = 0; channel < 3; channel++) {
            int x = panelX + SLIDER_X;
            int y = panelY + SLIDER_Y + channel * SLIDER_SPACING_Y;

            if (isMouseOver(mouseX, mouseY, x, y - 2, SLIDER_WIDTH, SLIDER_HEIGHT + 4)) {
                this.draggingChannel = channel;
                updateSliderValue(mouseX, panelX, channel, containerId);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!typingHex) {
            return false;
        }

        char c = Character.toUpperCase(codePoint);

        boolean valid =
                (c >= '0' && c <= '9')
                        || (c >= 'A' && c <= 'F');

        if (!valid) {
            return true;
        }

        if (hexInputSelected) {
            hexInput = "";
            hexInputSelected = false;
        }

        if (hexInput.length() < 6) {
            hexInput += c;
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!typingHex) {
            return false;
        }

        // Ctrl + V paste
        if (Screen.hasControlDown() && keyCode == InputConstants.KEY_V) {
            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();

            clipboard = clipboard
                    .replace("#", "")
                    .replace("0x", "")
                    .replace("0X", "")
                    .trim()
                    .toUpperCase();

            StringBuilder cleaned = new StringBuilder();

            for (int i = 0; i < clipboard.length(); i++) {
                char c = clipboard.charAt(i);

                boolean valid =
                        (c >= '0' && c <= '9')
                                || (c >= 'A' && c <= 'F');

                if (valid) {
                    cleaned.append(c);
                }

                if (cleaned.length() >= 6) {
                    break;
                }
            }

            if (!cleaned.isEmpty()) {
                this.hexInput = cleaned.toString();
                this.hexInputSelected = false;

                if (this.hexInput.length() == 6) {
                    applyHexInput();
                }
            }

            return true;
        }

        if (keyCode == 259) {
            if (!hexInput.isEmpty()) {
                hexInput = hexInput.substring(0, hexInput.length() - 1);
            }

            return true;
        }

        if (keyCode == 257 || keyCode == 335) {
            applyHexInput();
            typingHex = false;
            return true;
        }

        if (keyCode == 256) {
            typingHex = false;
            return true;
        }

        return true;
    }

    private void applyHexInput() {
        if (hexInput.length() != 6) {
            return;
        }

        try {
            int color = Integer.parseInt(hexInput, 16);
            setSelectedDraftColor(color);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int panelX, int panelY, int containerId) {
        if (this.draggingChannel == -1) {
            return false;
        }

        updateSliderValue(mouseX, panelX, this.draggingChannel, containerId);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int panelX, int panelY) {
        if (this.draggingChannel != -1) {
            this.draggingChannel = -1;
            return true;
        }

        return false;
    }

    private void updateSliderValue(double mouseX, int panelX, int channel, int containerId) {
        int x = panelX + SLIDER_X;

        int value = (int) (((mouseX - x) / SLIDER_WIDTH) * 255.0D);
        value = Math.max(0, Math.min(255, value));

        int color = getSelectedDraftColor();

        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        if (channel == 0) {
            r = value;
        } else if (channel == 1) {
            g = value;
        } else if (channel == 2) {
            b = value;
        }

        setSelectedDraftColor((r << 16) | (g << 8) | b);
    }

    private static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}
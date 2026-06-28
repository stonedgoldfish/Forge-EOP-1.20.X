package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.client.screen.customization.MenderCustomizationPanel;
import net.stonedgoldfish.eopmod.client.screen.customization.PowerCustomizationPanel;
import net.stonedgoldfish.eopmod.client.screen.customization.SpeedsterCustomizationPanel;
import net.threetag.palladium.power.SuperpowerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizationSequencerPanel implements SequencerPanel {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_icons.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 50;

    private static final int POWER_BUTTON_SIZE = 21;
    private static final int POWER_BUTTON_Y = 152;
    private static final int POWER_BUTTON_SPACING = 25;
    private static final int POWER_BUTTON_CENTER_X = 113;

    private static final int POWER_BUTTONS_PER_ROW = 2;
    private static final int POWER_BUTTON_ROWS = 2;
    private static final int POWER_BUTTONS_PER_PAGE = POWER_BUTTONS_PER_ROW * POWER_BUTTON_ROWS;
    private static final int POWER_BUTTON_ROW_SPACING = 25;

    private static final int POWER_BUTTON_U = 0;
    private static final int POWER_BUTTON_V = 230;
    private static final int POWER_BUTTON_HOVER_U = 21;
    private static final int POWER_BUTTON_HOVER_V = 230;
    private static final int POWER_BUTTON_SELECTED_U = 42;
    private static final int POWER_BUTTON_SELECTED_V = 230;

    private static final int NEXT_PAGE_BUTTON_X = 138;
    private static final int NEXT_PAGE_BUTTON_Y = 187;
    private static final int NEXT_PAGE_BUTTON_WIDTH = 6;
    private static final int NEXT_PAGE_BUTTON_HEIGHT = 9;

    private static final int NEXT_PAGE_NORMAL_U = 175;
    private static final int NEXT_PAGE_NORMAL_V = 218;
    private static final int NEXT_PAGE_HOVER_U = 181;
    private static final int NEXT_PAGE_HOVER_V = 218;

    private static final int PREV_PAGE_BUTTON_X = 81;
    private static final int PREV_PAGE_BUTTON_Y = 187;
    private static final int PREV_PAGE_BUTTON_WIDTH = 6;
    private static final int PREV_PAGE_BUTTON_HEIGHT = 9;

    private static final int PREV_PAGE_NORMAL_U = 193;
    private static final int PREV_PAGE_NORMAL_V = 218;
    private static final int PREV_PAGE_HOVER_U = 187;
    private static final int PREV_PAGE_HOVER_V = 218;

    private final Map<String, PowerCustomizationPanel> customizationPanels = new HashMap<>();

    private String selectedPower = null;
    private int powerPage = 0;

    public CustomizationSequencerPanel() {
        this.customizationPanels.put("speedster", new SpeedsterCustomizationPanel());
        this.customizationPanels.put("mender", new MenderCustomizationPanel());
    }

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
                0,
                0,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        List<String> ownedCustomizablePowers = getOwnedCustomizablePowers();

        if (ownedCustomizablePowers.isEmpty()) {
            guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "No customization available",
                    panelX + PANEL_WIDTH / 2,
                    panelY + 60,
                    0xFFFFFF
            );
            return;
        }

        int maxPage = Math.max(0, (ownedCustomizablePowers.size() - 1) / POWER_BUTTONS_PER_PAGE);

        if (this.powerPage > maxPage) {
            this.powerPage = maxPage;
        }

        if (this.selectedPower == null || !ownedCustomizablePowers.contains(this.selectedPower)) {
            int startIndex = this.powerPage * POWER_BUTTONS_PER_PAGE;
            this.selectedPower = ownedCustomizablePowers.get(Math.min(startIndex, ownedCustomizablePowers.size() - 1));
        }

        renderPowerButtons(guiGraphics, mouseX, mouseY, panelX, panelY, ownedCustomizablePowers);

        PowerCustomizationPanel panel = this.customizationPanels.get(this.selectedPower);

        if (panel != null) {
            panel.render(guiGraphics, mouseX, mouseY, partialTick, panelX, panelY);
        }
    }

    private void renderPowerButtons(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            List<String> powers
    ) {
        int maxPage = Math.max(0, (powers.size() - 1) / POWER_BUTTONS_PER_PAGE);

        if (this.powerPage > maxPage) {
            this.powerPage = maxPage;
        }

        int startIndex = this.powerPage * POWER_BUTTONS_PER_PAGE;
        int endIndex = Math.min(startIndex + POWER_BUTTONS_PER_PAGE, powers.size());
        int visibleCount = endIndex - startIndex;

        for (int localIndex = 0; localIndex < visibleCount; localIndex++) {
            int actualIndex = startIndex + localIndex;
            String powerKey = powers.get(actualIndex);

            int row = localIndex / POWER_BUTTONS_PER_ROW;
            int col = localIndex % POWER_BUTTONS_PER_ROW;

            int rowCount = Math.min(
                    POWER_BUTTONS_PER_ROW,
                    visibleCount - row * POWER_BUTTONS_PER_ROW
            );

            int rowWidth = rowCount * POWER_BUTTON_SIZE
                    + (rowCount - 1) * (POWER_BUTTON_SPACING - POWER_BUTTON_SIZE);

            int rowStartX = panelX + POWER_BUTTON_CENTER_X - rowWidth / 2;
            int x = rowStartX + col * POWER_BUTTON_SPACING;
            int y = getPowerButtonY(panelY, powers.size(), row);

            boolean hovered = isMouseOver(mouseX, mouseY, x, y, POWER_BUTTON_SIZE, POWER_BUTTON_SIZE);
            boolean selected = powerKey.equals(this.selectedPower);

            int u = selected ? POWER_BUTTON_SELECTED_U : hovered ? POWER_BUTTON_HOVER_U : POWER_BUTTON_U;
            int v = selected ? POWER_BUTTON_SELECTED_V : hovered ? POWER_BUTTON_HOVER_V : POWER_BUTTON_V;

            guiGraphics.blit(
                    TEXTURE,
                    x,
                    y,
                    u,
                    v,
                    POWER_BUTTON_SIZE,
                    POWER_BUTTON_SIZE,
                    TEXTURE_WIDTH,
                    TEXTURE_HEIGHT
            );

            ResourceLocation icon =
                    ResourceLocation.fromNamespaceAndPath(
                            EOPMod.MOD_ID,
                            "textures/gui/" + powerKey + ".png"
                    );

            guiGraphics.blit(
                    icon,
                    x + 2,
                    y + 2,
                    0,
                    0,
                    17,
                    17,
                    17,
                    17
            );
        }

        if (powers.size() > POWER_BUTTONS_PER_PAGE) {
            renderPrevPageButton(guiGraphics, mouseX, mouseY, panelX, panelY);
            renderNextPageButton(guiGraphics, mouseX, mouseY, panelX, panelY);
        }
    }

    private void renderNextPageButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY
    ) {
        int x = panelX + NEXT_PAGE_BUTTON_X;
        int y = panelY + NEXT_PAGE_BUTTON_Y;

        boolean hovered = isMouseOver(
                mouseX,
                mouseY,
                x,
                y,
                NEXT_PAGE_BUTTON_WIDTH,
                NEXT_PAGE_BUTTON_HEIGHT
        );

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                hovered ? NEXT_PAGE_HOVER_U : NEXT_PAGE_NORMAL_U,
                hovered ? NEXT_PAGE_HOVER_V : NEXT_PAGE_NORMAL_V,
                NEXT_PAGE_BUTTON_WIDTH,
                NEXT_PAGE_BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private void renderPrevPageButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY
    ) {
        int x = panelX + PREV_PAGE_BUTTON_X;
        int y = panelY + PREV_PAGE_BUTTON_Y;

        boolean hovered = isMouseOver(
                mouseX,
                mouseY,
                x,
                y,
                PREV_PAGE_BUTTON_WIDTH,
                PREV_PAGE_BUTTON_HEIGHT
        );

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                hovered ? PREV_PAGE_HOVER_U : PREV_PAGE_NORMAL_U,
                hovered ? PREV_PAGE_HOVER_V : PREV_PAGE_NORMAL_V,
                PREV_PAGE_BUTTON_WIDTH,
                PREV_PAGE_BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    public boolean charTyped(char codePoint, int modifiers) {
        PowerCustomizationPanel panel = this.customizationPanels.get(this.selectedPower);

        if (panel != null) {
            return panel.charTyped(codePoint, modifiers);
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        PowerCustomizationPanel panel = this.customizationPanels.get(this.selectedPower);

        if (panel != null) {
            return panel.keyPressed(keyCode, scanCode, modifiers);
        }

        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY) {
        if (handlePowerButtonClick(mouseX, mouseY, panelX, panelY)) {
            return true;
        }

        PowerCustomizationPanel panel = this.customizationPanels.get(this.selectedPower);

        if (panel != null && Minecraft.getInstance().player != null) {
            return panel.mouseClicked(
                    mouseX,
                    mouseY,
                    panelX,
                    panelY,
                    Minecraft.getInstance().player.containerMenu.containerId
            );
        }

        return false;
    }

    private boolean handlePowerButtonClick(double mouseX, double mouseY, int panelX, int panelY) {
        List<String> powers = getOwnedCustomizablePowers();

        if (powers.isEmpty()) {
            return false;
        }

        int maxPage = Math.max(0, (powers.size() - 1) / POWER_BUTTONS_PER_PAGE);

        if (powers.size() > POWER_BUTTONS_PER_PAGE) {
            int prevX = panelX + PREV_PAGE_BUTTON_X;
            int prevY = panelY + PREV_PAGE_BUTTON_Y;

            if (isMouseOver(mouseX, mouseY, prevX, prevY, PREV_PAGE_BUTTON_WIDTH, PREV_PAGE_BUTTON_HEIGHT)) {
                this.powerPage--;

                if (this.powerPage < 0) {
                    this.powerPage = maxPage;
                }

                int startIndex = this.powerPage * POWER_BUTTONS_PER_PAGE;

                if (startIndex < powers.size()) {
                    this.selectedPower = powers.get(startIndex);
                }

                return true;
            }

            int nextX = panelX + NEXT_PAGE_BUTTON_X;
            int nextY = panelY + NEXT_PAGE_BUTTON_Y;

            if (isMouseOver(mouseX, mouseY, nextX, nextY, NEXT_PAGE_BUTTON_WIDTH, NEXT_PAGE_BUTTON_HEIGHT)) {
                this.powerPage++;

                if (this.powerPage > maxPage) {
                    this.powerPage = 0;
                }

                int startIndex = this.powerPage * POWER_BUTTONS_PER_PAGE;

                if (startIndex < powers.size()) {
                    this.selectedPower = powers.get(startIndex);
                }

                return true;
            }
        }

        int startIndex = this.powerPage * POWER_BUTTONS_PER_PAGE;
        int endIndex = Math.min(startIndex + POWER_BUTTONS_PER_PAGE, powers.size());
        int visibleCount = endIndex - startIndex;

        for (int localIndex = 0; localIndex < visibleCount; localIndex++) {
            int actualIndex = startIndex + localIndex;

            int row = localIndex / POWER_BUTTONS_PER_ROW;
            int col = localIndex % POWER_BUTTONS_PER_ROW;

            int rowCount = Math.min(
                    POWER_BUTTONS_PER_ROW,
                    visibleCount - row * POWER_BUTTONS_PER_ROW
            );

            int rowWidth = rowCount * POWER_BUTTON_SIZE
                    + (rowCount - 1) * (POWER_BUTTON_SPACING - POWER_BUTTON_SIZE);

            int rowStartX = panelX + POWER_BUTTON_CENTER_X - rowWidth / 2;

            int x = rowStartX + col * POWER_BUTTON_SPACING;
            int y = getPowerButtonY(panelY, powers.size(), row);

            if (isMouseOver(mouseX, mouseY, x, y, POWER_BUTTON_SIZE, POWER_BUTTON_SIZE)) {
                this.selectedPower = powers.get(actualIndex);
                return true;
            }
        }

        return false;
    }

    private List<String> getOwnedCustomizablePowers() {
        List<String> owned = new ArrayList<>();

        var player = Minecraft.getInstance().player;

        if (player == null) {
            return owned;
        }

        for (String powerKey : this.customizationPanels.keySet()) {
            if (SuperpowerUtil.hasSuperpower(
                    player,
                    ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, powerKey)
            )) {
                owned.add(powerKey);
            }
        }

        return owned;
    }

    private static int getPowerButtonY(int panelY, int totalPowers, int row) {
        int yOffset = totalPowers > 2 ? -10 : 0;
        return panelY + POWER_BUTTON_Y + yOffset + row * POWER_BUTTON_ROW_SPACING;
    }

    private static boolean isMouseOver(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int panelX, int panelY) {
        PowerCustomizationPanel panel = this.customizationPanels.get(this.selectedPower);

        if (panel != null && Minecraft.getInstance().player != null) {
            return panel.mouseDragged(
                    mouseX,
                    mouseY,
                    panelX,
                    panelY,
                    Minecraft.getInstance().player.containerMenu.containerId
            );
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int panelX, int panelY) {
        PowerCustomizationPanel panel = this.customizationPanels.get(this.selectedPower);

        if (panel != null) {
            return panel.mouseReleased(mouseX, mouseY, panelX, panelY);
        }

        return false;
    }

    @Override
    public boolean showsCustomization() {
        return true;
    }
}
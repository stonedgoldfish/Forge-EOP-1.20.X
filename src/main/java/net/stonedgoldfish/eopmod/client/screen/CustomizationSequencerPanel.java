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
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_customize.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 219;

    private static final int POWER_BUTTON_Y = 142;
    private static final int POWER_BUTTON_SPACING = 25;

    private static final int POWER_BUTTON_U = 0;
    private static final int POWER_BUTTON_V = 230;

    private static final int POWER_BUTTON_HOVER_U = 21;
    private static final int POWER_BUTTON_HOVER_V = 230;

    private static final int POWER_BUTTON_SELECTED_U = 42;
    private static final int POWER_BUTTON_SELECTED_V = 230;

    private static final int POWER_BUTTON_SIZE = 21;

    private final Map<String, PowerCustomizationPanel> customizationPanels = new HashMap<>();

    private String selectedPower = null;

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
                    0xAAAAAA
            );
            return;
        }

        if (this.selectedPower == null || !ownedCustomizablePowers.contains(this.selectedPower)) {
            this.selectedPower = ownedCustomizablePowers.get(0);
        }

        float titleScale = 0.75F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(titleScale, titleScale, 1.0F);

        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                "Customizable Powers",
                (int) ((panelX + PANEL_WIDTH / 2) / titleScale),
                (int) ((panelY + 125) / titleScale), // adjust this value
                0xFFFFFF
        );

        guiGraphics.pose().popPose();

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
        int totalWidth = powers.size() * POWER_BUTTON_SIZE
                + (powers.size() - 1) * (POWER_BUTTON_SPACING - POWER_BUTTON_SIZE);

        int startX = panelX + (PANEL_WIDTH / 2) - (totalWidth / 2);
        int y = panelY + POWER_BUTTON_Y;

        for (int i = 0; i < powers.size(); i++) {
            String powerKey = powers.get(i);

            int x = startX + i * POWER_BUTTON_SPACING;

            boolean hovered = isMouseOver(mouseX, mouseY, x, y, POWER_BUTTON_SIZE, POWER_BUTTON_SIZE);
            boolean selected = powerKey.equals(this.selectedPower);

            int u;
            int v;

            if (selected) {
                u = POWER_BUTTON_SELECTED_U;
                v = POWER_BUTTON_SELECTED_V;
            } else if (hovered) {
                u = POWER_BUTTON_HOVER_U;
                v = POWER_BUTTON_HOVER_V;
            } else {
                u = POWER_BUTTON_U;
                v = POWER_BUTTON_V;
            }

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
    }

    public boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY) {
        List<String> powers = getOwnedCustomizablePowers();

        if (powers.isEmpty()) {
            return false;
        }

        int totalWidth = powers.size() * POWER_BUTTON_SIZE
                + (powers.size() - 1) * (POWER_BUTTON_SPACING - POWER_BUTTON_SIZE);

        int startX = panelX + (PANEL_WIDTH / 2) - (totalWidth / 2);
        int y = panelY + POWER_BUTTON_Y;

        for (int i = 0; i < powers.size(); i++) {
            String powerKey = powers.get(i);

            int x = startX + i * POWER_BUTTON_SPACING;

            if (isMouseOver(mouseX, mouseY, x, y, POWER_BUTTON_SIZE, POWER_BUTTON_SIZE)) {
                this.selectedPower = powerKey;
                return true;
            }
        }

        PowerCustomizationPanel panel = this.customizationPanels.get(this.selectedPower);

        if (panel != null) {
            return panel.mouseClicked(mouseX, mouseY, panelX, panelY, Minecraft.getInstance().player.containerMenu.containerId);
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

    @Override
    public boolean showsCustomization() {
        return true;
    }
}
package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.stonedgoldfish.eopmod.menu.GeneticSequencerMenu;
import java.util.HashMap;
import java.util.Map;

public class GeneticSequencerScreen extends AbstractContainerScreen<GeneticSequencerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "eop",
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer.png"
            );
    private final Map<Integer, SequencerPanel> panels = new HashMap<>();
    private boolean showInventory() {
        SequencerPanel panel = this.panels.get(this.selectedButton);
        return panel != null && panel.showsInventory();
    }
    private boolean showChipSlot() {
        SequencerPanel panel = this.panels.get(this.selectedButton);
        return panel != null && panel.showsChipSlot();
    }
    private boolean showFusionSlot() {
        SequencerPanel panel = this.panels.get(this.selectedButton);
        return panel != null && panel.showsFusionSlot();
    }
    private boolean showChimeraSlot() {
        SequencerPanel panel = this.panels.get(this.selectedButton);
        return panel != null && panel.showsChimeraSlot();
    }
    private void updateSlotVisibility() {
        this.menu.chipSlot.setVisible(showChipSlot());
        this.menu.fusionSlot.setVisible(showFusionSlot());
        this.menu.chimeraSlot.setVisible(showChimeraSlot());
    }

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 219;
    private static final int BUTTON_WIDTH = 21;
    private static final int BUTTON_HEIGHT = 21;
    private static final int BUTTON_NORMAL_U = 0;
    private static final int BUTTON_NORMAL_V = 219;
    private static final int BUTTON_HOVER_U = 21;
    private static final int BUTTON_HOVER_V = 219;
    private static final int BUTTON_SELECTED_U = 42;
    private static final int BUTTON_SELECTED_V = 219;

    private static final List<SequencerButton> BUTTONS = List.of(
            new SequencerButton(0, 37, 139, 63, 219),
            new SequencerButton(1, 12, 164, 84, 219),
            new SequencerButton(2, 37, 164, 105, 219),

            new SequencerButton(3, 166, 139, 126, 219),
            new SequencerButton(4, 166, 164, 147, 219),
            new SequencerButton(5, 191, 164, 168, 219)
    );

    private int selectedButton = -1;

    public GeneticSequencerScreen(GeneticSequencerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;

        this.panels.put(0, new ChipSequencerPanel());
        this.panels.put(1, new FusionSequencerPanel());
        this.panels.put(2, new ChimeraSequencerPanel());
        updateSlotVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        if (showInventory()) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            renderBg(guiGraphics, partialTick, mouseX, mouseY);
        }

        int panelX = getPanelX();
        int panelY = getPanelY();

        for (SequencerButton button : BUTTONS) {
            renderButton(guiGraphics, mouseX, mouseY, panelX, panelY, button);
        }

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 9, 0xFFFFFF);

        if (showInventory()) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        guiGraphics.blit(TEXTURE, panelX, panelY, 0, 0,
                PANEL_WIDTH, PANEL_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        SequencerPanel activePanel = this.panels.get(this.selectedButton);

        if (activePanel instanceof ChipSequencerPanel chipPanel) {
            chipPanel.setSuccessRate(this.menu.getChipSuccessRate());
        }

        if (activePanel != null) {
            activePanel.render(guiGraphics, mouseX, mouseY, partialTick, panelX, panelY);
        }
    }

    private void renderButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int panelX, int panelY, SequencerButton button) {
        int x = panelX + button.x();
        int y = panelY + button.y();

        boolean hovered = isMouseOver(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        boolean selected = selectedButton == button.id();

        int u = selected ? BUTTON_SELECTED_U : hovered ? BUTTON_HOVER_U : BUTTON_NORMAL_U;
        int v = selected ? BUTTON_SELECTED_V : hovered ? BUTTON_HOVER_V : BUTTON_NORMAL_V;

        guiGraphics.blit(TEXTURE, x, y, u, v,
                BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.blit(TEXTURE, x, y, button.iconU(), button.iconV(),
                BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        if (!showInventory()) {
            return false;
        }

        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int panelX = getPanelX();
            int panelY = getPanelY();

            for (SequencerButton button : BUTTONS) {
                int x = panelX + button.x();
                int y = panelY + button.y();

                if (isMouseOver(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    selectedButton = selectedButton == button.id() ? -1 : button.id();
                    updateSlotVisibility();

                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            GeneticSequencerMenu.SET_ACTIVE_PANEL_BASE_ID + button.id()
                    );

                    playButtonClick();
                    return true;
                }
            }

            if (selectedButton == 0 &&
                    ChipSequencerPanel.isApplyButtonHovered(mouseX, mouseY, panelX, panelY)) {

                Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        GeneticSequencerMenu.CHIP_APPLY_BUTTON_ID
                );

                playButtonClick();
                return true;
            }

            if (selectedButton == 1 &&
                    FusionSequencerPanel.isApplyButtonHovered(mouseX, mouseY, panelX, panelY)) {

                Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        GeneticSequencerMenu.FUSION_APPLY_BUTTON_ID
                );

                playButtonClick();
                return true;
            }

            if (selectedButton == 2 &&
                    ChimeraSequencerPanel.isApplyButtonHovered(mouseX, mouseY, panelX, panelY)) {

                Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                        this.menu.containerId,
                        GeneticSequencerMenu.CHIMERA_APPLY_BUTTON_ID
                );

                playButtonClick();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!showInventory()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private int getPanelX() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int getPanelY() {
        return (this.height - PANEL_HEIGHT) / 2;
    }

    private void playButtonClick() {
        Minecraft.getInstance()
                .getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record SequencerButton(int id, int x, int y, int iconU, int iconV) {}
}
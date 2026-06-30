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
    private boolean showEvolutionSlot() {
        SequencerPanel panel = this.panels.get(this.selectedButton);
        return panel != null && panel.showsEvolutionSlot();
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
        this.menu.evolutionSlot.setVisible(showEvolutionSlot());
        this.menu.setPlayerInventoryVisible(showInventory());
    }

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 219;
    private static final int BUTTON_WIDTH = 17;
    private static final int BUTTON_HEIGHT = 17;
    private static final int BUTTON_NORMAL_U = 0;
    private static final int BUTTON_NORMAL_V = 219;
    private static final int BUTTON_HOVER_U = 17;
    private static final int BUTTON_HOVER_V = 219;
    private static final int BUTTON_SELECTED_U = 34;
    private static final int BUTTON_SELECTED_V = 219;

    private static final List<SequencerButton> BUTTONS = List.of(
            new SequencerButton(0, 51, 140, 51, 219),
            new SequencerButton(1, 51, 158, 68, 219),
            new SequencerButton(2, 51, 176, 85, 219),

            new SequencerButton(3, 160, 140, 102, 219),
            new SequencerButton(4, 160, 158, 119, 219),
            new SequencerButton(5, 160, 176, 136, 219)
    );

    private int selectedButton = -1;

    public GeneticSequencerScreen(GeneticSequencerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;

        this.panels.put(0, new ChipSequencerPanel());
        this.panels.put(1, new FusionSequencerPanel());
        this.panels.put(2, new ChimeraSequencerPanel());
        this.panels.put(3, new CustomizationSequencerPanel());
        this.panels.put(4, new ScanSequencerPanel());
        this.panels.put(5, new AwakeningSequencerPanel());
        updateSlotVisibility();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (selectedButton == 3) {
            int panelX = getPanelX();
            int panelY = getPanelY();

            SequencerPanel activePanel = this.panels.get(this.selectedButton);

            if (activePanel instanceof CustomizationSequencerPanel customizationPanel) {
                if (customizationPanel.mouseDragged(mouseX, mouseY, panelX, panelY)) {
                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selectedButton == 3) {
            int panelX = getPanelX();
            int panelY = getPanelY();

            SequencerPanel activePanel = this.panels.get(this.selectedButton);

            if (activePanel instanceof CustomizationSequencerPanel customizationPanel) {
                if (customizationPanel.mouseReleased(mouseX, mouseY, panelX, panelY)) {
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (selectedButton == 3) {
            SequencerPanel activePanel = this.panels.get(this.selectedButton);

            if (activePanel instanceof CustomizationSequencerPanel customizationPanel) {
                if (customizationPanel.charTyped(codePoint, modifiers)) {
                    return true;
                }
            }
        }

        return super.charTyped(codePoint, modifiers);
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

            if (selectedButton == 3) {
                SequencerPanel activePanel = this.panels.get(this.selectedButton);

                if (activePanel instanceof CustomizationSequencerPanel customizationPanel) {
                    if (customizationPanel.mouseClicked(mouseX, mouseY, panelX, panelY)) {
                        playButtonClick();
                        return true;
                    }
                }
            }

            if (selectedButton == 4) {
                SequencerPanel activePanel = this.panels.get(this.selectedButton);

                if (activePanel instanceof ScanSequencerPanel scanPanel) {
                    if (scanPanel.mouseClicked(mouseX, mouseY, getPanelX(), getPanelY())) {
                        playButtonClick();
                        return true;
                    }
                }
            }

            if (selectedButton == 5) {
                SequencerPanel activePanel = this.panels.get(this.selectedButton);

                if (activePanel instanceof AwakeningSequencerPanel awakeningPanel) {
                    if (awakeningPanel.mouseClicked(mouseX, mouseY, panelX, panelY)) {
                        updateSlotVisibility();
                        playButtonClick();
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedButton == 3) {
            SequencerPanel activePanel = this.panels.get(this.selectedButton);

            if (activePanel instanceof CustomizationSequencerPanel customizationPanel) {
                if (customizationPanel.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
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
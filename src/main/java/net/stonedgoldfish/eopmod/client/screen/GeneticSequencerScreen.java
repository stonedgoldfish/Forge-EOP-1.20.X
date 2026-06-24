package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import java.util.List;
import net.stonedgoldfish.eopmod.client.screen.ChipSequencerPanel;
import net.stonedgoldfish.eopmod.client.screen.SequencerPanel;
import java.util.HashMap;
import java.util.Map;

public class GeneticSequencerScreen extends Screen {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "eop",
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer.png"
            );
    private static final int CHIP_BUTTON_ID = 0;
    private final Map<Integer, SequencerPanel> panels = new HashMap<>();

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

    public GeneticSequencerScreen() {
        super(Component.literal("Genetic Sequencer"));
        this.panels.put(CHIP_BUTTON_ID, new ChipSequencerPanel());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int panelX = getPanelX();
        int panelY = getPanelY();

        guiGraphics.blit(TEXTURE, panelX, panelY, 0, 0,
                PANEL_WIDTH, PANEL_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        for (SequencerButton button : BUTTONS) {
            renderButton(guiGraphics, mouseX, mouseY, panelX, panelY, button);
        }

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 9, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        SequencerPanel activePanel = this.panels.get(this.selectedButton);

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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int panelX = getPanelX();
            int panelY = getPanelY();

            for (SequencerButton button : BUTTONS) {
                int x = panelX + button.x();
                int y = panelY + button.y();

                if (isMouseOver(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    selectedButton = selectedButton == button.id() ? -1 : button.id();
                    playButtonClick();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
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
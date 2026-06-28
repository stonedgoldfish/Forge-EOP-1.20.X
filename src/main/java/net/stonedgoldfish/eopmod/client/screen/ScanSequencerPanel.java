package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.threetag.palladium.power.SuperpowerUtil;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;

public class ScanSequencerPanel implements SequencerPanel {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_icons.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 50;

    private static final int BASE_TEXTURE_U = 0;
    private static final int BASE_TEXTURE_V = 0;
    private static final int BASE_TEXTURE_X = 102;
    private static final int BASE_TEXTURE_Y = 145;
    private static final int BASE_TEXTURE_WIDTH = 84;
    private static final int BASE_TEXTURE_HEIGHT = 84;

    private static final int ANIM_TEXTURE_U = 0;
    private static final int ANIM_TEXTURE_V = 192;
    private static final int ANIM_FRAME_WIDTH = 23;
    private static final int ANIM_FRAME_HEIGHT = 23;
    private static final int ANIM_FRAME_COUNT = 5;
    private static final int ANIM_TICKS_PER_FRAME = 5;

    private static final int SCAN_BUTTON_X = 95;
    private static final int SCAN_BUTTON_Y = 182;
    private static final int SCAN_BUTTON_WIDTH = 37;
    private static final int SCAN_BUTTON_HEIGHT = 13;

    private static final int SCAN_BUTTON_NORMAL_U = 0;
    private static final int SCAN_BUTTON_NORMAL_V = 216;
    private static final int SCAN_BUTTON_HOVER_U = 37;
    private static final int SCAN_BUTTON_HOVER_V = 216;

    private static final int POWER_BUTTON_SIZE = 21;
    private static final int POWER_BUTTON_Y = 152;
    private static final int POWER_BUTTON_SPACING = 25;
    private static final int POWER_BUTTON_CENTER_X = 113;

    private static final int POWER_BUTTON_NORMAL_U = 0;
    private static final int POWER_BUTTON_NORMAL_V = 230;
    private static final int POWER_BUTTON_HOVER_U = 21;
    private static final int POWER_BUTTON_HOVER_V = 230;
    private static final int POWER_BUTTON_SELECTED_U = 42;
    private static final int POWER_BUTTON_SELECTED_V = 230;

    private static final int POWER_BUTTONS_PER_ROW = 2;
    private static final int POWER_BUTTON_ROWS = 2;
    private static final int POWER_BUTTONS_PER_PAGE = POWER_BUTTONS_PER_ROW * POWER_BUTTON_ROWS;

    private static final int POWER_BUTTON_ROW_SPACING = 25;

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

    private int powerPage = 0;

    private String selectedPower = null;

    private boolean scanning = false;
    private boolean scanComplete = false;
    private long scanStartTick = 0L;

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

        guiGraphics.blit(
                TEXTURE,
                panelX + BASE_TEXTURE_X,
                panelY + BASE_TEXTURE_Y,
                BASE_TEXTURE_U,
                BASE_TEXTURE_V,
                BASE_TEXTURE_WIDTH,
                BASE_TEXTURE_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        if (scanning) {
            long gameTime = Minecraft.getInstance().level != null
                    ? Minecraft.getInstance().level.getGameTime()
                    : 0L;

            long elapsed = gameTime - scanStartTick;

            if (elapsed >= 100) {
                scanning = false;
                scanComplete = true;
            } else {
                int frame = (int) ((elapsed / ANIM_TICKS_PER_FRAME) % ANIM_FRAME_COUNT);

                guiGraphics.blit(
                        TEXTURE,
                        panelX + BASE_TEXTURE_X,
                        panelY + BASE_TEXTURE_Y,
                        ANIM_TEXTURE_U + frame * ANIM_FRAME_WIDTH,
                        ANIM_TEXTURE_V,
                        ANIM_FRAME_WIDTH,
                        ANIM_FRAME_HEIGHT,
                        TEXTURE_WIDTH,
                        TEXTURE_HEIGHT
                );
            }
        }

        if (scanning) {
            guiGraphics.drawCenteredString(
                    font,
                    getScanningText(),
                    panelX + PANEL_WIDTH / 2,
                    panelY + 65,
                    0xFFFF55
            );
        } else if (scanComplete) {
            renderPowerStatus(guiGraphics, mouseX, mouseY, panelX, panelY);
        }

        drawScanButton(guiGraphics, mouseX, mouseY, panelX, panelY);
    }

    private static String getScanningText() {
        long gameTime = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;

        int frame = (int) ((gameTime / 10) % 3);

        return switch (frame) {
            case 0 -> "Scanning.";
            case 1 -> "Scanning..";
            default -> "Scanning...";
        };
    }

    private void renderPowerStatus(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY
    ) {
        var player = Minecraft.getInstance().player;
        var font = Minecraft.getInstance().font;

        if (player == null) {
            return;
        }

        int y = panelY + 35;
        int lineHeight = 10;
        int infoX = panelX + 30;

        boolean dnaCorrupted = hasAbility(player, "base", "DNA.Corrupted");
        boolean chimeraCoreIntegrated = hasAbility(player, "base", "Chimera.Core");

        java.util.List<EOPPowerRegistry.EOPPower> ownedPowers = getOwnedPowers();

        if (ownedPowers.isEmpty()) {
            guiGraphics.drawCenteredString(
                    font,
                    "No power detected",
                    panelX + PANEL_WIDTH / 2,
                    y,
                    0xAAAAAA
            );

            y += lineHeight;

            if (dnaCorrupted) {
                guiGraphics.drawString(font, "• DNA Corrupted!", infoX, y + 3, 0xFF5555, false);
                y += lineHeight;
            }

            if (chimeraCoreIntegrated) {
                guiGraphics.drawString(font, "• Chimera Core Integrated", infoX, y + 3, 0x55FFAA, false);
                y += lineHeight;
            }

            guiGraphics.drawString(font, "• Power Slots: 0/2", infoX, y + 3, 0xFFFFFF, false);
            return;
        }

        if (selectedPower == null || ownedPowers.stream().noneMatch(power -> power.key().equals(selectedPower))) {
            selectedPower = ownedPowers.get(0).key();
        }

        renderPowerButtons(guiGraphics, mouseX, mouseY, panelX, panelY, ownedPowers);

        EOPPowerRegistry.EOPPower selected = getSelectedPower(ownedPowers);

        if (selected == null) {
            return;
        }

        if (dnaCorrupted) {
            guiGraphics.drawString(font, "• DNA Corrupted!", infoX, y + 3, 0xFF5555, false);
            y += lineHeight;
        }

        if (chimeraCoreIntegrated) {
            guiGraphics.drawString(font, "• Chimera Core Integrated", infoX, y + 3, 0x55FFAA, false);
            y += lineHeight;
        }

        int maxPowerSlots = selected.soloPower() ? 1 : 2;
        int usedPowerSlots = Math.min(ownedPowers.size(), maxPowerSlots);

        drawPowerSlotsLine(
                guiGraphics,
                infoX,
                y + 3,
                usedPowerSlots,
                maxPowerSlots
        );

        y += lineHeight;

        guiGraphics.drawCenteredString(
                font,
                selected.display().replace("_", " "),
                panelX + PANEL_WIDTH / 2,
                y + 6,
                0xFF000000 | selected.titleColor()
        );

        y += lineHeight;

        drawBooleanLine(guiGraphics, infoX, y + 9, "• Awakening Potential: ", selected.hasAwakening());
        y += lineHeight;

        drawCompatibilityLine(guiGraphics, infoX, y + 9, selected.soloPower());
        y += lineHeight;

        drawBooleanLine(guiGraphics, infoX, y + 9, "• Fusion Compatible: ", selected.fusionComponent());
    }

    private java.util.List<EOPPowerRegistry.EOPPower> getOwnedPowers() {
        var player = Minecraft.getInstance().player;
        java.util.List<EOPPowerRegistry.EOPPower> ownedPowers = new java.util.ArrayList<>();

        if (player == null) {
            return ownedPowers;
        }

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            if (SuperpowerUtil.hasSuperpower(
                    player,
                    ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, power.key())
            )) {
                ownedPowers.add(power);
            }
        }

        return ownedPowers;
    }

    private void drawCompatibilityLine(
            GuiGraphics guiGraphics,
            int x,
            int y,
            boolean soloPower
    ) {
        var font = Minecraft.getInstance().font;

        String label = "• Power Compatibility: ";
        String value = soloPower ? "Exclusive" : "Compatible";
        int color = soloPower ? 0xFF5555 : 0x55FF55;

        guiGraphics.drawString(font, label, x, y, 0xFFFFFF, false);
        guiGraphics.drawString(font, value, x + font.width(label), y, color, false);
    }

    private void drawPowerSlotsLine(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int usedSlots,
            int maxSlots
    ) {
        var font = Minecraft.getInstance().font;

        String label = "• Power Slots: ";
        String value = usedSlots + "/" + maxSlots;

        int valueColor = (usedSlots >= maxSlots) ? 0xFF5555 : 0x55FF55;

        guiGraphics.drawString(
                font,
                label,
                x,
                y,
                0xFFFFFF,
                false
        );

        guiGraphics.drawString(
                font,
                value,
                x + font.width(label),
                y,
                valueColor,
                false
        );
    }

    private void drawBooleanLine(
            GuiGraphics guiGraphics,
            int x,
            int y,
            String label,
            boolean value
    ) {
        var font = Minecraft.getInstance().font;

        String text = value ? "Yes" : "No";
        int color = value ? 0x55FF55 : 0xFF5555;

        guiGraphics.drawString(
                font,
                label,
                x,
                y,
                0xFFFFFF,
                false
        );

        guiGraphics.drawString(
                font,
                text,
                x + font.width(label),
                y,
                color,
                false
        );
    }

    private EOPPowerRegistry.EOPPower getSelectedPower(java.util.List<EOPPowerRegistry.EOPPower> ownedPowers) {
        for (EOPPowerRegistry.EOPPower power : ownedPowers) {
            if (power.key().equals(selectedPower)) {
                return power;
            }
        }

        return null;
    }

    private void renderPowerButtons(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY,
            java.util.List<EOPPowerRegistry.EOPPower> powers
    ) {
        int maxPage = Math.max(0, (powers.size() - 1) / POWER_BUTTONS_PER_PAGE);

        if (powerPage > maxPage) {
            powerPage = maxPage;
        }

        int startIndex = powerPage * POWER_BUTTONS_PER_PAGE;
        int endIndex = Math.min(startIndex + POWER_BUTTONS_PER_PAGE, powers.size());

        int visibleCount = endIndex - startIndex;

        for (int localIndex = 0; localIndex < visibleCount; localIndex++) {
            int actualIndex = startIndex + localIndex;

            EOPPowerRegistry.EOPPower power = powers.get(actualIndex);

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
            boolean selected = power.key().equals(this.selectedPower);

            int u = selected ? POWER_BUTTON_SELECTED_U : hovered ? POWER_BUTTON_HOVER_U : POWER_BUTTON_NORMAL_U;
            int v = selected ? POWER_BUTTON_SELECTED_V : hovered ? POWER_BUTTON_HOVER_V : POWER_BUTTON_NORMAL_V;

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

            ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/" + power.key() + ".png"
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

        int u = hovered ? NEXT_PAGE_HOVER_U : NEXT_PAGE_NORMAL_U;
        int v = hovered ? NEXT_PAGE_HOVER_V : NEXT_PAGE_NORMAL_V;

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                u,
                v,
                NEXT_PAGE_BUTTON_WIDTH,
                NEXT_PAGE_BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private void renderPrevPageButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int panelX, int panelY) {
        int x = panelX + PREV_PAGE_BUTTON_X;
        int y = panelY + PREV_PAGE_BUTTON_Y;

        boolean hovered = isMouseOver(mouseX, mouseY, x, y, PREV_PAGE_BUTTON_WIDTH, PREV_PAGE_BUTTON_HEIGHT);

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

    private static String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private static boolean hasAbility(LivingEntity entity, String powerKey, String abilityKey) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, powerKey),
                abilityKey
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }

    private void drawScanButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY
    ) {
        var font = Minecraft.getInstance().font;

        int x = panelX + SCAN_BUTTON_X;
        int y = panelY + SCAN_BUTTON_Y;

        boolean hovered = isMouseOver(mouseX, mouseY, x, y, SCAN_BUTTON_WIDTH, SCAN_BUTTON_HEIGHT);

        int u = hovered ? SCAN_BUTTON_HOVER_U : SCAN_BUTTON_NORMAL_U;
        int v = hovered ? SCAN_BUTTON_HOVER_V : SCAN_BUTTON_NORMAL_V;

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                u,
                v,
                SCAN_BUTTON_WIDTH,
                SCAN_BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        guiGraphics.drawCenteredString(
                font,
                "Scan",
                x + SCAN_BUTTON_WIDTH / 2,
                y + 3,
                0xFFFFFF
        );
    }

    public boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY) {
        if (scanComplete && handlePowerButtonClick(mouseX, mouseY, panelX, panelY)) {
            return true;
        }

        int x = panelX + SCAN_BUTTON_X;
        int y = panelY + SCAN_BUTTON_Y;

        if (!isMouseOver(mouseX, mouseY, x, y, SCAN_BUTTON_WIDTH, SCAN_BUTTON_HEIGHT)) {
            return false;
        }

        long gameTime = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;

        this.scanning = true;
        this.scanComplete = false;
        this.scanStartTick = gameTime;
        this.selectedPower = null;

        return true;
    }

    private boolean handlePowerButtonClick(double mouseX, double mouseY, int panelX, int panelY) {
        java.util.List<EOPPowerRegistry.EOPPower> ownedPowers = getOwnedPowers();

        if (ownedPowers.isEmpty()) {
            return false;
        }

        int maxPage = Math.max(0, (ownedPowers.size() - 1) / POWER_BUTTONS_PER_PAGE);

        if (ownedPowers.size() > POWER_BUTTONS_PER_PAGE) {
            int prevX = panelX + PREV_PAGE_BUTTON_X;
            int prevY = panelY + PREV_PAGE_BUTTON_Y;

            if (isMouseOver(mouseX, mouseY, prevX, prevY, PREV_PAGE_BUTTON_WIDTH, PREV_PAGE_BUTTON_HEIGHT)) {
                this.powerPage--;

                if (this.powerPage < 0) {
                    this.powerPage = maxPage;
                }

                int startIndex = this.powerPage * POWER_BUTTONS_PER_PAGE;

                if (startIndex < ownedPowers.size()) {
                    this.selectedPower = ownedPowers.get(startIndex).key();
                }

                return true;
            }
        }

        if (ownedPowers.size() > POWER_BUTTONS_PER_PAGE) {
            int nextX = panelX + NEXT_PAGE_BUTTON_X;
            int nextY = panelY + NEXT_PAGE_BUTTON_Y;

            if (isMouseOver(
                    mouseX,
                    mouseY,
                    nextX,
                    nextY,
                    NEXT_PAGE_BUTTON_WIDTH,
                    NEXT_PAGE_BUTTON_HEIGHT
            )) {
                this.powerPage++;

                if (this.powerPage > maxPage) {
                    this.powerPage = 0;
                }

                int startIndex = this.powerPage * POWER_BUTTONS_PER_PAGE;

                if (startIndex < ownedPowers.size()) {
                    this.selectedPower = ownedPowers.get(startIndex).key();
                }

                return true;
            }
        }

        int startIndex = powerPage * POWER_BUTTONS_PER_PAGE;
        int endIndex = Math.min(startIndex + POWER_BUTTONS_PER_PAGE, ownedPowers.size());
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
            int y = getPowerButtonY(panelY, ownedPowers.size(), row);

            if (isMouseOver(mouseX, mouseY, x, y, POWER_BUTTON_SIZE, POWER_BUTTON_SIZE)) {
                this.selectedPower = ownedPowers.get(actualIndex).key();
                return true;
            }
        }

        return false;
    }

    private static int getPowerButtonY(int panelY, int totalPowers, int row) {
        int yOffset = totalPowers > 2 ? -17 : 0;

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
}
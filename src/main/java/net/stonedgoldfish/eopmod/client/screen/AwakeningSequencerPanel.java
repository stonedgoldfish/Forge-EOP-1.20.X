package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.menu.GeneticSequencerMenu;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.threetag.palladium.power.SuperpowerUtil;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;

public class AwakeningSequencerPanel implements SequencerPanel {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_icons.png"
            );

    private static final ResourceLocation INVENTORY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_genetic_sequencer_awakening_inventory.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_WIDTH = 230;

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

    private static final int EVOLVE_BUTTON_X = 92;
    private static final int EVOLVE_BUTTON_Y = 160;
    private static final int EVOLVE_BUTTON_WIDTH = 44;
    private static final int EVOLVE_BUTTON_HEIGHT = 13;

    private static final int EVOLVE_NORMAL_U = 120;
    private static final int EVOLVE_NORMAL_V = 200;
    private static final int EVOLVE_HOVER_U = 164;
    private static final int EVOLVE_HOVER_V = 200;

    private int powerPage = 0;

    private String selectedPower = null;

    private boolean showInventory = false;

    private static final int INVENTORY_TOGGLE_X = 27;
    private static final int INVENTORY_TOGGLE_Y = 108;
    private static final int INVENTORY_TOGGLE_WIDTH = 10;
    private static final int INVENTORY_TOGGLE_HEIGHT = 10;

    private static final int INVENTORY_TOGGLE_NORMAL_U = 201;
    private static final int INVENTORY_TOGGLE_NORMAL_V = 218;
    private static final int INVENTORY_TOGGLE_HOVER_U = 211;
    private static final int INVENTORY_TOGGLE_HOVER_V = 218;
    private static final int INVENTORY_TOGGLE_SELECTED_U = 221;
    private static final int INVENTORY_TOGGLE_SELECTED_V = 218;

    private static String errorMessage = "";
    private static long errorEndTime = 0L;

    public static void showError(String message) {
        errorMessage = message;
        errorEndTime = System.currentTimeMillis() + 5000L;
    }

    @Override
    public boolean showsInventory() {
        return this.showInventory;
    }

    @Override
    public boolean showsEvolutionSlot() {
        return this.showInventory;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int panelX, int panelY) {
        var player = Minecraft.getInstance().player;
        var font = Minecraft.getInstance().font;

        if (player == null) {
            return;
        }

        int centerX = panelX + PANEL_WIDTH / 2;
        int x = panelX + 35;
        int y = panelY + 55;
        int lineHeight = 13;

        java.util.List<EOPPowerRegistry.EOPPower> ownedPowers = getOwnedPowers();

        if (ownedPowers.isEmpty()) {
            guiGraphics.drawCenteredString(font, "No awakening", centerX, y, 0xFFFFFF);
            return;
        }

        if (selectedPower == null || ownedPowers.stream().noneMatch(power -> power.key().equals(selectedPower))) {
            selectedPower = ownedPowers.get(0).key();
        }

        EOPPowerRegistry.EOPPower ownedPower = getSelectedPower(ownedPowers);

        boolean anyPowerHasAwakening = ownedPowers.stream()
                .anyMatch(EOPPowerRegistry.EOPPower::hasAwakening);

        if (ownedPower == null || !ownedPower.hasAwakening()) {
            if (anyPowerHasAwakening && !this.showInventory) {
                renderPowerButtons(guiGraphics, mouseX, mouseY, panelX, panelY, ownedPowers);
            }

            guiGraphics.drawCenteredString(font, "No awakening", centerX, y, 0xFFFFFF);
            return;
        }

        boolean alreadyAwakened = hasAbility(player, "base", "Awakened");

        if (alreadyAwakened) {
            this.showInventory = false;

            guiGraphics.drawCenteredString(
                    font,
                    "Power already awakened!",
                    centerX,
                    panelY + 70,
                    0x55FF55
            );

            return;
        }

        drawInventoryToggleButton(guiGraphics, mouseX, mouseY, panelX, panelY);

        if (this.showInventory) {
            guiGraphics.blit(
                    INVENTORY_TEXTURE,
                    panelX,
                    panelY,
                    0,
                    0,
                    230,
                    219,
                    256,
                    256
            );

            drawInventoryToggleButton(guiGraphics, mouseX, mouseY, panelX, panelY);
            drawEvolveButton(guiGraphics, mouseX, mouseY, panelX, panelY);
            drawAwakeningError(guiGraphics, panelX, panelY);
            return;
        }


        renderPowerButtons(guiGraphics, mouseX, mouseY, panelX, panelY, ownedPowers);
        guiGraphics.drawCenteredString(
                font,
                "Awakening Requirements",
                centerX,
                panelY + 35,
                0xFFFFFF
        );

        int powerAmount = EOPPalladiumProperties.getPowerAmount(player);
        boolean exactlyOnePower = powerAmount == 1;

        int level = EOPPalladiumProperties.getLevel(player, ownedPower.key());
        boolean levelReady = level >= 25;

        boolean dnaClean = !isDNACorrupted(player);

        boolean allAbilitiesUnlocked = hasAbility(
                player,
                ownedPower.key(),
                "All.Abilities.Unlocked"
        );

        drawRequirementLine(guiGraphics, x, y, "Level: ", level + "/25", levelReady);
        y += lineHeight;

        drawRequirementLine(guiGraphics, x, y, "DNA: ", dnaClean ? "Clean" : "Corrupt", dnaClean);
        y += lineHeight;

        drawRequirementLine(guiGraphics, x, y, "Unlocked All Abilities: ", allAbilitiesUnlocked ? "True" : "False", allAbilitiesUnlocked);
        y += lineHeight;

        drawRequirementLine(guiGraphics, x, y, "Power Amount: ", powerAmount + "/1", exactlyOnePower);
    }

    private void drawAwakeningError(GuiGraphics guiGraphics, int panelX, int panelY) {
        if (errorMessage.isEmpty() || System.currentTimeMillis() > errorEndTime) {
            return;
        }

        var font = Minecraft.getInstance().font;

        String[] lines = errorMessage.split("\n");

        int startY = panelY + 173;

        for (int i = 0; i < lines.length; i++) {
            guiGraphics.drawCenteredString(
                    font,
                    lines[i],
                    panelX + PANEL_WIDTH / 2,
                    startY + i * 10,
                    0xFF5555
            );
        }
    }

    private void drawInventoryToggleButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int panelX,
            int panelY
    ) {
        var font = Minecraft.getInstance().font;

        int x = panelX + INVENTORY_TOGGLE_X;
        int y = panelY + INVENTORY_TOGGLE_Y;

        boolean hovered = isMouseOver(mouseX, mouseY, x, y, INVENTORY_TOGGLE_WIDTH, INVENTORY_TOGGLE_HEIGHT);

        int u = this.showInventory
                ? INVENTORY_TOGGLE_SELECTED_U
                : hovered
                  ? INVENTORY_TOGGLE_HOVER_U
                  : INVENTORY_TOGGLE_NORMAL_U;

        int v = this.showInventory
                ? INVENTORY_TOGGLE_SELECTED_V
                : hovered
                  ? INVENTORY_TOGGLE_HOVER_V
                  : INVENTORY_TOGGLE_NORMAL_V;

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                u,
                v,
                INVENTORY_TOGGLE_WIDTH,
                INVENTORY_TOGGLE_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    public boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY) {
        var player = Minecraft.getInstance().player;

        if (player == null) {
            return false;
        }

        java.util.List<EOPPowerRegistry.EOPPower> ownedPowers = getOwnedPowers();
        EOPPowerRegistry.EOPPower ownedPower = getSelectedPower(ownedPowers);

        boolean selectedHasAwakening = ownedPower != null && ownedPower.hasAwakening();
        boolean anyPowerHasAwakening = ownedPowers.stream().anyMatch(EOPPowerRegistry.EOPPower::hasAwakening);

        if (hasAbility(player, "base", "Awakened")) {
            this.showInventory = false;
            return false;
        }

        if (anyPowerHasAwakening && !this.showInventory) {
            if (handlePowerButtonClick(mouseX, mouseY, panelX, panelY)) {
                return true;
            }
        }

        if (!selectedHasAwakening) {
            this.showInventory = false;
            return false;
        }

        int invX = panelX + INVENTORY_TOGGLE_X;
        int invY = panelY + INVENTORY_TOGGLE_Y;

        if (isMouseOver(mouseX, mouseY, invX, invY, INVENTORY_TOGGLE_WIDTH, INVENTORY_TOGGLE_HEIGHT)) {
            this.showInventory = !this.showInventory;
            return true;
        }

        if (this.showInventory) {
            if (isMouseOver(
                    mouseX,
                    mouseY,
                    panelX + EVOLVE_BUTTON_X,
                    panelY + EVOLVE_BUTTON_Y,
                    EVOLVE_BUTTON_WIDTH,
                    EVOLVE_BUTTON_HEIGHT
            )) {
                Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                        Minecraft.getInstance().player.containerMenu.containerId,
                        GeneticSequencerMenu.AWAKEN_BUTTON_ID
                );

                return true;
            }

            return false;
        }

        return false;
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
        int yOffset = totalPowers > 2 ? -10 : 0;

        return panelY + POWER_BUTTON_Y + yOffset + row * POWER_BUTTON_ROW_SPACING;
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

    private void drawEvolveButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int panelX, int panelY) {
        var font = Minecraft.getInstance().font;

        int x = panelX + EVOLVE_BUTTON_X;
        int y = panelY + EVOLVE_BUTTON_Y;

        boolean hovered = isMouseOver(mouseX, mouseY, x, y, EVOLVE_BUTTON_WIDTH, EVOLVE_BUTTON_HEIGHT);

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                hovered ? EVOLVE_HOVER_U : EVOLVE_NORMAL_U,
                hovered ? EVOLVE_HOVER_V : EVOLVE_NORMAL_V,
                EVOLVE_BUTTON_WIDTH,
                EVOLVE_BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        guiGraphics.drawCenteredString(
                font,
                "Evolve",
                x + EVOLVE_BUTTON_WIDTH / 2,
                y + 3,
                0xFFFFFF
        );
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

    private EOPPowerRegistry.EOPPower getSelectedPower(java.util.List<EOPPowerRegistry.EOPPower> ownedPowers) {
        for (EOPPowerRegistry.EOPPower power : ownedPowers) {
            if (power.key().equals(this.selectedPower)) {
                return power;
            }
        }

        return null;
    }

    private static void drawRequirementLine(
            GuiGraphics guiGraphics,
            int x,
            int y,
            String label,
            String value,
            boolean passed
    ) {
        var font = Minecraft.getInstance().font;

        guiGraphics.drawString(
                font,
                "• " + label,
                x,
                y,
                0xAAAAAA,
                false
        );

        guiGraphics.drawString(
                font,
                value,
                x + font.width("• " + label),
                y,
                passed ? 0x55FF55 : 0xFF5555,
                false
        );
    }

    private static boolean hasAbility(LivingEntity entity, String powerKey, String abilityKey) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, powerKey),
                abilityKey
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }

    private static boolean isDNACorrupted(LivingEntity entity) {
        return hasAbility(entity, "base", "DNA.Corrupted");
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
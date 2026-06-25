package net.stonedgoldfish.eopmod.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.stonedgoldfish.eopmod.item.GeneticChipItem;
import net.stonedgoldfish.eopmod.menu.slot.ChimeraCoreSlot;
import net.stonedgoldfish.eopmod.menu.slot.ChipsSlot;
import net.stonedgoldfish.eopmod.menu.slot.FusionCatalystSlot;
import net.stonedgoldfish.eopmod.power.EOPPowerGrantHandler;

import static net.stonedgoldfish.eopmod.menu.slot.ChipsSlot.CHIP_TAG;

public class GeneticSequencerMenu extends AbstractContainerMenu {

    private static final int INVENTORY_X = 35;
    private static final int INVENTORY_Y = 29;
    private static final int HOTBAR_Y = 87;

    private final SimpleContainer chipContainer = new SimpleContainer(1);
    private final SimpleContainer fusionContainer = new SimpleContainer(1);
    private final SimpleContainer chimeraContainer = new SimpleContainer(1);

    public final ChipsSlot chipSlot;
    public final FusionCatalystSlot fusionSlot;
    public final ChimeraCoreSlot chimeraSlot;

    public static final int PANEL_CHIP = 0;
    public static final int PANEL_FUSION = 1;
    public static final int PANEL_CHIMERA = 2;

    public static final int SET_ACTIVE_PANEL_BASE_ID = 100;

    private int activePanel = -1;

    public static final int CHIP_SLOT_INDEX = 0;
    public static final int FUSION_SLOT_INDEX = 1;
    public static final int CHIMERA_SLOT_INDEX = 2;

    public static final int PLAYER_INV_START = 3;
    public static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    public static final int CHIP_APPLY_BUTTON_ID = 0;
    public static final int FUSION_APPLY_BUTTON_ID = 1;
    public static final int CHIMERA_APPLY_BUTTON_ID = 2;

    public static final int MENDER_CLAW_TYPE_BASE_ID = 200;

    public GeneticSequencerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }

    public GeneticSequencerMenu(int containerId, Inventory playerInventory) {
        super(EOPMenus.GENETIC_SEQUENCER_MENU.get(), containerId);

        this.chipSlot = new ChipsSlot(chipContainer, 0, 108, 135);
        this.addSlot(this.chipSlot);

        this.fusionSlot = new FusionCatalystSlot(fusionContainer, 0, 108, 135);
        this.addSlot(this.fusionSlot);

        this.chimeraSlot = new ChimeraCoreSlot(chimeraContainer, 0, 108, 135);
        this.addSlot(this.chimeraSlot);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        INVENTORY_X + col * 18,
                        INVENTORY_Y + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    INVENTORY_X + col * 18,
                    HOTBAR_Y
            ));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (id >= MENDER_CLAW_TYPE_BASE_ID && id < MENDER_CLAW_TYPE_BASE_ID + 11) {
            int clawType = id - MENDER_CLAW_TYPE_BASE_ID;

            serverPlayer.getServer().getCommands().performPrefixedCommand(
                    serverPlayer.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2),
                    "scoreboard players set @s EOP.Claw.Type " + clawType
            );

            return true;
        }

        if (id >= SET_ACTIVE_PANEL_BASE_ID) {
            int panelId = id - SET_ACTIVE_PANEL_BASE_ID;

            this.activePanel = this.activePanel == panelId ? -1 : panelId;

            return true;
        }

        if (id == CHIP_APPLY_BUTTON_ID) {
            return applyChip(serverPlayer);
        }

        if (id == FUSION_APPLY_BUTTON_ID) {
            return applyFusion(serverPlayer);
        }

        if (id == CHIMERA_APPLY_BUTTON_ID) {
            return applyChimeraCore(serverPlayer);
        }

        return false;
    }

    private boolean applyChip(ServerPlayer serverPlayer) {
        ItemStack chipStack = this.chipSlot.getItem();

        if (chipStack.isEmpty()) {
            return false;
        }

        if (!(chipStack.getItem() instanceof GeneticChipItem chipItem)) {
            return false;
        }

        String powerKey = chipItem.getPowerKey();
        double successRate = chipItem.getSuccessRate();

        chipStack.shrink(1);
        this.chipSlot.setChanged();

        EOPPowerGrantHandler.GrantResult result =
                EOPPowerGrantHandler.tryGrantPower(
                        serverPlayer,
                        powerKey,
                        successRate
                );

        if (result == EOPPowerGrantHandler.GrantResult.SUCCESS) {
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.blockPosition(),
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        } else if (result == EOPPowerGrantHandler.GrantResult.FAILED_CHANCE) {
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.blockPosition(),
                    SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        EOPPowerGrantHandler.sendResultMessage(
                serverPlayer,
                result,
                powerKey,
                EOPPowerGrantHandler.GrantSource.CHIP
        );

        return true;
    }

    private boolean applyFusion(ServerPlayer serverPlayer) {
        ItemStack catalystStack = this.fusionSlot.getItem();

        if (catalystStack.isEmpty()) {
            return false;
        }

        boolean success = EOPPowerGrantHandler.tryFuseCurrentPowers(serverPlayer);

        if (success) {
            catalystStack.shrink(1);
            this.fusionSlot.setChanged();
        }

        return true;
    }

    private boolean applyChimeraCore(ServerPlayer serverPlayer) {
        ItemStack chimeraStack = this.chimeraSlot.getItem();

        if (chimeraStack.isEmpty()) {
            return false;
        }

        boolean success = EOPPowerGrantHandler.tryUseChimeraCore(serverPlayer);

        if (success) {
            chimeraStack.shrink(1);
            this.chimeraSlot.setChanged();
        }

        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(index);

        if (clickedSlot != null && clickedSlot.hasItem()) {
            ItemStack clickedStack = clickedSlot.getItem();
            originalStack = clickedStack.copy();

            if (index == CHIP_SLOT_INDEX || index == FUSION_SLOT_INDEX || index == CHIMERA_SLOT_INDEX) {
                if (!this.moveItemStackTo(clickedStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.activePanel == PANEL_CHIP && clickedStack.is(CHIP_TAG)) {
                    if (!this.moveItemStackTo(clickedStack, CHIP_SLOT_INDEX, CHIP_SLOT_INDEX + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.activePanel == PANEL_FUSION && this.fusionSlot.mayPlace(clickedStack)) {
                    if (!this.moveItemStackTo(clickedStack, FUSION_SLOT_INDEX, FUSION_SLOT_INDEX + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.activePanel == PANEL_CHIMERA && this.chimeraSlot.mayPlace(clickedStack)) {
                    if (!this.moveItemStackTo(clickedStack, CHIMERA_SLOT_INDEX, CHIMERA_SLOT_INDEX + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (clickedStack.isEmpty()) {
                clickedSlot.set(ItemStack.EMPTY);
            } else {
                clickedSlot.setChanged();
            }
        }

        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide) {
            this.clearContainer(player, this.chipContainer);
            this.clearContainer(player, this.fusionContainer);
            this.clearContainer(player, this.chimeraContainer);
        }
    }

    public double getChipSuccessRate() {
        ItemStack stack = this.chipSlot.getItem();

        if (stack.getItem() instanceof GeneticChipItem chipItem) {
            return chipItem.getSuccessRate();
        }

        return 0.0;
    }
}
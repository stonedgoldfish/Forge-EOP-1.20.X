package net.stonedgoldfish.eopmod.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.client.screen.AwakeningSequencerPanel;
import net.stonedgoldfish.eopmod.item.GeneticChipItem;
import net.stonedgoldfish.eopmod.menu.slot.*;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.power.EOPPowerGrantHandler;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.ShowAwakeningErrorPacket;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;

import static net.stonedgoldfish.eopmod.menu.slot.ChipsSlot.CHIP_TAG;

public class GeneticSequencerMenu extends AbstractContainerMenu {

    private static final int INVENTORY_X = 33;
    private static final int INVENTORY_Y = 33;
    private static final int HOTBAR_Y = 91;

    private final SimpleContainer chipContainer = new SimpleContainer(1);
    private final SimpleContainer fusionContainer = new SimpleContainer(1);
    private final SimpleContainer chimeraContainer = new SimpleContainer(1);
    private final SimpleContainer evolutionContainer = new SimpleContainer(1);

    public final ChipsSlot chipSlot;
    public final FusionCatalystSlot fusionSlot;
    public final ChimeraCoreSlot chimeraSlot;
    public final EvolutionItemSlot evolutionSlot;

    public static final int PANEL_CHIP = 0;
    public static final int PANEL_FUSION = 1;
    public static final int PANEL_CHIMERA = 2;
    public static final int PANEL_AWAKENING = 5;

    public static final int SET_ACTIVE_PANEL_BASE_ID = 100;

    private int activePanel = -1;

    public static final int CHIP_SLOT_INDEX = 0;
    public static final int FUSION_SLOT_INDEX = 1;
    public static final int CHIMERA_SLOT_INDEX = 2;
    public static final int EVOLUTION_SLOT_INDEX = 3;

    public static final int PLAYER_INV_START = 4;
    public static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    public static final int CHIP_APPLY_BUTTON_ID = 0;
    public static final int FUSION_APPLY_BUTTON_ID = 1;
    public static final int CHIMERA_APPLY_BUTTON_ID = 2;
    public static final int AWAKEN_BUTTON_ID = 5;

    public static final int MAIN_INV_START = PLAYER_INV_START;
    public static final int MAIN_INV_END = MAIN_INV_START + 27;

    public static final int HOTBAR_START = MAIN_INV_END;
    public static final int HOTBAR_END = HOTBAR_START + 9;

    public static final int MENDER_CLAW_TYPE_BASE_ID = 200;
    public static final int SPEEDSTER_APPLY_COLOR_BASE_ID = 300;

    private final java.util.List<Slot> playerInventorySlots = new java.util.ArrayList<>();

    public GeneticSequencerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }

    public GeneticSequencerMenu(int containerId, Inventory playerInventory) {
        super(EOPMenus.GENETIC_SEQUENCER_MENU.get(), containerId);

        this.chipSlot = new ChipsSlot(chipContainer, 0, 105, 139);
        this.addSlot(this.chipSlot);

        this.fusionSlot = new FusionCatalystSlot(fusionContainer, 0, 105, 139);
        this.addSlot(this.fusionSlot);

        this.chimeraSlot = new ChimeraCoreSlot(chimeraContainer, 0, 105, 139);
        this.addSlot(this.chimeraSlot);

        this.evolutionSlot = new EvolutionItemSlot(evolutionContainer, 0, 105, 139);
        this.addSlot(this.evolutionSlot);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                ToggleableSlot slot = new ToggleableSlot(
                        playerInventory,
                        col + row * 9 + 9,
                        INVENTORY_X + col * 18,
                        INVENTORY_Y + row * 18
                );

                this.playerInventorySlots.add(slot);
                this.addSlot(slot);
            }
        }

        for (int col = 0; col < 9; col++) {
            ToggleableSlot slot = new ToggleableSlot(
                    playerInventory,
                    col,
                    INVENTORY_X + col * 18,
                    HOTBAR_Y
            );

            this.playerInventorySlots.add(slot);
            this.addSlot(slot);
        }
    }

    public void setPlayerInventoryVisible(boolean visible) {
        for (Slot slot : this.playerInventorySlots) {
            if (slot instanceof ToggleableSlot toggleableSlot) {
                toggleableSlot.setVisible(visible);
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (id >= SPEEDSTER_APPLY_COLOR_BASE_ID && id < SPEEDSTER_APPLY_COLOR_BASE_ID + 0x2000000) {
            int data = id - SPEEDSTER_APPLY_COLOR_BASE_ID;

            int target = data / 0x1000000;
            int color = data % 0x1000000;

            if (target == 0) {
                EOPPalladiumProperties.setSpeedsterPrimaryLightningColor(serverPlayer, color);
            } else {
                EOPPalladiumProperties.setSpeedsterSecondaryLightningColor(serverPlayer, color);
            }

            return true;
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

        if (id == AWAKEN_BUTTON_ID) {
            return applyAwakening(serverPlayer);
        }

        return false;
    }

    private boolean applyAwakening(ServerPlayer serverPlayer) {
        ItemStack stack = this.evolutionSlot.getItem();

        if (stack.isEmpty()) {
            return false;
        }

        EOPPowerRegistry.EOPPower ownedPower = null;
        int powerAmount = 0;

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            if (EOPPowerGrantHandler.hasPower(serverPlayer, power.key())) {
                ownedPower = power;
                powerAmount++;
            }
        }

        if (ownedPower == null) {
            return false;
        }

        if (powerAmount != 1) {
            AwakeningSequencerPanel.showError("Requirements\nnot met!");
            serverPlayer.playNotifySound(
                    SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),
                    SoundSource.PLAYERS,
                    1.0F,
                    0.5F
            );
            return false;
        }

        if (!ownedPower.hasAwakening()) {
            return false;
        }

        int level = EOPPalladiumProperties.getLevel(serverPlayer, ownedPower.key());

        boolean dnaClean = !hasAbility(serverPlayer, "base", "DNA.Corrupted");

        boolean allAbilitiesUnlocked = hasAbility(
                serverPlayer,
                ownedPower.key(),
                "All.Abilities.Unlocked"
        );

        if (level < 25 || !dnaClean || !allAbilitiesUnlocked || powerAmount != 1) {
            AwakeningSequencerPanel.showError("Requirements\nnot met!");
            serverPlayer.playNotifySound(
                    SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),
                    SoundSource.PLAYERS,
                    1.0F,
                    0.5F
            );
            return false;
        }

        Item requiredItem = EOPPowerRegistry.getEvolutionItem(ownedPower.key());

        if (requiredItem == null || !stack.is(requiredItem)) {
            serverPlayer.playNotifySound(
                    SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),
                    SoundSource.PLAYERS,
                    1.0F,
                    0.5F
            );
            EOPNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new ShowAwakeningErrorPacket("Wrong item!")
            );

            return true;
        }

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            if (power.key().equals(ownedPower.key())) {
                continue;
            }

            if (EOPPowerGrantHandler.hasPower(serverPlayer, power.key())) {
                EOPPowerGrantHandler.removePower(serverPlayer, power.key());
            }
        }

        serverPlayer.getServer().getCommands().performPrefixedCommand(
                serverPlayer.createCommandSourceStack()
                        .withSuppressedOutput()
                        .withPermission(2),
                "function eop:items/awakening/" + ownedPower.key() + "_awaken"
        );

        String capitalizedKey =
                Character.toUpperCase(ownedPower.key().charAt(0))
                        + ownedPower.key().substring(1);

        serverPlayer.addTag("EOP." + capitalizedKey + ".Awakened");
        serverPlayer.addTag("EOP.Awakened");

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
            this.evolutionSlot.setChanged();
        }

        serverPlayer.closeContainer();
        return true;
    }

    private static boolean hasAbility(LivingEntity entity, String powerKey, String abilityKey) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, powerKey),
                abilityKey
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }

    private void setSpeedsterColorChannel(ServerPlayer player, int target, int channel, int value) {
        int color = target == 0
                ? EOPPalladiumProperties.getSpeedsterPrimaryLightningColor(player)
                : EOPPalladiumProperties.getSpeedsterSecondaryLightningColor(player);

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

        int newColor = (r << 16) | (g << 8) | b;

        if (target == 0) {
            EOPPalladiumProperties.setSpeedsterPrimaryLightningColor(player, newColor);
        } else {
            EOPPalladiumProperties.setSpeedsterSecondaryLightningColor(player, newColor);
        }
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

        EOPPowerGrantHandler.GrantResult result =
                EOPPowerGrantHandler.tryGrantPower(
                        serverPlayer,
                        powerKey,
                        successRate
                );

        boolean shouldConsume = switch (result) {
            case SUCCESS, FAILED_CHANCE -> true;
            default -> false;
        };

        if (shouldConsume && !serverPlayer.getAbilities().instabuild) {
            chipStack.shrink(1);
            this.chipSlot.setChanged();
        }

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

            if (index == CHIP_SLOT_INDEX
                    || index == FUSION_SLOT_INDEX
                    || index == CHIMERA_SLOT_INDEX
                    || index == EVOLUTION_SLOT_INDEX) {
                if (!this.moveItemStackTo(clickedStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean movedToSpecialSlot = false;

                if (this.activePanel == PANEL_CHIP && clickedStack.is(CHIP_TAG)) {
                    movedToSpecialSlot = this.moveItemStackTo(
                            clickedStack,
                            CHIP_SLOT_INDEX,
                            CHIP_SLOT_INDEX + 1,
                            false
                    );
                } else if (this.activePanel == PANEL_FUSION && this.fusionSlot.mayPlace(clickedStack)) {
                    movedToSpecialSlot = this.moveItemStackTo(
                            clickedStack,
                            FUSION_SLOT_INDEX,
                            FUSION_SLOT_INDEX + 1,
                            false
                    );
                } else if (this.activePanel == PANEL_CHIMERA && this.chimeraSlot.mayPlace(clickedStack)) {
                    movedToSpecialSlot = this.moveItemStackTo(
                            clickedStack,
                            CHIMERA_SLOT_INDEX,
                            CHIMERA_SLOT_INDEX + 1,
                            false
                    );
                } else if (this.activePanel == PANEL_AWAKENING && this.evolutionSlot.mayPlace(clickedStack)) {
                    movedToSpecialSlot = this.moveItemStackTo(
                            clickedStack,
                            EVOLUTION_SLOT_INDEX,
                            EVOLUTION_SLOT_INDEX + 1,
                            false
                    );
                }

                if (!movedToSpecialSlot) {
                    if (index >= MAIN_INV_START && index < MAIN_INV_END) {
                        if (!this.moveItemStackTo(clickedStack, HOTBAR_START, HOTBAR_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                        if (!this.moveItemStackTo(clickedStack, MAIN_INV_START, MAIN_INV_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
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
            this.clearContainer(player, this.evolutionContainer);
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
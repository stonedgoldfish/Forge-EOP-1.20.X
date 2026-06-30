package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.stonedgoldfish.eopmod.power.ability.SavedRestrictSlotsAbility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Shadow
    @Final
    public Player player;

    @Shadow
    public int selected;

    @Inject(
            method = "getSelected()Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eop$hideSelectedItemWhenMainhandRestricted(
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (SavedRestrictSlotsAbility.shouldBypassHandRestriction()
                || this.player.isDeadOrDying()
                || this.player.getHealth() <= 0.0F) {
            return;
        }

        if (SavedRestrictSlotsAbility.isEquipmentSlotRestricted(this.player, EquipmentSlot.MAINHAND)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(
            method = "getItem(I)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eop$hideRestrictedHandInventoryItems(
            int slot,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (SavedRestrictSlotsAbility.shouldBypassHandRestriction()
                || this.player.isDeadOrDying()
                || this.player.getHealth() <= 0.0F) {
            return;
        }

        if (slot == this.selected
                && SavedRestrictSlotsAbility.isEquipmentSlotRestricted(this.player, EquipmentSlot.MAINHAND)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }

        if (slot == 40
                && SavedRestrictSlotsAbility.isEquipmentSlotRestricted(this.player, EquipmentSlot.OFFHAND)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
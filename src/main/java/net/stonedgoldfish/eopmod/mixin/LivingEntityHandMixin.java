package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.stonedgoldfish.eopmod.power.ability.SavedRestrictSlotsAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityHandMixin {

    @Inject(method = "getMainHandItem", at = @At("RETURN"), cancellable = true)
    private void eop$hideRestrictedMainHand(CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (SavedRestrictSlotsAbility.isEquipmentSlotRestricted(entity, EquipmentSlot.MAINHAND)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "getOffhandItem", at = @At("RETURN"), cancellable = true)
    private void eop$hideRestrictedOffhand(CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (SavedRestrictSlotsAbility.isEquipmentSlotRestricted(entity, EquipmentSlot.OFFHAND)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "getItemInHand", at = @At("RETURN"), cancellable = true)
    private void eop$hideRestrictedHand(
            InteractionHand hand,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (hand == InteractionHand.MAIN_HAND
                && SavedRestrictSlotsAbility.isEquipmentSlotRestricted(entity, EquipmentSlot.MAINHAND)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }

        if (hand == InteractionHand.OFF_HAND
                && SavedRestrictSlotsAbility.isEquipmentSlotRestricted(entity, EquipmentSlot.OFFHAND)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
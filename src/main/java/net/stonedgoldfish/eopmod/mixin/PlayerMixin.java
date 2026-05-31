package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void eop$customFlightSprintDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        Player player = (Player) (Object) this;

        if (CustomFlightAbility.isSprintFlying(player)) {

            cir.setReturnValue(EntityDimensions.scalable(0.6F, 0.6F));
        }
    }
}
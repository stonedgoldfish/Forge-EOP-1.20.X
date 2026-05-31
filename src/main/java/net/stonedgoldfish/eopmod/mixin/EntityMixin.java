package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
import net.stonedgoldfish.eopmod.power.ability.SilentStepsAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(
            method = "spawnSprintParticle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eopmod$cancelSprintParticles(CallbackInfo ci) {
        if ((Object) this instanceof Player player &&
                SilentStepsAbility.hasSilentSteps(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "makeBoundingBox", at = @At("HEAD"), cancellable = true)
    private void eop$customSprintFlightBoundingBox(CallbackInfoReturnable<AABB> cir) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof Player player)) {
            return;
        }

        if (!CustomFlightAbility.isSprintFlying(player)) {
            return;
        }

        double width = 0.6D;
        double height = 0.6D;
        double halfWidth = width / 2.0D;

        double bottomY = player.getY() + 1.2D;

        cir.setReturnValue(new AABB(
                player.getX() - halfWidth,
                bottomY,
                player.getZ() - halfWidth,
                player.getX() + halfWidth,
                bottomY + height,
                player.getZ() + halfWidth
        ));
    }
}
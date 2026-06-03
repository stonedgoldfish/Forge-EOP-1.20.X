package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.stonedgoldfish.eopmod.power.ability.NoCollisionAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public class ProjectileMixin {

    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void eop$projectilesIgnorePhasingEntities(Entity target, CallbackInfoReturnable<Boolean> cir) {

        Projectile projectile = (Projectile) (Object) this;

        if (NoCollisionAbility.isProjectilePhasing(target)
                && !NoCollisionAbility.isProjectileBlacklisted(target, projectile)) {
            cir.setReturnValue(false);
        }
    }
}
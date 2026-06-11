package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.stonedgoldfish.eopmod.power.ability.AutoDodgeAbility;
import net.stonedgoldfish.eopmod.power.ability.NoCollisionAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.DodgePacket;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public class ProjectileMixin {

    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void eop$projectilesIgnoreEntities(Entity target, CallbackInfoReturnable<Boolean> cir) {

        Projectile projectile = (Projectile) (Object) this;

        if (NoCollisionAbility.isProjectilePhasing(target)
                && !NoCollisionAbility.isProjectileBlacklisted(target, projectile)) {
            cir.setReturnValue(false);
            return;
        }

        if (target instanceof LivingEntity living
                && AutoDodgeAbility.canDodge(living)
                && !AutoDodgeAbility.isProjectileBlacklisted(living, projectile)) {

            if (living instanceof ServerPlayer player) {
                EOPNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new DodgePacket(getProjectileDodgeAnimation(living, projectile))
                );
            }

            cir.setReturnValue(false);
        }
    }
    private static EOPAnimationType getProjectileDodgeAnimation(
            LivingEntity target,
            Projectile projectile
    ) {
        double eyeLevelDifference =
                Math.abs(projectile.getY() - target.getEyeY());

        if (eyeLevelDifference <= 0.45D) {
            return EOPAnimationType.AUTO_DODGE_1;
        }

        Vec3 toProjectile = projectile.position()
                .subtract(target.position())
                .normalize();

        float yawRad = (float) Math.toRadians(target.getYRot());

        Vec3 right = new Vec3(
                Math.cos(yawRad),
                0.0D,
                Math.sin(yawRad)
        ).normalize();

        double sideDot = toProjectile.dot(right);

        if (sideDot > 0.0D) {
            return EOPAnimationType.AUTO_DODGE_3;
        }

        return EOPAnimationType.AUTO_DODGE_2;
    }
}
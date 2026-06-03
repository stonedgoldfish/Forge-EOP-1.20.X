package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.stonedgoldfish.eopmod.power.ability.LavaSwimmingAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void eop$lavaSwimLikeWater(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!LavaSwimmingAbility.hasLavaSwimming(entity)) {
            return;
        }

        if (!entity.isInLava()) {
            return;
        }

        if (!entity.isControlledByLocalInstance()) {
            return;
        }

        double oldY = entity.getY();

        float movementSpeed = 0.02F;
        float drag = entity.isSprinting() ? 0.9F : 0.8F;

        if (entity.isSprinting()) {
            entity.setSwimming(true);
            entity.setPose(net.minecraft.world.entity.Pose.SWIMMING);

            double swimSpeed = 0.18D;

            Vec3 look = entity.getLookAngle().normalize();

            entity.setDeltaMovement(
                    look.x * swimSpeed,
                    look.y * swimSpeed,
                    look.z * swimSpeed
            );
        } else {
            entity.setSwimming(false);
            entity.setPose(net.minecraft.world.entity.Pose.STANDING);

            entity.moveRelative(movementSpeed, travelVector);
        }

        entity.move(MoverType.SELF, entity.getDeltaMovement());

        Vec3 motion = entity.getDeltaMovement();

        entity.setDeltaMovement(
                motion.x * drag,
                motion.y * 0.8D,
                motion.z * drag
        );

        if (!entity.isSprinting() && !entity.isNoGravity()) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -0.02D, 0.0D));
        }

        entity.fallDistance = 0.0F;

        entity.calculateEntityAnimation(entity.isSwimming());

        ci.cancel();
    }
}
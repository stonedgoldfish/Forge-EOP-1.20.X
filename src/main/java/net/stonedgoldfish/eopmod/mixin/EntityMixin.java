package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
import net.stonedgoldfish.eopmod.power.ability.NoCollisionAbility;
import net.stonedgoldfish.eopmod.power.ability.SilentStepsAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.stonedgoldfish.eopmod.power.ability.IntangibilityAbility;
import net.stonedgoldfish.eopmod.power.ability.EOPAbilities;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class EntityMixin {

    @Shadow
    public Level level;

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

    @Inject(method = "canCollideWith", at = @At("HEAD"), cancellable = true)
    private void eop$disableEntityCollision(Entity other, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;

        if (NoCollisionAbility.isEntityPhasing(self)
                || NoCollisionAbility.isEntityPhasing(other)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "moveTowardsClosestSpace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"
            ),
            cancellable = true
    )
    private void eop$cancelPushOutOfBlocks(double x, double y, double z, CallbackInfo ci) {

        if (!((Object) this instanceof LivingEntity living)) {
            return;
        }

        for (AbilityInstance entry : AbilityUtil.getEnabledInstances(
                living,
                EOPAbilities.INTANGIBILITY.get()
        )) {
            if (IntangibilityAbility.canGoThrough(
                    entry,
                    this.level.getBlockState(BlockPos.containing(x, y, z))
            )) {
                ci.cancel();
                return;
            }
        }
    }
}
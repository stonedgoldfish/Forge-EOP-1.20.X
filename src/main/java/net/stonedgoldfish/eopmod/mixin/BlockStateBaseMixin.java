package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stonedgoldfish.eopmod.power.ability.EOPAbilities;
import net.stonedgoldfish.eopmod.power.ability.IntangibilityAbility;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void eop$getCollisionShape(
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        VoxelShape blockShape = cir.getReturnValue();

        if (blockShape.isEmpty()) {
            return;
        }

        if (!(context instanceof EntityCollisionContext ctx)) {
            return;
        }

        if (!(ctx.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        boolean isAbove = eop$isAbove(entity, blockShape, pos, false);

        for (AbilityInstance entry : AbilityUtil.getEnabledEntries(
                entity,
                EOPAbilities.INTANGIBILITY.get()
        )) {
            boolean horizontalPhase = !isAbove;

            boolean verticalPhase =
                    entry.getProperty(IntangibilityAbility.VERTICAL)
                            && eop$canVerticalPhase(entity, pos);

            if (horizontalPhase || verticalPhase) {
                if (IntangibilityAbility.canGoThrough(entry, level.getBlockState(pos))) {
                    cir.setReturnValue(Shapes.empty());
                    return;
                }
            }
        }
    }

    @Unique
    private static final Map<UUID, Double> EOP_VERTICAL_PHASE_START_Y = new HashMap<>();

    @Unique
    private static final Map<UUID, Boolean> EOP_VERTICAL_PHASE_LOCKED = new HashMap<>();

    @Unique
    private boolean eop$canVerticalPhase(LivingEntity entity, BlockPos pos) {
        if (!entity.isShiftKeyDown()) {
            EOP_VERTICAL_PHASE_START_Y.remove(entity.getUUID());
            EOP_VERTICAL_PHASE_LOCKED.remove(entity.getUUID());
            return false;
        }

        if (EOP_VERTICAL_PHASE_LOCKED.getOrDefault(entity.getUUID(), false)) {
            return false;
        }

        double startY = EOP_VERTICAL_PHASE_START_Y.computeIfAbsent(
                entity.getUUID(),
                uuid -> entity.getY()
        );

        if (startY - entity.getY() >= 1.05D) {
            EOP_VERTICAL_PHASE_LOCKED.put(entity.getUUID(), true);
            return false;
        }

        int entityFeetY = BlockPos.containing(entity.position()).getY();

        return pos.getY() >= entityFeetY - 1;
    }

    @Unique
    private boolean eop$isAbove(Entity entity, VoxelShape shape, BlockPos pos, boolean defaultValue) {
        return entity.getY() > (double) pos.getY()
                + shape.max(Direction.Axis.Y)
                - (entity.onGround() ? 8.05 / 16.0 : 0.0015);
    }

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void eop$preventCollisionWhenPhasing(
            Level level,
            BlockPos pos,
            Entity entity,
            CallbackInfo ci
    ) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        for (AbilityInstance entry : AbilityUtil.getEnabledEntries(
                living,
                EOPAbilities.INTANGIBILITY.get()
        )) {
            if (IntangibilityAbility.canGoThrough(entry, level.getBlockState(pos))) {
                ci.cancel();
                return;
            }
        }
    }
}
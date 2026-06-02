package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

public class RaycastTeleportAbility extends Ability {

    public static final PalladiumProperty<Float> RANGE =
            new FloatProperty("range")
                    .configurable("Maximum teleport distance.");

    public static final PalladiumProperty<Boolean> RANDOM_TELEPORT =
            new BooleanProperty("random_teleport")
                    .configurable("If true, ignores raycast and teleports to random coordinates nearby.");

    public static final PalladiumProperty<Float> RANDOM_RADIUS =
            new FloatProperty("random_radius")
                    .configurable("Radius used for random teleport.");

    public RaycastTeleportAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENDER_PEARL));
        this.withProperty(RANGE, 20.0F);
        this.withProperty(RANDOM_TELEPORT, false);
        this.withProperty(RANDOM_RADIUS, 8.0F);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (entry.getProperty(RANDOM_TELEPORT)) {
            Vec3 targetPos = getRandomTeleportPosition(entity, level, entry.getProperty(RANDOM_RADIUS));

            entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            entity.fallDistance = 0.0F;

            return;
        }

        float range = entry.getProperty(RANGE);

        Vec3 start = entity.getEyePosition();
        Vec3 look = entity.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));

        BlockHitResult hit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos hitBlockPos = hit.getBlockPos();

        var state = level.getBlockState(hitBlockPos);

        if (state.is(net.minecraft.world.level.block.Blocks.AIR)
                || state.is(net.minecraft.world.level.block.Blocks.CAVE_AIR)
                || state.is(net.minecraft.world.level.block.Blocks.VOID_AIR)) {
            return;
        }

        Vec3 targetPos = findSafeTeleportPosition(entity, level, hit);

        if (targetPos == null) {
            Direction face = hit.getDirection();

            if (face == Direction.DOWN) {
                // Hit the underside of a ceiling: teleport slightly below it
                targetPos = hit.getLocation().add(0.0D, -entity.getBbHeight() - 0.15D, 0.0D);
            } else {
                targetPos = hit.getLocation().add(
                        face.getStepX() * 0.75D,
                        face.getStepY() * 0.75D,
                        face.getStepZ() * 0.75D
                );
            }
        }

        entity.teleportTo(
                targetPos.x,
                targetPos.y,
                targetPos.z
        );

        entity.fallDistance = 0.0F;
    }

    private static Vec3 getRandomTeleportPosition(LivingEntity entity, ServerLevel level, float radius) {
        radius = Math.max(0.0F, radius);

        java.util.Random random = new java.util.Random();

        for (int tries = 0; tries < 32; tries++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = random.nextDouble() * radius;

            double x = entity.getX() + Math.cos(angle) * distance;
            double z = entity.getZ() + Math.sin(angle) * distance;
            double y = entity.getY() + ((random.nextDouble() * 2.0D) - 1.0D) * radius;

            BlockPos feetPos = BlockPos.containing(x, y, z);
            BlockPos headPos = feetPos.above();

            if (!level.getBlockState(feetPos).isAir()) {
                continue;
            }

            if (!level.getBlockState(headPos).isAir()) {
                continue;
            }

            Vec3 candidate = new Vec3(
                    feetPos.getX() + 0.5D,
                    feetPos.getY(),
                    feetPos.getZ() + 0.5D
            );

            if (isSafeTeleportPosition(entity, level, candidate)) {
                return candidate;
            }
        }

        return entity.position();
    }

    private static Vec3 findSafeTeleportPosition(
            LivingEntity entity,
            ServerLevel level,
            BlockHitResult hit
    ) {
        Direction face = hit.getDirection();

        Vec3 base;

        if (face == Direction.DOWN) {
            base = hit.getLocation().add(0.0D, -entity.getBbHeight() - 0.15D, 0.0D);
        } else {
            base = hit.getLocation().add(
                    face.getStepX() * 0.75D,
                    face.getStepY() * 0.75D,
                    face.getStepZ() * 0.75D
            );
        }

        BlockPos baseBlock = BlockPos.containing(base);

        for (int radius = 0; radius <= 3; radius++) {
            for (BlockPos pos : BlockPos.betweenClosed(
                    baseBlock.offset(-radius, -radius, -radius),
                    baseBlock.offset(radius, radius, radius)
            )) {
                Vec3 candidate = new Vec3(
                        pos.getX() + 0.5D,
                        pos.getY(),
                        pos.getZ() + 0.5D
                );

                if (isSafeTeleportPosition(entity, level, candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private static boolean isSafeTeleportPosition(
            LivingEntity entity,
            ServerLevel level,
            Vec3 pos
    ) {
        AABB box = entity.getDimensions(entity.getPose())
                .makeBoundingBox(pos.x, pos.y, pos.z);

        if (!level.noCollision(entity, box)) {
            return false;
        }

        BlockPos feet = BlockPos.containing(pos.x, pos.y, pos.z);
        BlockPos below = feet.below();

        return !level.getBlockState(below).isAir();
    }

    @Override
    public String getDocumentationDescription() {
        return "Teleports the entity to the block or point they are looking at.";
    }
}
package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.*;

public class WallClimbAbility extends Ability {

    public static final PalladiumProperty<Float> CLIMB_SPEED =
            new FloatProperty("climb_speed")
                    .configurable("Upward speed while climbing a wall.");

    public static final PalladiumProperty<Float> WALL_JUMP_POWER =
            new FloatProperty("wall_jump_power")
                    .configurable("Vertical power applied when wall jumping.");

    public WallClimbAbility() {
        this.withProperty(ICON, new ItemIcon(Items.LADDER));
        this.withProperty(CLIMB_SPEED, 0.25F);
        this.withProperty(WALL_JUMP_POWER, 0.35F);
    }

    private static final Map<UUID, Float> WALL_CLIMBERS = new HashMap<>();
    private static final Map<UUID, Integer> WALL_JUMP_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Integer> LEDGE_ASSIST_TICKS = new HashMap<>();

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        if (!(entity instanceof Player)) {
            return;
        }

        if (!isTouchingClimbableWall(entity)) {
            WALL_CLIMBERS.remove(entity.getUUID());

            Integer ledgeTicks = LEDGE_ASSIST_TICKS.get(entity.getUUID());

            if (ledgeTicks != null && ledgeTicks > 0 && canClimbLedge(entity)) {
                Vec3 forward = getFlatForward(entity);

                entity.setDeltaMovement(
                        forward.x * 0.14D,
                        0.28D,
                        forward.z * 0.14D
                );

                LEDGE_ASSIST_TICKS.put(entity.getUUID(), ledgeTicks - 1);

                entity.fallDistance = 0.0F;
                entity.hurtMarked = true;
            } else {
                LEDGE_ASSIST_TICKS.remove(entity.getUUID());
            }

            return;
        }

        LEDGE_ASSIST_TICKS.put(entity.getUUID(), 5);

        if (entity instanceof Player player) {
            WALL_CLIMBERS.put(
                    player.getUUID(),
                    entry.getProperty(WALL_JUMP_POWER)
            );
        }

        LEDGE_ASSIST_TICKS.put(entity.getUUID(), 5);

        Integer cooldown = WALL_JUMP_COOLDOWN.get(entity.getUUID());

        if (cooldown != null) {
            if (cooldown <= 0) {
                WALL_JUMP_COOLDOWN.remove(entity.getUUID());
            } else {
                WALL_JUMP_COOLDOWN.put(entity.getUUID(), cooldown - 1);
                return;
            }
        }

        if (entity.isShiftKeyDown()) {
            entity.setDeltaMovement(
                    0.0D,
                    0.08D,
                    0.0D
            );

            entity.fallDistance = 0.0F;
            entity.hurtMarked = true;
            return;
        }

        double climbSpeed = entry.getProperty(CLIMB_SPEED);

        Vec3 motion = entity.getDeltaMovement();

        entity.setDeltaMovement(
                motion.x,
                climbSpeed,
                motion.z
        );

        entity.fallDistance = 0.0F;
        entity.hurtMarked = true;
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        WALL_CLIMBERS.remove(entity.getUUID());
        LEDGE_ASSIST_TICKS.remove(entity.getUUID());
        WALL_JUMP_COOLDOWN.remove(entity.getUUID());
    }

    private static boolean canClimbLedge(LivingEntity entity) {
        Level level = entity.level();

        double checkDistance = 0.45D;
        Vec3 forward = getFlatForward(entity);

        BlockPos frontFeet = BlockPos.containing(
                entity.getX() + forward.x * checkDistance,
                entity.getY(),
                entity.getZ() + forward.z * checkDistance
        );

        BlockPos frontHead = frontFeet.above();
        BlockPos frontAboveHead = frontHead.above();

        return isSolid(level, frontFeet)
                && !isSolid(level, frontHead)
                && !isSolid(level, frontAboveHead);
    }

    private static Vec3 getFlatForward(LivingEntity entity) {
        float yawRad = entity.getYRot() * ((float) Math.PI / 180.0F);

        return new Vec3(
                -Math.sin(yawRad),
                0.0D,
                Math.cos(yawRad)
        ).normalize();
    }

    public static void startWallJumpCooldown(Player player) {
        WALL_JUMP_COOLDOWN.put(player.getUUID(), 2);
        WALL_CLIMBERS.remove(player.getUUID());
    }

    public static boolean isWallClimbing(Player player) {
        return WALL_CLIMBERS.containsKey(player.getUUID());
    }

    public static float getWallJumpPower(Player player) {
        return WALL_CLIMBERS.getOrDefault(player.getUUID(), 0.35F);
    }

    private static boolean isTouchingClimbableWall(LivingEntity entity) {
        Level level = entity.level();

        double checkDistance = 0.35D;

        for (Direction direction : Direction.Plane.HORIZONTAL) {

            Vec3 checkPos = entity.position().add(
                    direction.getStepX() * checkDistance,
                    0.0D,
                    direction.getStepZ() * checkDistance
            );

            BlockPos feetWall = BlockPos.containing(checkPos);
            BlockPos headWall = feetWall.above();

            if (isSolid(level, feetWall) && isSolid(level, headWall)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSolid(Level level, BlockPos pos) {
        return !level.getBlockState(pos).isAir()
                && level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
    }

    @Override
    public String getDocumentationDescription() {
        return "Moves the player upward when touching a wall at least two blocks tall.";
    }
}
package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
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
import net.threetag.palladium.util.property.PalladiumProperties;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WallClimbAbility extends Ability {

    public static final PalladiumProperty<Float> CLIMB_SPEED =
            new FloatProperty("climb_speed")
                    .configurable("Vertical climb speed.");

    public static final PalladiumProperty<Float> WALL_JUMP_POWER =
            new FloatProperty("wall_jump_power")
                    .configurable("Power applied when wall jumping.");

    public static final PalladiumProperty<Float> SIDEWAYS_STRENGTH =
            new FloatProperty("sideways_strength")
                    .configurable("How much sideways movement blends with climbing.");

    public static final PalladiumProperty<Float> SIDEWAYS_SPEED =
            new FloatProperty("sideways_speed")
                    .configurable("How fast the player moves sideways while climbing.");

    private static final Map<UUID, Float> WALL_CLIMBERS = new HashMap<>();
    private static final Map<UUID, Integer> WALL_JUMP_COOLDOWN = new HashMap<>();

    public WallClimbAbility() {
        this.withProperty(ICON, new ItemIcon(Items.LADDER));
        this.withProperty(CLIMB_SPEED, 0.08F);
        this.withProperty(WALL_JUMP_POWER, 1.0F);
        this.withProperty(SIDEWAYS_STRENGTH, 0.35F);
        this.withProperty(SIDEWAYS_SPEED, 0.13F);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        if (entity.onClimbable()) {
            clearClimbing(entity);
            return;
        }

        UUID uuid = entity.getUUID();

        Integer cooldown = WALL_JUMP_COOLDOWN.get(uuid);

        if (cooldown != null) {
            if (cooldown <= 0) {
                WALL_JUMP_COOLDOWN.remove(uuid);
            } else {
                WALL_JUMP_COOLDOWN.put(uuid, cooldown - 1);
                clearClimbing(entity);
                return;
            }
        }

        WallCheckResult wall = getWallCheck(entity);

        boolean isClimbing = entity.getPersistentData().getBoolean("surfaceClimbing");
        boolean midAir = !entity.onGround() && entity.fallDistance > 0.0F;

        if (!isClimbing) {
            if (!wall.hasTwoVerticalBlocks()
                    && !(midAir && wall.hasOneVerticalBlock())) {
                clearClimbing(entity);
                return;
            }

            entity.getPersistentData().putBoolean("surfaceClimbing", true);
        } else {
            if (!wall.hasOneVerticalBlock()) {
                clearClimbing(entity);

                Vec3 motion = entity.getDeltaMovement();

                entity.setDeltaMovement(
                        motion.x,
                        0.0D,
                        motion.z
                );

                entity.fallDistance = 0.0F;
                entity.hurtMarked = true;
                syncMotion(entity);
                return;
            }
        }

        WALL_CLIMBERS.put(
                player.getUUID(),
                entry.getProperty(WALL_JUMP_POWER)
        );

        Vec3 motion = entity.getDeltaMovement();

        if (entity.isShiftKeyDown()) {
            entity.setDeltaMovement(
                    motion.x * 0.2D,
                    -0.02D,
                    motion.z * 0.2D
            );

            entity.fallDistance = 0.0F;
            entity.hurtMarked = true;
            syncMotion(entity);
            return;
        }

        double climbSpeed = entry.getProperty(CLIMB_SPEED);
        double sidewaysStrength = entry.getProperty(SIDEWAYS_STRENGTH);
        double sidewaysSpeed = entry.getProperty(SIDEWAYS_SPEED);

        boolean pressingForward = PalladiumProperties.FORWARD_KEY_DOWN.get(entity);

        double verticalMotion = pressingForward
                ? climbSpeed * Math.max(0.0D, 1.0D - sidewaysStrength)
                : -climbSpeed * 0.45D;

        Vec3 sideways = Vec3.ZERO;

        if (pressingForward && wall.hasWallAtHead()) {
            Vec3 look = entity.getLookAngle().normalize();
            Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);

            if (flatLook.lengthSqr() > 0.001D) {
                sideways = flatLook.normalize()
                        .scale(sidewaysSpeed * sidewaysStrength);
            }
        }

        entity.setDeltaMovement(
                sideways.x,
                verticalMotion,
                sideways.z
        );

        entity.fallDistance = 0.0F;
        entity.hurtMarked = true;
        syncMotion(entity);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        clearClimbing(entity);
        WALL_JUMP_COOLDOWN.remove(entity.getUUID());
    }

    private static void clearClimbing(LivingEntity entity) {
        WALL_CLIMBERS.remove(entity.getUUID());
        entity.getPersistentData().remove("surfaceClimbing");
    }

    public static void startWallJumpCooldown(Player player) {
        WALL_JUMP_COOLDOWN.put(player.getUUID(), 6);
        WALL_CLIMBERS.remove(player.getUUID());
        player.getPersistentData().remove("surfaceClimbing");
    }

    public static boolean isWallClimbing(Player player) {
        return WALL_CLIMBERS.containsKey(player.getUUID());
    }

    public static float getWallJumpPower(Player player) {
        return WALL_CLIMBERS.getOrDefault(player.getUUID(), 1.0F);
    }

    private static WallCheckResult getWallCheck(LivingEntity entity) {
        Level level = entity.level();

        var box = entity.getBoundingBox();
        double inflate = 0.05D;

        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.floor(box.maxY);
        int eyeY = (int) Math.floor(entity.getEyeY());

        double[][] checks = new double[][]{
                {box.maxX + inflate, entity.getZ()},
                {box.minX - inflate, entity.getZ()},
                {entity.getX(), box.maxZ + inflate},
                {entity.getX(), box.minZ - inflate}
        };

        boolean hasOneVerticalBlock = false;
        boolean hasTwoVerticalBlocks = false;
        boolean hasWallAtHead = false;

        for (double[] check : checks) {
            int consecutive = 0;

            for (int y = minY; y <= maxY; y++) {
                BlockPos pos = BlockPos.containing(
                        check[0],
                        y,
                        check[1]
                );

                if (isSolid(level, pos)) {
                    consecutive++;

                    if (consecutive >= 1) {
                        hasOneVerticalBlock = true;
                    }

                    if (consecutive >= 2) {
                        hasTwoVerticalBlocks = true;
                    }
                } else {
                    consecutive = 0;
                }
            }

            BlockPos headPos = BlockPos.containing(
                    check[0],
                    eyeY,
                    check[1]
            );

            if (isSolid(level, headPos)) {
                hasWallAtHead = true;
            }
        }

        return new WallCheckResult(
                hasOneVerticalBlock,
                hasTwoVerticalBlocks,
                hasWallAtHead
        );
    }

    private static boolean isSolid(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);

        return !state.isAir()
                && !state.getCollisionShape(level, pos).isEmpty();
    }

    private static void syncMotion(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(entity));
        }
    }

    private record WallCheckResult(
            boolean hasOneVerticalBlock,
            boolean hasTwoVerticalBlocks,
            boolean hasWallAtHead
    ) {}

    @Override
    public String getDocumentationDescription() {
        return "Allows the player to climb walls using bounding-box based surface detection.";
    }
}
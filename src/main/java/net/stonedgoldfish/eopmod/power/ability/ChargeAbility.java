package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.stonedgoldfish.eopmod.power.condition.EOPActivationHandler;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.*;

public class ChargeAbility extends Ability {

    public static final PalladiumProperty<Float> ACCELERATION = new FloatProperty("acceleration").configurable("Speed gained every tick while charging");
    public static final PalladiumProperty<Float> MAX_SPEED = new FloatProperty("max_speed").configurable("Maximum charge speed");
    public static final PalladiumProperty<Boolean> STOP_MOTION_ON_END = new BooleanProperty("stop_motion_on_end").configurable("Sets motion to 0 when the charge ends");
    public static final PalladiumProperty<Boolean> DESTROY_BLOCKS = new BooleanProperty("destroy_blocks").configurable("If true, destroys blocks while charging");
    public static final PalladiumProperty<Float> DESTRUCTION_RADIUS = new FloatProperty("destruction_radius").configurable("Destruction radius in front of the caster");
    public static final PalladiumProperty<Float> DESTRUCTION_ANGLE = new FloatProperty("destruction_angle").configurable("Angle of the frontal destruction cone");
    public static final PalladiumProperty<Float> MAX_BLOCK_HARDNESS = new FloatProperty("max_block_hardness").configurable("Will ignore blocks above the defined hardness");
    public static final PalladiumProperty<Boolean> DROP_BLOCKS = new BooleanProperty("drop_blocks").configurable("If true, destroyed blocks drop items");
    public static final PalladiumProperty<Integer> MAX_DESTRUCTION_LAYERS = new IntegerProperty("max_destruction_layers").configurable("How many block layers in front can be destroyed");
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("64e76d3d-b28d-45e8-b38a-846b6eb3c802");
    private static final Map<UUID, Float> CURRENT_SPEED = new HashMap<>();
    private static final Set<UUID> NO_BOB_PLAYERS = new HashSet<>();
    private static final Map<UUID, Integer> DESTROYED_WALL_LAYERS = new HashMap<>();

    public ChargeAbility() {
        this.withProperty(ICON, new ItemIcon(Items.IRON_BOOTS));
        this.withProperty(ACCELERATION, 0.02F);
        this.withProperty(MAX_SPEED, 0.4F);
        this.withProperty(STOP_MOTION_ON_END, true);
        this.withProperty(DESTROY_BLOCKS, false);
        this.withProperty(DESTRUCTION_RADIUS, 2.0F);
        this.withProperty(DESTRUCTION_ANGLE, 70.0F);
        this.withProperty(MAX_BLOCK_HARDNESS, 10.0F);
        this.withProperty(DROP_BLOCKS, false);
        this.withProperty(MAX_DESTRUCTION_LAYERS, 1);
    }

    public static boolean isCharging(Player player) {
        return CURRENT_SPEED.containsKey(player.getUUID());
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        UUID uuid = entity.getUUID();

        if (entity instanceof Player player) {
            NO_BOB_PLAYERS.add(uuid);
            player.setSprinting(false);
        }

        applyStepHeight(entity);

        float acceleration = entry.getProperty(ACCELERATION);
        float maxSpeed = entry.getProperty(MAX_SPEED);

        Vec3 currentMotion = entity.getDeltaMovement();
        float currentHorizontalSpeed = (float) Math.sqrt(
                currentMotion.x * currentMotion.x
                        + currentMotion.z * currentMotion.z
        );

        float currentSpeed = CURRENT_SPEED.getOrDefault(uuid, currentHorizontalSpeed);
        currentSpeed = Math.min(maxSpeed, currentSpeed + acceleration);

        CURRENT_SPEED.put(uuid, currentSpeed);

        float yawRad = entity.getYRot() * ((float) Math.PI / 180.0F);

        Vec3 direction = new Vec3(
                -Math.sin(yawRad),
                0.0D,
                Math.cos(yawRad)
        ).normalize();

        entity.setDeltaMovement(
                direction.x * currentSpeed,
                currentMotion.y,
                direction.z * currentSpeed
        );
        destroyBlocksInFront(entity, entry, direction);
        syncMotion(entity);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        reset(entity, entry);
    }

    private static void destroyBlocksInFront(
            LivingEntity entity,
            AbilityInstance entry,
            Vec3 direction
    ) {
        if (!entry.getProperty(DESTROY_BLOCKS)) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (!EOPGameRules.isDestructionMode(level.getServer())) {
            return;
        }

        UUID uuid = entity.getUUID();

        int totalDestroyed = 0;
        Vec3 destroyedCenter = Vec3.ZERO;

        int maxLayers = Math.max(1, entry.getProperty(MAX_DESTRUCTION_LAYERS));
        int alreadyDestroyedLayers = DESTROYED_WALL_LAYERS.getOrDefault(uuid, 0);

        if (alreadyDestroyedLayers >= maxLayers) {
            return;
        }

        float radius = Math.max(0.0F, entry.getProperty(DESTRUCTION_RADIUS));
        float angle = entry.getProperty(DESTRUCTION_ANGLE);
        float maxHardness = entry.getProperty(MAX_BLOCK_HARDNESS);
        boolean dropBlocks = entry.getProperty(DROP_BLOCKS);

        BlockPos center = entity.blockPosition();
        int r = (int) Math.ceil(radius);

        Vec3 origin = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);

        Map<Integer, List<BlockPos>> blocksByLayer = new TreeMap<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -r, -r),
                center.offset(r, r, r)
        )) {
            if (isBeneathCaster(entity, pos, radius)) {
                continue;
            }

            if (!isInFrontCone(origin, direction, pos, angle, radius)) {
                continue;
            }

            int layer = getForwardLayer(origin, direction, pos);

            if (layer < 1) {
                continue;
            }

            BlockState state = level.getBlockState(pos);

            if (state.isAir()) {
                continue;
            }

            float hardness = state.getDestroySpeed(level, pos);

            if (hardness < 0.0F) {
                continue;
            }

            if (maxHardness >= 0.0F && hardness > maxHardness) {
                continue;
            }

            blocksByLayer
                    .computeIfAbsent(layer, key -> new ArrayList<>())
                    .add(pos.immutable());
        }

        for (Map.Entry<Integer, List<BlockPos>> layerEntry : blocksByLayer.entrySet()) {
            if (alreadyDestroyedLayers >= maxLayers) {
                break;
            }

            boolean destroyedAnyInThisLayer = false;

            for (BlockPos pos : layerEntry.getValue()) {
                BlockState state = level.getBlockState(pos);

                if (state.isAir()) {
                    continue;
                }

                level.destroyBlock(pos, dropBlocks, entity);
                destroyedAnyInThisLayer = true;

                totalDestroyed++;
                destroyedCenter = destroyedCenter.add(Vec3.atCenterOf(pos));
            }

            if (destroyedAnyInThisLayer) {
                alreadyDestroyedLayers++;

                if (alreadyDestroyedLayers >= maxLayers) {
                    EOPActivationHandler.forceEndAndStartCooldown(entity, entry);
                    break;
                }
            }
        }
        if (totalDestroyed > 0) {
            destroyedCenter = destroyedCenter.scale(1.0D / totalDestroyed);
            spawnChargeDestructionEffect(level, destroyedCenter);
        }
        DESTROYED_WALL_LAYERS.put(uuid, alreadyDestroyedLayers);
    }

    private static int getForwardLayer(Vec3 origin, Vec3 forward, BlockPos pos) {
        Vec3 toBlock = Vec3.atCenterOf(pos).subtract(origin);
        double forwardDistance = toBlock.dot(forward);

        return Math.max(1, (int) Math.ceil(forwardDistance));
    }

    private static void spawnChargeDestructionEffect(ServerLevel level, Vec3 center) {
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                center.x,
                center.y,
                center.z,
                3,
                0.4D,
                0.4D,
                0.4D,
                0.0D
        );

        level.sendParticles(
                ParticleTypes.POOF,
                center.x,
                center.y,
                center.z,
                20,
                0.8D,
                0.6D,
                0.8D,
                0.08D
        );

        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                0.7F,
                1.15F
        );
    }

    private static boolean isBeneathCaster(LivingEntity entity, BlockPos pos, float radius) {
        BlockPos feet = entity.blockPosition();

        if (pos.getY() >= feet.getY()) {
            return false;
        }

        double dx = (pos.getX() + 0.5D) - entity.getX();
        double dz = (pos.getZ() + 0.5D) - entity.getZ();

        return (dx * dx) + (dz * dz) <= radius * radius;
    }

    private static boolean isInFrontCone(Vec3 origin, Vec3 forward, BlockPos pos, float angle, float radius) {
        Vec3 toBlock = Vec3.atCenterOf(pos).subtract(origin);
        Vec3 flat = new Vec3(toBlock.x, 0.0D, toBlock.z);

        if (flat.lengthSqr() < 0.001D) {
            return true;
        }

        double distance = flat.length();

        if (distance > radius) {
            return false;
        }

        flat = flat.normalize();

        double halfAngle = Math.toRadians(angle / 2.0F);
        double threshold = Math.cos(halfAngle);

        return forward.dot(flat) >= threshold;
    }

    private static void applyStepHeight(LivingEntity entity) {
        var attribute = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());

        if (attribute == null) {
            return;
        }

        attribute.removeModifier(STEP_HEIGHT_UUID);

        attribute.addTransientModifier(
                new AttributeModifier(
                        STEP_HEIGHT_UUID,
                        "eop_charge_step_height",
                        1.0D,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }

    private static void removeStepHeight(LivingEntity entity) {
        var attribute = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());

        if (attribute != null) {
            attribute.removeModifier(STEP_HEIGHT_UUID);
        }
    }

    private static void reset(LivingEntity entity, AbilityInstance entry) {
        boolean wasCharging = CURRENT_SPEED.containsKey(entity.getUUID());

        CURRENT_SPEED.remove(entity.getUUID());
        removeStepHeight(entity);

        if (entity instanceof Player player) {
            NO_BOB_PLAYERS.remove(player.getUUID());
            player.setSprinting(false);
        }

        if (wasCharging && entry.getProperty(STOP_MOTION_ON_END)) {
            entity.setDeltaMovement(Vec3.ZERO);
            syncMotion(entity);
        }
        DESTROYED_WALL_LAYERS.remove(entity.getUUID());
    }

    public static boolean disablesCameraBobbing(Player player) {
        return NO_BOB_PLAYERS.contains(player.getUUID());
    }

    private static void syncMotion(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(entity));
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Forces the entity to walk forward horizontally, starting from current speed and gradually accelerating.";
    }
}
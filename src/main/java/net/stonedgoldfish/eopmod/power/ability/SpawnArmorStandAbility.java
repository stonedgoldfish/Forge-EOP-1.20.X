package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.stonedgoldfish.eopmod.event.EOPForgeEvents;
import net.stonedgoldfish.eopmod.util.EOPArmorStandSpawner;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringArrayProperty;
import net.threetag.palladium.util.property.StringProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnArmorStandAbility extends Ability {

    public static final PalladiumProperty<Float> RANGE =
            new FloatProperty("range").configurable("Maximum distance to spawn the armor stand.");

    public static final PalladiumProperty<Boolean> SPAWN_BEHIND =
            new BooleanProperty("spawn_behind")
                    .configurable("If true, uses range to spawn the armor stand behind the player, ignoring pitch.");

    public static final PalladiumProperty<Boolean> SPAWN_AT_PLAYER =
            new BooleanProperty("spawn_at_player").configurable("If true, ignores raycast and spawns at the player's location.");

    public static final PalladiumProperty<Boolean> FOLLOW_MOUSE =
            new BooleanProperty("follow_mouse").configurable("If true, the armor stand follows the player's mouse/raycast position and dies on lastTick.");

    public static final PalladiumProperty<Integer> LIFETIME =
            new IntegerProperty("lifetime").configurable("Lifetime of the spawned armor stand in ticks. 0 = infinite.");

    public static final PalladiumProperty<Float> AOE_DAMAGE =
            new FloatProperty("aoe_damage").configurable("Damage dealt by the armor stand AOE.");

    public static final PalladiumProperty<Float> AOE_RADIUS =
            new FloatProperty("aoe_radius").configurable("Radius of the armor stand AOE.");

    public static final PalladiumProperty<String> AOE_DAMAGE_TYPE =
            new StringProperty("aoe_damage_type").configurable("Damage type used by the armor stand AOE.");

    public static final PalladiumProperty<Boolean> ENABLE_DAMAGE =
            new BooleanProperty("enable_damage").configurable("Whether the armor stand deals damage.");

    public static final PalladiumProperty<Boolean> DAMAGE_ON_LAST_TICK =
            new BooleanProperty("damage_on_last_tick").configurable("If true, AOE damage only happens when the armor stand expires.");

    public static final PalladiumProperty<Float> KNOCKBACK_ON_LAST_TICK =
            new FloatProperty("knockback_on_last_tick").configurable("Knockback strength applied when the armor stand expires. 0 disables it.");

    public static final PalladiumProperty<Float> TARGET_COMMAND_RADIUS =
            new FloatProperty("target_command_radius").configurable("Radius used for target command detection.");

    public static final PalladiumProperty<Float> PULL_STRENGTH =
            new FloatProperty("pull_strength").configurable("Pull strength toward the armor stand. 0 disables it.");

    public static final PalladiumProperty<Boolean> INVERT_PULL =
            new BooleanProperty("invert_pull").configurable("If true, pushes entities away instead of pulling them in.");

    public static final PalladiumProperty<String> STAND_POWER =
            new StringProperty("stand_power").configurable("Palladium power given to the spawned armor stand. Empty = none.");

    public static final PalladiumProperty<String[]> STAND_FIRST_TICK_COMMANDS =
            new StringArrayProperty("stand_first_tick_commands").configurable("Commands executed once when the armor stand spawns.");

    public static final PalladiumProperty<String[]> STAND_COMMANDS =
            new StringArrayProperty("stand_commands").configurable("Commands executed every tick by the armor stand.");

    public static final PalladiumProperty<String[]> STAND_LAST_TICK_COMMANDS =
            new StringArrayProperty("stand_last_tick_commands").configurable("Commands executed once before the armor stand is removed.");

    public static final PalladiumProperty<String[]> TARGET_FIRST_TICK_COMMANDS =
            new StringArrayProperty("target_first_tick_commands").configurable("Commands executed once as each valid target when first detected.");

    public static final PalladiumProperty<String[]> TARGET_COMMANDS =
            new StringArrayProperty("target_commands").configurable("Commands executed every tick as each valid target.");

    public static final PalladiumProperty<String[]> TARGET_LAST_TICK_COMMANDS =
            new StringArrayProperty("target_last_tick_commands").configurable("Commands executed once as each valid target when they leave the AOE.");

    public static final PalladiumProperty<String> LOOPING_SOUND =
            new StringProperty("looping_sound")
                    .configurable("Sound played repeatedly from the armor stand. Empty = none.");

    public static final PalladiumProperty<Float> LOOPING_SOUND_VOLUME =
            new FloatProperty("looping_sound_volume")
                    .configurable("Volume of the looping armor stand sound.");

    public static final PalladiumProperty<Float> LOOPING_SOUND_PITCH =
            new FloatProperty("looping_sound_pitch")
                    .configurable("Pitch of the looping armor stand sound.");

    public static final PalladiumProperty<Boolean> DESTROY_BLOCKS =
            new BooleanProperty("destroy_blocks")
                    .configurable("If true, destroys blocks around the armor stand when destructionMode is enabled.");

    public static final PalladiumProperty<Float> DESTROY_BLOCK_RADIUS =
            new FloatProperty("destroy_block_radius")
                    .configurable("Radius around the armor stand where blocks are destroyed.");

    private static final Map<UUID, ArmorStand> FOLLOWING_STANDS = new HashMap<>();

    public SpawnArmorStandAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ARMOR_STAND));

        this.withProperty(RANGE, 5.0F);
        this.withProperty(SPAWN_BEHIND, false);
        this.withProperty(SPAWN_AT_PLAYER, false);
        this.withProperty(FOLLOW_MOUSE, false);
        this.withProperty(LIFETIME, 100);

        this.withProperty(AOE_DAMAGE, 0.0F);
        this.withProperty(AOE_RADIUS, 3.0F);
        this.withProperty(AOE_DAMAGE_TYPE, "minecraft:magic");
        this.withProperty(ENABLE_DAMAGE, true);
        this.withProperty(DAMAGE_ON_LAST_TICK, false);
        this.withProperty(KNOCKBACK_ON_LAST_TICK, 0.0F);

        this.withProperty(TARGET_COMMAND_RADIUS, 3.0F);

        this.withProperty(PULL_STRENGTH, 0.0F);
        this.withProperty(INVERT_PULL, false);

        this.withProperty(STAND_POWER, "");

        this.withProperty(STAND_FIRST_TICK_COMMANDS, new String[]{});
        this.withProperty(STAND_COMMANDS, new String[]{});
        this.withProperty(STAND_LAST_TICK_COMMANDS, new String[]{});

        this.withProperty(TARGET_FIRST_TICK_COMMANDS, new String[]{});
        this.withProperty(TARGET_COMMANDS, new String[]{});
        this.withProperty(TARGET_LAST_TICK_COMMANDS, new String[]{});

        this.withProperty(LOOPING_SOUND, "");
        this.withProperty(LOOPING_SOUND_VOLUME, 1.0F);
        this.withProperty(LOOPING_SOUND_PITCH, 1.0F);

        this.withProperty(DESTROY_BLOCKS, false);
        this.withProperty(DESTROY_BLOCK_RADIUS, 0.0F);

    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        float range = entry.getProperty(RANGE);

        Vec3 spawnPos;

        if (entry.getProperty(SPAWN_AT_PLAYER)) {
            spawnPos = entity.position();
        } else if (entry.getProperty(SPAWN_BEHIND)) {
            spawnPos = getBehindPosition(entity, range);
        } else {
            spawnPos = raycastSpawnPosition(entity, range);
        }

        if (spawnPos == null) {
            return;
        }

        ArmorStand armorStand = EOPArmorStandSpawner.spawnBasic(
                entity,
                entity.level(),
                spawnPos,
                entity.getYRot()
        );

        EOPArmorStandSpawner.applyCommonData(
                armorStand,
                entry.getProperty(LIFETIME),
                entry.getProperty(AOE_DAMAGE),
                entry.getProperty(AOE_RADIUS),
                entry.getProperty(AOE_DAMAGE_TYPE),
                entry.getProperty(ENABLE_DAMAGE),
                entry.getProperty(DAMAGE_ON_LAST_TICK),
                entry.getProperty(KNOCKBACK_ON_LAST_TICK),
                entry.getProperty(TARGET_COMMAND_RADIUS),
                entry.getProperty(PULL_STRENGTH),
                entry.getProperty(INVERT_PULL),
                entry.getProperty(STAND_POWER),
                entry.getProperty(STAND_FIRST_TICK_COMMANDS),
                entry.getProperty(STAND_COMMANDS),
                entry.getProperty(STAND_LAST_TICK_COMMANDS),
                entry.getProperty(TARGET_FIRST_TICK_COMMANDS),
                entry.getProperty(TARGET_COMMANDS),
                entry.getProperty(TARGET_LAST_TICK_COMMANDS),
                entry.getProperty(LOOPING_SOUND),
                entry.getProperty(LOOPING_SOUND_VOLUME),
                entry.getProperty(LOOPING_SOUND_PITCH),
                entry.getProperty(DESTROY_BLOCKS),
                entry.getProperty(DESTROY_BLOCK_RADIUS)
        );
        ResourceLocation sound = ResourceLocation.tryParse(entry.getProperty(LOOPING_SOUND));

        if (sound != null && !entry.getProperty(LOOPING_SOUND).isEmpty()) {
            net.stonedgoldfish.eopmod.network.EOPNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> armorStand),
                    new net.stonedgoldfish.eopmod.network.PlayArmorStandLoopingSoundPacket(
                            armorStand.getId(),
                            sound,
                            entry.getProperty(LOOPING_SOUND_VOLUME),
                            entry.getProperty(LOOPING_SOUND_PITCH)
                    )
            );
        }
        if (entry.getProperty(FOLLOW_MOUSE)) {
            FOLLOWING_STANDS.put(entity.getUUID(), armorStand);
        }

        EOPForgeEvents.runStandCommands(armorStand, "EOPStandFirstTickCommands");
    }

    private static Vec3 raycastThroughBlocksPosition(LivingEntity entity, float range) {
        Level level = entity.level();

        Vec3 start = entity.getEyePosition();
        Vec3 look = entity.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                entity,
                start,
                end,
                entity.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D),
                target -> target instanceof LivingEntity
                        && target != entity
                        && target.isAlive()
        );

        if (entityHit != null) {
            return entityHit.getLocation();
        }

        return end;
    }

    private static Vec3 raycastSpawnPosition(LivingEntity entity, float range) {
        Level level = entity.level();

        Vec3 start = entity.getEyePosition();
        Vec3 look = entity.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));

        BlockHitResult blockHit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));

        double blockDistance = blockHit.getType() == HitResult.Type.BLOCK
                ? start.distanceTo(blockHit.getLocation())
                : range;

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                entity,
                start,
                end,
                entity.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D),
                target -> target instanceof LivingEntity
                        && target != entity
                        && target.isAlive()
        );

        double entityDistance = entityHit != null
                ? start.distanceTo(entityHit.getLocation())
                : range + 1.0D;

        if (entityHit != null && entityDistance < blockDistance) {
            return entityHit.getLocation();
        }

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getLocation();
        }

        return end;
    }

    private static Vec3 getBehindPosition(LivingEntity entity, float range) {
        float yawRad = (float) Math.toRadians(entity.getYRot());

        Vec3 forward = new Vec3(
                -Math.sin(yawRad),
                0.0D,
                Math.cos(yawRad)
        ).normalize();

        Vec3 behind = forward.scale(-range);

        return entity.position().add(behind);
    }

    private static Vec3 raycastFollowMousePosition(LivingEntity entity, float range) {
        Vec3 start = entity.getEyePosition();
        Vec3 look = entity.getLookAngle().normalize();
        return start.add(look.scale(range));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        if (!entry.getProperty(FOLLOW_MOUSE)) {
            return;
        }

        ArmorStand armorStand = FOLLOWING_STANDS.get(entity.getUUID());

        if (armorStand == null || !armorStand.isAlive()) {
            return;
        }

        Vec3 pos = entry.getProperty(SPAWN_AT_PLAYER)
                ? entity.position()
                : raycastFollowMousePosition(entity, entry.getProperty(RANGE));

        if (pos == null) {
            return;
        }

        armorStand.setDeltaMovement(Vec3.ZERO);
        armorStand.moveTo(
                pos.x,
                pos.y,
                pos.z,
                entity.getYRot(),
                entity.getXRot()
        );
        armorStand.setYHeadRot(entity.getYRot());
        armorStand.setYBodyRot(entity.getYRot());
        armorStand.hurtMarked = true;
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        ArmorStand armorStand = FOLLOWING_STANDS.remove(entity.getUUID());

        if (armorStand != null && armorStand.isAlive()) {
            EOPForgeEvents.runStandCommands(armorStand, "EOPStandLastTickCommands");
            armorStand.discard();
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Spawns an invisible armor stand at a raycast location or at the player, with configurable AOE, commands, pull, power, lifetime, and last-tick effects.";
    }
}
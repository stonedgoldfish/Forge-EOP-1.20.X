package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;

public class AreaDestroyAbility extends Ability {

    public static final PalladiumProperty<Float> RADIUS = new FloatProperty("radius").configurable("Destruction radius");
    public static final PalladiumProperty<Boolean> FRONT_ONLY = new BooleanProperty("front_only").configurable("If true, only destroys blocks in front of the caster");
    public static final PalladiumProperty<Float> FRONT_ANGLE = new FloatProperty("front_angle").configurable("Angle of the frontal destruction cone");
    public static final PalladiumProperty<Float> DESTRUCTION_AMOUNT = new FloatProperty("destruction_amount").configurable("Decides how clean the destruction is");
    public static final PalladiumProperty<Float> MAX_BLOCK_HARDNESS = new FloatProperty("max_block_hardness").configurable("Will ignore blocks above the defined hardness");
    public static final PalladiumProperty<Boolean> DROP_BLOCKS = new BooleanProperty("drop_blocks").configurable("If true, destroyed blocks drop items");
    public static final PalladiumProperty<Boolean> BREAK_BENEATH_CASTER = new BooleanProperty("break_beneath_caster").configurable("If false, blocks beneath the caster are protected");
    public static final PalladiumProperty<String> PARTICLE = new StringProperty("particle").configurable("Particle spawned in front of the caster when front_only is true");
    public static final PalladiumProperty<Integer> PARTICLE_AMOUNT = new IntegerProperty("particle_amount").configurable("Particles spawned in front of the caster");
    public static final PalladiumProperty<Float> PARTICLE_SPEED = new FloatProperty("particle_speed").configurable("Particle speed");
    public static final PalladiumProperty<String> SOUND = new StringProperty("sound").configurable("Sound played when destruction happens");
    public static final PalladiumProperty<Float> SOUND_VOLUME = new FloatProperty("sound_volume").configurable("Sound volume");
    public static final PalladiumProperty<Float> SOUND_PITCH = new FloatProperty("sound_pitch").configurable("Sound pitch");

    public AreaDestroyAbility() {
        this.withProperty(ICON, new ItemIcon(Items.IRON_PICKAXE));
        this.withProperty(RADIUS, 4.0F);
        this.withProperty(FRONT_ONLY, false);
        this.withProperty(FRONT_ANGLE, 70.0F);
        this.withProperty(DESTRUCTION_AMOUNT, 0.65F);
        this.withProperty(MAX_BLOCK_HARDNESS, 10.0F);
        this.withProperty(DROP_BLOCKS, false);
        this.withProperty(BREAK_BENEATH_CASTER, false);
        this.withProperty(PARTICLE, "minecraft:poof");
        this.withProperty(PARTICLE_AMOUNT, 12);
        this.withProperty(PARTICLE_SPEED, 0.05F);
        this.withProperty(SOUND, "minecraft:entity.generic.explode");
        this.withProperty(SOUND_VOLUME, 1.0F);
        this.withProperty(SOUND_PITCH, 1.0F);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (!EOPGameRules.isDestructionMode(level.getServer())) {
            return;
        }

        float radius = Math.max(0.0F, entry.getProperty(RADIUS));
        boolean frontOnly = entry.getProperty(FRONT_ONLY);
        float frontAngle = entry.getProperty(FRONT_ANGLE);
        float destructionAmount = clamp01(entry.getProperty(DESTRUCTION_AMOUNT));
        float maxHardness = entry.getProperty(MAX_BLOCK_HARDNESS);
        boolean dropBlocks = entry.getProperty(DROP_BLOCKS);
        boolean breakBeneathCaster = entry.getProperty(BREAK_BENEATH_CASTER);

        BlockPos center = entity.blockPosition();
        int r = (int) Math.ceil(radius);

        java.util.Random random = new java.util.Random();

        Vec3 origin = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        Vec3 forward = getFlatForward(entity);

        int destroyed = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -r, -r),
                center.offset(r, r, r)
        )) {
            double distance = Math.sqrt(pos.distSqr(center));

            if (distance > radius) {
                continue;
            }

            if (!breakBeneathCaster && isBeneathCaster(entity, pos, radius)) {
                continue;
            }

            if (frontOnly && !isInFrontCone(origin, forward, pos, frontAngle, radius)) {
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

            double norm = radius <= 0.0F ? 0.0D : distance / radius;

            double chance = 1.0D - (norm * (1.0D - destructionAmount));

            if (distance <= 1.5D) {
                chance = 1.0D;
            }

            if (random.nextDouble() > chance) {
                continue;
            }

            level.destroyBlock(pos, dropBlocks, entity);
            destroyed++;
        }

        if (frontOnly && destroyed > 0) {
            spawnFrontParticles(
                    level,
                    entity,
                    entry.getProperty(PARTICLE),
                    entry.getProperty(PARTICLE_AMOUNT),
                    entry.getProperty(PARTICLE_SPEED),
                    radius
            );
        }

        if (destroyed > 0) {
            playSound(
                    entity,
                    entry.getProperty(SOUND),
                    entry.getProperty(SOUND_VOLUME),
                    entry.getProperty(SOUND_PITCH)
            );
        }
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

    private static Vec3 getFlatForward(LivingEntity entity) {
        float yawRad = entity.getYRot() * ((float) Math.PI / 180.0F);

        return new Vec3(
                -Math.sin(yawRad),
                0.0D,
                Math.cos(yawRad)
        ).normalize();
    }

    private static void spawnFrontParticles(
            ServerLevel level,
            LivingEntity entity,
            String particleId,
            int amount,
            float speed,
            float radius
    ) {
        if (particleId == null || particleId.isBlank() || amount <= 0) {
            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(particleId);

        if (id == null) {
            return;
        }

        var particleType = BuiltInRegistries.PARTICLE_TYPE.get(id);

        if (!(particleType instanceof SimpleParticleType simple)) {
            return;
        }

        Vec3 look = getFlatForward(entity);

        Vec3 center = entity.position()
                .add(0.0D, entity.getBbHeight() * 0.5D, 0.0D)
                .add(look.scale(Math.min(radius, 2.0D)));

        level.sendParticles(
                simple,
                center.x,
                center.y,
                center.z,
                amount,
                0.25D,
                0.25D,
                0.25D,
                speed
        );
    }

    private static void playSound(LivingEntity entity, String soundId, float volume, float pitch) {
        if (soundId == null || soundId.isBlank()) {
            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(soundId);

        if (id == null) {
            return;
        }

        SoundEvent sound = SoundEvent.createVariableRangeEvent(id);

        entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                sound,
                SoundSource.BLOCKS,
                volume,
                pitch
        );
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    @Override
    public String getDocumentationDescription() {
        return "Destroys blocks in a radius.";
    }
}
package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringProperty;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParticlePatternAbility extends Ability {

    public static final PalladiumProperty<String> PARTICLE_PATTERN = new StringProperty("particle_pattern").configurable("Particle pattern. Currently supports: implosion, outwards_pulse, inwards_pulse, sphere_outwards_pulse, sphere_inwards_pulse");
    public static final PalladiumProperty<String> PARTICLE_TYPE = new StringProperty("particle_type").configurable("Particle type resource location");
    public static final PalladiumProperty<Integer> PARTICLE_AMOUNT = new IntegerProperty("particle_amount").configurable("Particles spawned per tick");
    public static final PalladiumProperty<Float> PARTICLE_RADIUS = new FloatProperty("particle_radius").configurable("Particle effect radius");
    public static final PalladiumProperty<Float> PARTICLE_SPEED = new FloatProperty("particle_speed").configurable("Particle movement speed");
    public static final PalladiumProperty<Float> PARTICLE_Y_OFFSET = new FloatProperty("particle_y_offset").configurable("Vertical offset for the particle center");

    public ParticlePatternAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FIREWORK_STAR));

        this.withProperty(PARTICLE_PATTERN, "implosion");
        this.withProperty(PARTICLE_TYPE, "minecraft:smoke");
        this.withProperty(PARTICLE_AMOUNT, 8);
        this.withProperty(PARTICLE_RADIUS, 2.5F);
        this.withProperty(PARTICLE_SPEED, 0.08F);
        this.withProperty(PARTICLE_Y_OFFSET, 1.0F);
    }
    private static final Map<UUID, Integer> ACTIVATION_START_TICKS = new ConcurrentHashMap<>();

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        UUID entityId = entity.getUUID();

        if (!enabled) {
            ACTIVATION_START_TICKS.remove(entityId);
            return;
        }

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int activationTick = ACTIVATION_START_TICKS.computeIfAbsent(entityId, id -> entity.tickCount);
        int abilityTicks = entity.tickCount - activationTick;

        String pattern = entry.getProperty(PARTICLE_PATTERN);
        ParticleOptions particle = getParticle(entry.getProperty(PARTICLE_TYPE));

        int amount = entry.getProperty(PARTICLE_AMOUNT);
        float radius = entry.getProperty(PARTICLE_RADIUS);
        float speed = entry.getProperty(PARTICLE_SPEED);
        float yOffset = entry.getProperty(PARTICLE_Y_OFFSET);

        if (particle == null || amount <= 0 || radius <= 0.0F) {
            return;
        }

        double centerX = entity.getX();
        double centerY = entity.getY() + entity.getBbHeight() * 0.5D + yOffset;
        double centerZ = entity.getZ();

        switch (pattern) {
            case "outwards_pulse" -> spawnPulseRingParticles(
                    serverLevel, abilityTicks,
                    centerX, centerY, centerZ,
                    particle, amount, radius, speed,
                    false
            );

            case "inwards_pulse" -> spawnPulseRingParticles(
                    serverLevel, abilityTicks,
                    centerX, centerY, centerZ,
                    particle, amount, radius, speed,
                    true
            );

            case "sphere_outwards_pulse" -> spawnPulseSphereParticles(
                    serverLevel,
                    abilityTicks,
                    centerX,
                    centerY,
                    centerZ,
                    particle,
                    amount,
                    radius,
                    false
            );

            case "sphere_inwards_pulse" -> spawnPulseSphereParticles(
                    serverLevel,
                    abilityTicks,
                    centerX,
                    centerY,
                    centerZ,
                    particle,
                    amount,
                    radius,
                    true
            );

            case "implosion" -> spawnImplosionParticles(
                    serverLevel,
                    centerX,
                    centerY,
                    centerZ,
                    particle,
                    amount,
                    radius,
                    speed
            );
        }
    }

    private static ParticleOptions getParticle(String particleId) {
        ResourceLocation location = ResourceLocation.tryParse(particleId);

        if (location == null) {
            return ParticleTypes.SMOKE;
        }

        var particleType = BuiltInRegistries.PARTICLE_TYPE.getOptional(location);

        if (particleType.isEmpty()) {
            return ParticleTypes.SMOKE;
        }

        if (particleType.get() instanceof net.minecraft.core.particles.SimpleParticleType simpleParticle) {
            return simpleParticle;
        }

        return ParticleTypes.SMOKE;
    }

    private static void spawnImplosionParticles(
            ServerLevel level,
            double centerX,
            double centerY,
            double centerZ,
            ParticleOptions particle,
            int amount,
            float radius,
            float speed
    ) {
        for (int i = 0; i < amount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double height = (level.random.nextDouble() - 0.5D) * radius;
            double distance = radius * (0.6D + level.random.nextDouble() * 0.4D);

            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + height;
            double z = centerZ + Math.sin(angle) * distance;

            double motionX = (centerX - x) * speed;
            double motionY = (centerY - y) * speed;
            double motionZ = (centerZ - z) * speed;

            level.sendParticles(
                    particle,
                    x,
                    y,
                    z,
                    0,
                    motionX,
                    motionY,
                    motionZ,
                    1.0D
            );
        }
    }

    private static void spawnPulseRingParticles(
            ServerLevel level,
            int abilityTicks,
            double centerX,
            double centerY,
            double centerZ,
            ParticleOptions particle,
            int amount,
            float maxRadius,
            float speed,
            boolean inward
    ) {
        int pulseLength = 20;

        double progress = (abilityTicks % pulseLength) / (double) pulseLength;

        double ringRadius = inward
                ? maxRadius * (1.0D - progress)
                : maxRadius * progress;

        int ringAmount = Math.min(
                amount * 8,
                Math.max(amount, (int) Math.ceil(ringRadius * Math.PI * 2.0D * amount))
        );

        for (int i = 0; i < ringAmount; i++) {
            double angle = (Math.PI * 2.0D * i) / ringAmount;

            double x = centerX + Math.cos(angle) * ringRadius;
            double y = centerY;
            double z = centerZ + Math.sin(angle) * ringRadius;

            level.sendParticles(
                    particle,
                    x,
                    y,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static void spawnPulseSphereParticles(
            ServerLevel level,
            int abilityTicks,
            double centerX,
            double centerY,
            double centerZ,
            ParticleOptions particle,
            int amount,
            float maxRadius,
            boolean inward
    ) {
        int pulseLength = 20;

        double progress = (abilityTicks % pulseLength) / (double) pulseLength;

        double sphereRadius = inward
                ? maxRadius * (1.0D - progress)
                : maxRadius * progress;

        int sphereAmount = Math.max(
                amount,
                (int) Math.ceil(4.0D * Math.PI * sphereRadius * sphereRadius * amount)
        );

        sphereAmount = Math.min(sphereAmount, amount * 20);

        double goldenAngle = Math.PI * (3.0D - Math.sqrt(5.0D));

        for (int i = 0; i < sphereAmount; i++) {
            double yNormalized = 1.0D - (i / (double) (sphereAmount - 1)) * 2.0D;
            double horizontalRadius = Math.sqrt(1.0D - yNormalized * yNormalized);

            double theta = goldenAngle * i;

            double x = centerX + Math.cos(theta) * horizontalRadius * sphereRadius;
            double y = centerY + yNormalized * sphereRadius;
            double z = centerZ + Math.sin(theta) * horizontalRadius * sphereRadius;

            level.sendParticles(
                    particle,
                    x,
                    y,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Spawns configurable particle patterns around the entity while enabled.";
    }
}
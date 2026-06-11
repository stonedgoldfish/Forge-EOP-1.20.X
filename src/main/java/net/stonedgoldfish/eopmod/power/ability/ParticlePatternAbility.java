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

public class ParticlePatternAbility extends Ability {

    public static final PalladiumProperty<String> PARTICLE_PATTERN = new StringProperty("particle_pattern").configurable("Particle pattern. Currently supports: implosion, outward_pulse, inward_pulse");
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

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

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
            case "outwards_pulse" -> spawnOutwardsPulseParticles(
                    serverLevel,
                    centerX,
                    centerY,
                    centerZ,
                    particle,
                    amount,
                    radius,
                    speed
            );

            case "inwards_pulse" -> spawnInwardsPulseParticles(
                    serverLevel,
                    centerX,
                    centerY,
                    centerZ,
                    particle,
                    amount,
                    radius,
                    speed
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

    private static void spawnOutwardsPulseParticles(
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
            double yOffset = (level.random.nextDouble() - 0.5D) * radius * 0.4D;

            double x = centerX;
            double y = centerY + yOffset;
            double z = centerZ;

            double motionX = Math.cos(angle) * speed;
            double motionY = (level.random.nextDouble() - 0.5D) * speed * 0.3D;
            double motionZ = Math.sin(angle) * speed;

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

    private static void spawnInwardsPulseParticles(
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
            double yOffset = (level.random.nextDouble() - 0.5D) * radius * 0.4D;

            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + yOffset;
            double z = centerZ + Math.sin(angle) * radius;

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

    @Override
    public String getDocumentationDescription() {
        return "Spawns configurable particle patterns around the entity while enabled.";
    }
}
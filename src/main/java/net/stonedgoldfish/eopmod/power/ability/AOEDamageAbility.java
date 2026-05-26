package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.stonedgoldfish.eopmod.util.EOPTargeting;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AOEDamageAbility extends Ability {

    public static final PalladiumProperty<Float> DAMAGE =
            new FloatProperty("damage")
                    .configurable("Amount of damage dealt to nearby entities.");

    public static final PalladiumProperty<Boolean> ENABLE_DAMAGE =
            new BooleanProperty("enable_damage")
                    .configurable("Whether the ability deals damage.");

    public static final PalladiumProperty<Float> KNOCKBACK =
            new FloatProperty("knockback")
                    .configurable("Knockback strength applied to damaged targets");

    public static final PalladiumProperty<Float> RADIUS =
            new FloatProperty("radius")
                    .configurable("Radius around the user that entities will be damaged in.");

    public static final PalladiumProperty<String[]> COMMANDS_ON_TARGET =
            new StringArrayProperty("commands_on_target")
                    .configurable("Commands executed as valid targets in range.");

    public static final PalladiumProperty<Boolean> CONE =
            new BooleanProperty("cone")
                    .configurable("If true, only damages entities in front of the user.");

    public static final PalladiumProperty<Float> CONE_ANGLE =
            new FloatProperty("cone_angle")
                    .configurable("Width of the cone in degrees.");

    public static final PalladiumProperty<String> DAMAGE_TYPE =
            new StringProperty("damage_type")
                    .configurable("Damage type used by this ability. Example: minecraft:magic");

    public static final PalladiumProperty<Integer> SET_ON_FIRE =
            new IntegerProperty("set_on_fire")
                    .configurable("Sets targets on fire for this many seconds. 0 disables it.");

    public static final PalladiumProperty<Boolean> CREATE_EXPLOSION =
            new BooleanProperty("create_explosion")
                    .configurable("If true, creates an explosion effect");

    public static final PalladiumProperty<Boolean> EXPLOSION_CAUSES_FIRE =
            new BooleanProperty("explosion_causes_fire")
                    .configurable("If true, the explosion creates fire when destructionMode is enabled.");

    public static final PalladiumProperty<Float> EXPLOSION_RADIUS =
            new FloatProperty("explosion_radius")
                    .configurable("Radius of the explosion");

    public static final PalladiumProperty<Boolean> EXPLOSION_DROP_BLOCKS =
            new BooleanProperty("explosion_drop_blocks")
                    .configurable("If true, destroyed explosion blocks will drop items");

    public static final PalladiumProperty<Boolean> ENABLE_PARTICLES =
            new BooleanProperty("enable_particles")
                    .configurable("Whether this ability spawns outward burst particles.");
    public static final PalladiumProperty<String> PARTICLE_TYPE =
            new StringProperty("particle_type")
                    .configurable("Particle ID to spawn. Example: minecraft:flame");

    public static final PalladiumProperty<Float> PARTICLE_SPEED =
            new FloatProperty("particle_speed")
                    .configurable("Speed particles travel outward.");

    public static final PalladiumProperty<Integer> PARTICLE_AMOUNT =
            new IntegerProperty("particle_amount")
                    .configurable("Base amount of particles spawned. Scales with radius.");

    public AOEDamageAbility() {
        this.withProperty(ICON, new ItemIcon(Items.TNT));
        this.withProperty(DAMAGE, 6.0F);
        this.withProperty(ENABLE_DAMAGE, true);
        this.withProperty(KNOCKBACK, 0.0F);
        this.withProperty(RADIUS, 4.0F);
        this.withProperty(COMMANDS_ON_TARGET, new String[]{});
        this.withProperty(CONE, false);
        this.withProperty(CONE_ANGLE, 90.0F);
        this.withProperty(DAMAGE_TYPE, "minecraft:magic");
        this.withProperty(SET_ON_FIRE, 0);
        this.withProperty(CREATE_EXPLOSION, false);
        this.withProperty(EXPLOSION_CAUSES_FIRE, false);
        this.withProperty(EXPLOSION_RADIUS, 3.0F);
        this.withProperty(EXPLOSION_DROP_BLOCKS, false);
        this.withProperty(ENABLE_PARTICLES, false);
        this.withProperty(PARTICLE_TYPE, "minecraft:poof");
        this.withProperty(PARTICLE_SPEED, 0.2F);
        this.withProperty(PARTICLE_AMOUNT, 24);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        float damage = entry.getProperty(DAMAGE);
        boolean enableDamage = entry.getProperty(ENABLE_DAMAGE);
        float knockback = entry.getProperty(KNOCKBACK);
        float radius = entry.getProperty(RADIUS);
        String[] commandsOnTarget = entry.getProperty(COMMANDS_ON_TARGET);
        boolean cone = entry.getProperty(CONE);
        float coneAngle = entry.getProperty(CONE_ANGLE);
        String damageTypeId = entry.getProperty(DAMAGE_TYPE);
        int fireSeconds = entry.getProperty(SET_ON_FIRE);
        boolean createExplosion = entry.getProperty(CREATE_EXPLOSION);
        boolean explosionCausesFire = entry.getProperty(EXPLOSION_CAUSES_FIRE);
        float explosionRadius = entry.getProperty(EXPLOSION_RADIUS);
        boolean explosionDropBlocks = entry.getProperty(EXPLOSION_DROP_BLOCKS);
        boolean enableParticles = entry.getProperty(ENABLE_PARTICLES);
        String particleType = entry.getProperty(PARTICLE_TYPE);
        float particleSpeed = entry.getProperty(PARTICLE_SPEED);
        int particleAmount = entry.getProperty(PARTICLE_AMOUNT);

        AABB area = entity.getBoundingBox().inflate(radius);

        List<LivingEntity> targets = entity.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                target -> target != entity && target.isAlive()
        );

        DamageSource damageSource = createDamageSource(entity, damageTypeId);

        for (LivingEntity target : targets) {
            if (!EOPTargeting.isValidTarget(entity, target)) {
                continue;
            }

            if (cone && !isInsideCone(entity, target, coneAngle)) {
                continue;
            }

            if (enableDamage) {
                target.hurt(damageSource, damage);
            }

            if (fireSeconds > 0) {
                target.setSecondsOnFire(fireSeconds);
            }

            if (knockback > 0.0F) {
                Vec3 direction = target.position()
                        .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                        .subtract(entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D));

                if (direction.lengthSqr() < 0.001D) {
                    direction = new Vec3(0.0D, 0.2D, 0.0D);
                } else {
                    direction = direction.normalize();
                }

                target.setDeltaMovement(
                        target.getDeltaMovement().add(direction.scale(knockback))
                );

                target.hurtMarked = true;
            }

            runCommandsAsTarget(target, commandsOnTarget);
        }
        if (createExplosion) {

            createBlockOnlyExplosion(
                    entity,
                    explosionRadius,
                    explosionDropBlocks,
                    EOPGameRules.isDestructionMode(entity.level().getServer()),
                    explosionCausesFire
            );
        }

        if (enableParticles) {
            spawnOutwardParticles(
                    entity,
                    radius,
                    cone,
                    coneAngle,
                    particleAmount,
                    particleType,
                    particleSpeed
            );
        }
    }

    private static void spawnOutwardParticles(
            LivingEntity entity,
            float radius,
            boolean cone,
            float coneAngle,
            int baseAmount,
            String particleId,
            float particleSpeed
    ) {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }

        java.util.Random random = new java.util.Random();

        Vec3 origin = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        Vec3 look = entity.getLookAngle().normalize();

        int amount = Math.max(1, (int) (baseAmount * Math.max(1.0F, radius)));

        net.minecraft.core.particles.ParticleOptions particle = getParticle(particleId);

        for (int i = 0; i < amount; i++) {
            Vec3 direction;

            if (cone) {
                direction = randomDirectionInCone(look, coneAngle, random);
            } else {
                direction = randomDirectionSphere(random);
            }

            Vec3 spawnPos = origin.add(direction.scale(0.4D));

            level.sendParticles(
                    particle,
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    0,
                    direction.x * particleSpeed,
                    direction.y * particleSpeed,
                    direction.z * particleSpeed,
                    1.0D
            );
        }
    }

    private static net.minecraft.core.particles.ParticleOptions getParticle(String particleId) {
        net.minecraft.resources.ResourceLocation id =
                net.minecraft.resources.ResourceLocation.tryParse(particleId);

        if (id == null) {
            return net.minecraft.core.particles.ParticleTypes.POOF;
        }

        var particleType =
                net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.get(id);

        if (particleType instanceof net.minecraft.core.particles.SimpleParticleType simple) {
            return simple;
        }

        return net.minecraft.core.particles.ParticleTypes.POOF;
    }

    private static Vec3 randomDirectionSphere(java.util.Random random) {
        double theta = random.nextDouble() * Math.PI * 2.0D;
        double z = (random.nextDouble() * 2.0D) - 1.0D;
        double root = Math.sqrt(1.0D - z * z);

        return new Vec3(
                root * Math.cos(theta),
                z,
                root * Math.sin(theta)
        ).normalize();
    }

    private static Vec3 randomDirectionInCone(Vec3 forward, float coneAngle, java.util.Random random) {
        double halfAngleRad = Math.toRadians(coneAngle / 2.0F);

        double cosHalfAngle = Math.cos(halfAngleRad);
        double cosTheta = cosHalfAngle + random.nextDouble() * (1.0D - cosHalfAngle);
        double sinTheta = Math.sqrt(1.0D - cosTheta * cosTheta);
        double phi = random.nextDouble() * Math.PI * 2.0D;

        Vec3 up = Math.abs(forward.y) > 0.99D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);

        Vec3 right = forward.cross(up).normalize();
        Vec3 actualUp = right.cross(forward).normalize();

        return forward.scale(cosTheta)
                .add(right.scale(Math.cos(phi) * sinTheta))
                .add(actualUp.scale(Math.sin(phi) * sinTheta))
                .normalize();
    }

    private static void runCommandsAsTarget(LivingEntity target, String[] commands) {
        if (commands == null || commands.length == 0) {
            return;
        }

        if (target.getServer() == null) {
            return;
        }

        for (String command : commands) {
            if (command == null || command.isBlank()) {
                continue;
            }

            target.getServer().getCommands().performPrefixedCommand(
                    target.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2),
                    command
            );
        }
    }

    private static boolean isInsideCone(LivingEntity caster, LivingEntity target, float coneAngle) {
        Vec3 look = caster.getLookAngle().normalize();

        Vec3 toTarget = target.position()
                .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D))
                .normalize();

        double halfAngleRad = Math.toRadians(coneAngle / 2.0F);
        double threshold = Math.cos(halfAngleRad);

        return look.dot(toTarget) >= threshold;
    }

    private static DamageSource createDamageSource(LivingEntity entity, String damageTypeId) {
        ResourceLocation damageLocation = ResourceLocation.tryParse(damageTypeId);

        if (damageLocation == null) {
            return entity.damageSources().magic();
        }

        var damageTypeKey = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                damageLocation
        );

        return new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(damageTypeKey),
                entity
        );
    }

    private static void createBlockOnlyExplosion(
            LivingEntity entity,
            float radius,
            boolean dropBlocks,
            boolean destroyBlocks,
            boolean createFire
    ) {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }

        net.minecraft.core.BlockPos center = entity.blockPosition();
        int r = (int) Math.ceil(radius);

        java.util.Random random = new java.util.Random();

        if (destroyBlocks) {

            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                    center.offset(-r, -r, -r),
                    center.offset(r, r, r)
            )) {
                double distance = Math.sqrt(pos.distSqr(center));

                if (distance > radius) {
                    continue;
                }

                var state = level.getBlockState(pos);

                if (state.isAir()) {
                    continue;
                }

                if (state.getDestroySpeed(level, pos) < 0.0F) {
                    continue;
                }

                double norm = distance / radius;

                double breakChance = 1.0D - (norm * norm);
                breakChance += (random.nextDouble() - 0.5D) * (norm * 0.8D);

                if (breakChance < 0.15D) {
                    continue;
                }

                if (random.nextDouble() <= breakChance) {
                    level.destroyBlock(pos, dropBlocks, entity);
                }
            }
        }

        int particleAmount = (int) (radius * radius * 8);

        for (int i = 0; i < particleAmount; i++) {
            double offsetX = (random.nextDouble() - 0.5D) * radius * 2.0D;
            double offsetY = (random.nextDouble() - 0.5D) * radius * 2.0D;
            double offsetZ = (random.nextDouble() - 0.5D) * radius * 2.0D;

            if ((offsetX * offsetX) + (offsetY * offsetY) + (offsetZ * offsetZ) > radius * radius) {
                continue;
            }

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    entity.getX() + offsetX,
                    entity.getY() + 0.5D + offsetY,
                    entity.getZ() + offsetZ,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.POOF,
                    entity.getX() + offsetX,
                    entity.getY() + 0.5D + offsetY,
                    entity.getZ() + offsetZ,
                    2,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.02D
            );
        }

        level.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.BLOCKS,
                Math.min(4.0F, radius / 2.0F),
                1.0F
        );

        if (createFire && destroyBlocks) {

            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                    center.offset(-r, -1, -r),
                    center.offset(r, 1, r)
            )) {

                if (random.nextFloat() > 0.25F) {
                    continue;
                }

                if (pos.distSqr(center) > radius * radius) {
                    continue;
                }

                net.minecraft.core.BlockPos firePos = pos.above();

                if (!level.getBlockState(firePos).isAir()) {
                    continue;
                }

                if (level.getBlockState(pos).isAir()) {
                    continue;
                }

                level.setBlockAndUpdate(
                        firePos,
                        net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState()
                );
            }
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Deals configurable area damage to nearby living entities.";
    }
}
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
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringProperty;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.BooleanProperty;
import net.stonedgoldfish.eopmod.util.EOPGameRules;

import java.util.List;

public class AOEDamageAbility extends Ability {

    public static final PalladiumProperty<Float> DAMAGE =
            new FloatProperty("damage")
                    .configurable("Amount of damage dealt to nearby entities.");

    public static final PalladiumProperty<Float> RADIUS =
            new FloatProperty("radius")
                    .configurable("Radius around the user that entities will be damaged in.");

    public static final PalladiumProperty<String> DAMAGE_TYPE =
            new StringProperty("damage_type")
                    .configurable("Damage type used by this ability. Example: minecraft:magic");

    public static final PalladiumProperty<Integer> SET_ON_FIRE =
            new IntegerProperty("set_on_fire")
                    .configurable("Sets targets on fire for this many seconds. 0 disables it.");

    public static final PalladiumProperty<Boolean> CREATE_EXPLOSION =
            new BooleanProperty("create_explosion")
                    .configurable("If true, creates an explosion effect");

    public static final PalladiumProperty<Float> EXPLOSION_RADIUS =
            new FloatProperty("explosion_radius")
                    .configurable("Radius of the explosion");

    public static final PalladiumProperty<Boolean> EXPLOSION_DROP_BLOCKS =
            new BooleanProperty("explosion_drop_blocks")
                    .configurable("If true, destroyed explosion blocks will drop items");

    public AOEDamageAbility() {
        this.withProperty(ICON, new ItemIcon(Items.TNT));
        this.withProperty(DAMAGE, 6.0F);
        this.withProperty(RADIUS, 4.0F);
        this.withProperty(DAMAGE_TYPE, "minecraft:magic");
        this.withProperty(SET_ON_FIRE, 0);
        this.withProperty(CREATE_EXPLOSION, false);
        this.withProperty(EXPLOSION_RADIUS, 3.0F);
        this.withProperty(EXPLOSION_DROP_BLOCKS, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        float damage = entry.getProperty(DAMAGE);
        float radius = entry.getProperty(RADIUS);
        String damageTypeId = entry.getProperty(DAMAGE_TYPE);
        int fireSeconds = entry.getProperty(SET_ON_FIRE);
        boolean createExplosion = entry.getProperty(CREATE_EXPLOSION);
        float explosionRadius = entry.getProperty(EXPLOSION_RADIUS);
        boolean explosionDropBlocks = entry.getProperty(EXPLOSION_DROP_BLOCKS);

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

            target.hurt(damageSource, damage);

            if (fireSeconds > 0) {
                target.setSecondsOnFire(fireSeconds);
            }
        }
        if (createExplosion) {

            createBlockOnlyExplosion(
                    entity,
                    explosionRadius,
                    explosionDropBlocks,
                    EOPGameRules.isDestructionMode(entity.level().getServer())
            );
        }
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
            boolean destroyBlocks
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
    }

    @Override
    public String getDocumentationDescription() {
        return "Deals configurable area damage to nearby living entities.";
    }
}
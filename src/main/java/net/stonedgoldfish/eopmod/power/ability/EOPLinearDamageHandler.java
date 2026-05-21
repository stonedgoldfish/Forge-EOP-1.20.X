package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.util.EOPTargeting;

import java.util.*;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EOPLinearDamageHandler {

    private static final List<LinearDamageInstance> ACTIVE = new ArrayList<>();

    public static void spawn(
            LivingEntity caster,
            float damage,
            float range,
            float width,
            int travelTime,
            String damageType,
            String particle
    ) {
        ACTIVE.add(new LinearDamageInstance(
                caster,
                caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D),
                caster.getLookAngle().normalize(),
                damage,
                range,
                width,
                Math.max(1, travelTime),
                damageType,
                particle
        ));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Iterator<LinearDamageInstance> iterator = ACTIVE.iterator();

        while (iterator.hasNext()) {
            LinearDamageInstance instance = iterator.next();

            if (!instance.tick()) {
                iterator.remove();
            }
        }
    }

    private static class LinearDamageInstance {

        private final LivingEntity caster;
        private final Vec3 origin;
        private final Vec3 direction;
        private final float damage;
        private final float range;
        private final float width;
        private final int travelTime;
        private final String damageType;
        private final String particle;
        private final Set<UUID> hitEntities = new HashSet<>();

        private int age = 0;

        private LinearDamageInstance(
                LivingEntity caster,
                Vec3 origin,
                Vec3 direction,
                float damage,
                float range,
                float width,
                int travelTime,
                String damageType,
                String particle
        ) {
            this.caster = caster;
            this.origin = origin;
            this.direction = direction;
            this.damage = damage;
            this.range = range;
            this.width = width;
            this.travelTime = travelTime;
            this.damageType = damageType;
            this.particle = particle;
        }

        private boolean tick() {
            if (caster == null || !caster.isAlive()) {
                return false;
            }

            age++;

            float progress = Math.min((float) age / travelTime, 1.0F);
            double currentDistance = range * progress;

            Vec3 center = origin.add(direction.scale(currentDistance));

            spawnTravelParticle(center);

            AABB searchBox = new AABB(
                    center.x - width,
                    center.y - width,
                    center.z - width,
                    center.x + width,
                    center.y + width,
                    center.z + width
            );

            DamageSource source = createDamageSource(caster, damageType);

            for (LivingEntity target : caster.level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
                if (!EOPTargeting.isValidTarget(caster, target)) {
                    continue;
                }

                if (hitEntities.contains(target.getUUID())) {
                    continue;
                }

                Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);

                if (!isInsideLine(origin, direction, targetPos, currentDistance, width)) {
                    continue;
                }

                target.hurt(source, damage);
                hitEntities.add(target.getUUID());
            }

            return age < travelTime;
        }

        private void spawnTravelParticle(Vec3 center) {
            if (particle == null || particle.isEmpty()) {
                return;
            }

            if (!(caster.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
                return;
            }

            ResourceLocation particleLocation = ResourceLocation.tryParse(particle);

            if (particleLocation == null) {
                return;
            }

            var particleType = BuiltInRegistries.PARTICLE_TYPE.get(particleLocation);

            if (!(particleType instanceof SimpleParticleType simpleParticleType)) {
                return;
            }

            serverLevel.sendParticles(
                    simpleParticleType,
                    center.x,
                    center.y,
                    center.z,
                    8,
                    width * 0.25D,
                    width * 0.25D,
                    width * 0.25D,
                    0.02D
            );
        }
    }

    private static boolean isInsideLine(Vec3 origin, Vec3 direction, Vec3 targetPos, double maxDistance, double width) {
        Vec3 toTarget = targetPos.subtract(origin);

        double forwardDistance = toTarget.dot(direction);

        if (forwardDistance < 0.0D || forwardDistance > maxDistance) {
            return false;
        }

        Vec3 closestPoint = origin.add(direction.scale(forwardDistance));

        return targetPos.distanceTo(closestPoint) <= width;
    }

    private static DamageSource createDamageSource(LivingEntity entity, String damageTypeId) {
        ResourceLocation damageLocation = ResourceLocation.tryParse(damageTypeId);

        if (damageLocation == null) {
            return entity.damageSources().magic();
        }

        ResourceKey<DamageType> damageTypeKey = ResourceKey.create(
                Registries.DAMAGE_TYPE,
                damageLocation
        );

        return new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(damageTypeKey),
                entity
        );
    }
}
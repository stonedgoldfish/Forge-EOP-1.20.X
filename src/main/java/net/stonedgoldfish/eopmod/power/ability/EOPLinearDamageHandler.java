package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.event.EOPForgeEvents;
import net.stonedgoldfish.eopmod.util.EOPArmorStandSpawner;
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
            String particle,
            String[] commandsOnTargets,
            String[] commandsOnAllies,
            int maxWallThickness,
            boolean ignorePitch,

            boolean spawnArmorStand,
            int armorStandInterval,
            int armorStandLifetime,
            float armorStandAoeDamage,
            float armorStandAoeRadius,
            String armorStandAoeDamageType,
            boolean armorStandEnableDamage,
            boolean armorStandDamageOnLastTick,
            float armorStandKnockbackOnLastTick,
            float armorStandTargetCommandRadius,
            float armorStandPullStrength,
            boolean armorStandInvertPull,
            String armorStandPower,
            String[] armorStandFirstTickCommands,
            String[] armorStandCommands,
            String[] armorStandLastTickCommands,
            String[] armorStandTargetFirstTickCommands,
            String[] armorStandTargetCommands,
            String[] armorStandTargetLastTickCommands,
            String armorStandLoopingSound,
            float armorStandLoopingSoundVolume,
            float armorStandLoopingSoundPitch,
            boolean armorStandDestroyBlocks,
            float armorStandDestroyBlockRadius
    ) {
        ACTIVE.add(new LinearDamageInstance(
                caster,
                caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D),
                getDirection(caster, ignorePitch),
                damage,
                range,
                width,
                Math.max(1, travelTime),
                damageType,
                particle,
                commandsOnTargets,
                commandsOnAllies,
                maxWallThickness,

                spawnArmorStand,
                Math.max(1, armorStandInterval),
                armorStandLifetime,
                armorStandAoeDamage,
                armorStandAoeRadius,
                armorStandAoeDamageType,
                armorStandEnableDamage,
                armorStandDamageOnLastTick,
                armorStandKnockbackOnLastTick,
                armorStandTargetCommandRadius,
                armorStandPullStrength,
                armorStandInvertPull,
                armorStandPower,
                armorStandFirstTickCommands,
                armorStandCommands,
                armorStandLastTickCommands,
                armorStandTargetFirstTickCommands,
                armorStandTargetCommands,
                armorStandTargetLastTickCommands,
                armorStandLoopingSound,
                armorStandLoopingSoundVolume,
                armorStandLoopingSoundPitch,
                armorStandDestroyBlocks,
                armorStandDestroyBlockRadius
        ));
    }

    private static Vec3 getDirection(LivingEntity caster, boolean ignorePitch) {
        if (!ignorePitch) {
            return caster.getLookAngle().normalize();
        }

        float yawRad = caster.getYRot() * ((float) Math.PI / 180.0F);

        return new Vec3(
                -Math.sin(yawRad),
                0.0D,
                Math.cos(yawRad)
        ).normalize();
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
        private final String[] commandsOnTargets;
        private final String[] commandsOnAllies;
        private final Set<UUID> hitEntities = new HashSet<>();
        private final int maxWallThickness;

        private final boolean spawnArmorStand;
        private final int armorStandInterval;
        private final int armorStandLifetime;
        private final float armorStandAoeDamage;
        private final float armorStandAoeRadius;
        private final String armorStandAoeDamageType;
        private final boolean armorStandEnableDamage;
        private final boolean armorStandDamageOnLastTick;
        private final float armorStandKnockbackOnLastTick;
        private final float armorStandTargetCommandRadius;
        private final float armorStandPullStrength;
        private final boolean armorStandInvertPull;
        private final String armorStandPower;
        private final String[] armorStandFirstTickCommands;
        private final String[] armorStandCommands;
        private final String[] armorStandLastTickCommands;
        private final String[] armorStandTargetFirstTickCommands;
        private final String[] armorStandTargetCommands;
        private final String[] armorStandTargetLastTickCommands;
        private final String armorStandLoopingSound;
        private final float armorStandLoopingSoundVolume;
        private final float armorStandLoopingSoundPitch;
        private final boolean armorStandDestroyBlocks;
        private final float armorStandDestroyBlockRadius;

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
                String particle,
                String[] commandsOnTargets,
                String[] commandsOnAllies,
                int maxWallThickness,

                boolean spawnArmorStand,
                int armorStandInterval,
                int armorStandLifetime,
                float armorStandAoeDamage,
                float armorStandAoeRadius,
                String armorStandAoeDamageType,
                boolean armorStandEnableDamage,
                boolean armorStandDamageOnLastTick,
                float armorStandKnockbackOnLastTick,
                float armorStandTargetCommandRadius,
                float armorStandPullStrength,
                boolean armorStandInvertPull,
                String armorStandPower,
                String[] armorStandFirstTickCommands,
                String[] armorStandCommands,
                String[] armorStandLastTickCommands,
                String[] armorStandTargetFirstTickCommands,
                String[] armorStandTargetCommands,
                String[] armorStandTargetLastTickCommands,
                String armorStandLoopingSound,
                float armorStandLoopingSoundVolume,
                float armorStandLoopingSoundPitch,
                boolean armorStandDestroyBlocks,
                float armorStandDestroyBlockRadius
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
            this.commandsOnTargets = commandsOnTargets;
            this.commandsOnAllies = commandsOnAllies;
            this.maxWallThickness = maxWallThickness;

            this.spawnArmorStand = spawnArmorStand;
            this.armorStandInterval = armorStandInterval;
            this.armorStandLifetime = armorStandLifetime;
            this.armorStandAoeDamage = armorStandAoeDamage;
            this.armorStandAoeRadius = armorStandAoeRadius;
            this.armorStandAoeDamageType = armorStandAoeDamageType;
            this.armorStandEnableDamage = armorStandEnableDamage;
            this.armorStandDamageOnLastTick = armorStandDamageOnLastTick;
            this.armorStandKnockbackOnLastTick = armorStandKnockbackOnLastTick;
            this.armorStandTargetCommandRadius = armorStandTargetCommandRadius;
            this.armorStandPullStrength = armorStandPullStrength;
            this.armorStandInvertPull = armorStandInvertPull;
            this.armorStandPower = armorStandPower;
            this.armorStandFirstTickCommands = armorStandFirstTickCommands;
            this.armorStandCommands = armorStandCommands;
            this.armorStandLastTickCommands = armorStandLastTickCommands;
            this.armorStandTargetFirstTickCommands = armorStandTargetFirstTickCommands;
            this.armorStandTargetCommands = armorStandTargetCommands;
            this.armorStandTargetLastTickCommands = armorStandTargetLastTickCommands;
            this.armorStandLoopingSound = armorStandLoopingSound;
            this.armorStandLoopingSoundVolume = armorStandLoopingSoundVolume;
            this.armorStandLoopingSoundPitch = armorStandLoopingSoundPitch;
            this.armorStandDestroyBlocks = armorStandDestroyBlocks;
            this.armorStandDestroyBlockRadius = armorStandDestroyBlockRadius;
        }

        private boolean tick() {
            if (caster == null || !caster.isAlive()) {
                return false;
            }

            age++;

            float progress = Math.min((float) age / travelTime, 1.0F);
            double currentDistance = range * progress;

            Vec3 center = origin.add(direction.scale(currentDistance));

            if (maxWallThickness >= 0
                    && exceedsWallThickness(origin, center, caster, maxWallThickness)) {
                return false;
            }

            if (spawnArmorStand && age % armorStandInterval == 0) {
                spawnArmorStandAt(center);
            }

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
                if (target == caster || !target.isAlive()) {
                    continue;
                }

                if (hitEntities.contains(target.getUUID())) {
                    continue;
                }

                Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);

                if (!isInsideLine(origin, direction, targetPos, currentDistance, width)) {
                    continue;
                }

                boolean ally = isAlly(caster, target);

                if (ally) {
                    runCommandsAs(target, commandsOnAllies);
                } else {
                    if (!EOPTargeting.isValidTarget(caster, target)) {
                        continue;
                    }

                    target.hurt(source, damage);
                    runCommandsAs(target, commandsOnTargets);
                }

                hitEntities.add(target.getUUID());
            }

            return age < travelTime;
        }

        private void spawnArmorStandAt(Vec3 position) {
            if (!(caster.level() instanceof ServerLevel level)) {
                return;
            }

            var armorStand = EOPArmorStandSpawner.spawnBasic(
                    caster,
                    level,
                    position,
                    caster.getYRot()
            );

            EOPArmorStandSpawner.applyCommonData(
                    armorStand,
                    armorStandLifetime,
                    armorStandAoeDamage,
                    armorStandAoeRadius,
                    armorStandAoeDamageType,
                    armorStandEnableDamage,
                    armorStandDamageOnLastTick,
                    armorStandKnockbackOnLastTick,
                    armorStandTargetCommandRadius,
                    armorStandPullStrength,
                    armorStandInvertPull,
                    armorStandPower,
                    armorStandFirstTickCommands,
                    armorStandCommands,
                    armorStandLastTickCommands,
                    armorStandTargetFirstTickCommands,
                    armorStandTargetCommands,
                    armorStandTargetLastTickCommands,
                    armorStandLoopingSound,
                    armorStandLoopingSoundVolume,
                    armorStandLoopingSoundPitch,
                    armorStandDestroyBlocks,
                    armorStandDestroyBlockRadius
            );

            EOPForgeEvents.runStandCommands(
                    armorStand,
                    "EOPStandFirstTickCommands"
            );
        }

        private static boolean exceedsWallThickness(
                Vec3 start,
                Vec3 end,
                LivingEntity caster,
                int allowedThickness
        ) {
            var level = caster.level();

            Vec3 direction = end.subtract(start).normalize();
            double distance = start.distanceTo(end);

            int solidBlocksInRow = 0;
            int maxSolidBlocks = 0;

            for (double d = 0; d <= distance; d += 0.25D) {
                Vec3 pos = start.add(direction.scale(d));
                var blockPos = net.minecraft.core.BlockPos.containing(pos);

                boolean solid = !level.getBlockState(blockPos).isAir()
                        && level.getBlockState(blockPos)
                        .isCollisionShapeFullBlock(level, blockPos);

                if (solid) {
                    solidBlocksInRow++;
                    maxSolidBlocks = Math.max(maxSolidBlocks, solidBlocksInRow);
                } else {
                    solidBlocksInRow = 0;
                }
            }

            return maxSolidBlocks > allowedThickness;
        }

        private void spawnTravelParticle(Vec3 center) {
            if (particle == null || particle.isEmpty()) {
                return;
            }

            if (!(caster.level() instanceof ServerLevel serverLevel)) {
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

    private static boolean isAlly(LivingEntity source, LivingEntity target) {
        if (source.isAlliedTo(target) || target.isAlliedTo(source)) {
            return true;
        }

        if (target instanceof TamableAnimal pet) {
            return pet.isOwnedBy(source);
        }

        if (source instanceof TamableAnimal sourcePet) {
            LivingEntity owner = sourcePet.getOwner();

            if (owner != null && target.isAlliedTo(owner)) {
                return true;
            }

            if (target instanceof TamableAnimal targetPet
                    && owner != null
                    && targetPet.isOwnedBy(owner)) {
                return true;
            }
        }

        return false;
    }

    private static void runCommandsAs(LivingEntity executor, String[] commands) {
        if (commands == null || commands.length == 0 || executor.level().isClientSide) {
            return;
        }

        if (!(executor.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CommandSourceStack sourceStack = executor
                .createCommandSourceStack()
                .withLevel(serverLevel)
                .withPermission(2)
                .withSuppressedOutput();

        for (String command : commands) {
            if (command == null || command.isBlank()) {
                continue;
            }

            String cleanedCommand = command.startsWith("/")
                    ? command.substring(1)
                    : command;

            serverLevel.getServer()
                    .getCommands()
                    .performPrefixedCommand(sourceStack, cleanedCommand);
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
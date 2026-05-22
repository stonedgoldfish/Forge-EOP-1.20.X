package net.stonedgoldfish.eopmod.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.power.EOPPowerConstants;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
import net.stonedgoldfish.eopmod.util.EOPTargeting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.stonedgoldfish.eopmod.power.ability.NoInteractionAbility;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EOPForgeEvents {

    private static final Map<UUID, Set<UUID>> ARMOR_STAND_TARGETS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        EOPPalladiumProperties.setClimbExtra(player, player.getTags().contains("EOP.Extra.Climb"));
        EOPPalladiumProperties.setNightVisionExtra(player, player.getTags().contains("EOP.Extra.Night.Vision"));
        EOPPalladiumProperties.setSmeltingExtra(player, player.getTags().contains("EOP.Extra.Smelting"));
        EOPPalladiumProperties.setFireResistanceExtra(player, player.getTags().contains("EOP.Extra.Fire.Resistance"));
        EOPPalladiumProperties.setEntitySenseExtra(player, player.getTags().contains("EOP.Extra.Entity.Sense"));
        EOPPalladiumProperties.setSuperJumpExtra(player, player.getTags().contains("EOP.Extra.Super.Jump"));
        EOPPalladiumProperties.setEraseExtra(player, player.getTags().contains("EOP.Extra.Erase"));
        EOPPalladiumProperties.setExtraReachExtra(player, player.getTags().contains("EOP.Extra.Extra.Reach"));
        EOPPalladiumProperties.setSlowFallExtra(player, player.getTags().contains("EOP.Extra.Slow.Fall"));
        EOPPalladiumProperties.setLightExtra(player, player.getTags().contains("EOP.Extra.Light"));
        EOPPalladiumProperties.setWaterBreathingExtra(player, player.getTags().contains("EOP.Extra.Water.Breathing"));
        EOPPalladiumProperties.setFrostWalkerExtra(player, player.getTags().contains("EOP.Extra.Frost.Walker"));

        if (!CustomFlightAbility.hasCustomFlight(player)) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05F);
                player.onUpdateAbilities();
            }

            return;
        }

        player.getAbilities().mayfly = true;
        player.getAbilities().setFlyingSpeed(0.0F);
        player.onUpdateAbilities();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        float mobMaxHealth = event.getEntity().getMaxHealth();

        int xpGain = Math.round(mobMaxHealth / 2.0F);
        xpGain = Math.max(1, xpGain);
        xpGain = Math.min(50, xpGain);

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            String powerKey = power.key();

            if (!hasEopPower(player, powerKey)) {
                continue;
            }

            int currentXp = EOPPalladiumProperties.getXp(player, powerKey);
            int currentLevel = EOPPalladiumProperties.getLevel(player, powerKey);

            int maxXp = EOPPowerConstants.getMaxXpForLevel(currentLevel);

            currentXp += xpGain;

            if (currentLevel < EOPPowerConstants.MAX_LEVEL) {
                if (currentXp >= maxXp) {
                    currentXp = 0;
                    currentLevel++;

                    int currentSkillPoints = EOPPalladiumProperties.getSkillPoints(player, powerKey);
                    EOPPalladiumProperties.setSkillPoints(player, powerKey, currentSkillPoints + 1);

                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§6" + power.display().replace("_", " ")
                                            + " leveled up to Level "
                                            + currentLevel + "!"
                            )
                    );
                }
            } else {
                currentXp = maxXp;
            }

            EOPPalladiumProperties.setXp(player, powerKey, currentXp);
            EOPPalladiumProperties.setLevel(player, powerKey, currentLevel);
        }
    }

    @SubscribeEvent
    public static void onStepSound(net.minecraftforge.event.PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) {
            return;
        }

        if (!net.stonedgoldfish.eopmod.power.ability.SilentStepsAbility.hasSilentSteps(player)) {
            return;
        }

        String soundPath = event.getSound().value().getLocation().getPath();

        if (soundPath.endsWith(".step")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onArmorStandTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) {
            return;
        }

        if (armorStand.level().isClientSide) {
            return;
        }

        runStandCommands(armorStand, "EOPStandCommands");

        if (armorStand.getPersistentData().contains("EOPLifetime")) {
            int lifetime = armorStand.getPersistentData().getInt("EOPLifetime") - 1;

            if (lifetime <= 0) {
                runStandCommands(armorStand, "EOPStandLastTickCommands");
                runLastTargetCommandsForStand(armorStand);
                ARMOR_STAND_TARGETS.remove(armorStand.getUUID());
                if (armorStand.getPersistentData().getBoolean("EOPDamageOnLastTick")) {
                    dealArmorStandAOEDamage(armorStand);
                }
                applyArmorStandLastTickKnockback(armorStand);
                armorStand.discard();
                return;
            }

            armorStand.getPersistentData().putInt("EOPLifetime", lifetime);
        }

        if (!armorStand.getPersistentData().contains("EOPCaster")) {
            return;
        }

        if (!(armorStand.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID casterUuid = armorStand.getPersistentData().getUUID("EOPCaster");
        Entity casterEntity = serverLevel.getEntity(casterUuid);

        if (!(casterEntity instanceof LivingEntity caster)) {
            return;
        }

        float damage = armorStand.getPersistentData().getFloat("EOPAOEDamage");
        boolean enableDamage =
                armorStand.getPersistentData().getBoolean("EOPEnableDamage");
        boolean damageOnLastTick =
                armorStand.getPersistentData().getBoolean("EOPDamageOnLastTick");
        float radius = armorStand.getPersistentData().getFloat("EOPAOERadius");
        float pullStrength = armorStand.getPersistentData().getFloat("EOPPullStrength");
        boolean invertPull = armorStand.getPersistentData().getBoolean("EOPInvertPull");
        float targetCommandRadius =
                armorStand.getPersistentData().getFloat("EOPTargetCommandRadius");
        String damageType = armorStand.getPersistentData().getString("EOPAOEDamageType");

        String targetFirstCommands = armorStand.getPersistentData().getString("EOPTargetFirstTickCommands");
        String targetCommands = armorStand.getPersistentData().getString("EOPTargetCommands");
        String targetLastCommands = armorStand.getPersistentData().getString("EOPTargetLastTickCommands");

        Set<UUID> previousTargets = ARMOR_STAND_TARGETS.computeIfAbsent(
                armorStand.getUUID(),
                uuid -> new HashSet<>()
        );

        Set<UUID> currentTargets = new HashSet<>();

        DamageSource source = createDamageSource(caster, damageType);

        for (LivingEntity target : armorStand.level().getEntitiesOfClass(
                LivingEntity.class,
                armorStand.getBoundingBox().inflate(radius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            if (enableDamage && !damageOnLastTick && damage > 0.0F) {
                target.hurt(source, damage);
            }

            if (pullStrength > 0.0F) {
                net.minecraft.world.phys.Vec3 direction = armorStand.position()
                        .add(0.0D, armorStand.getBbHeight() * 0.5D, 0.0D)
                        .subtract(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D));

                if (direction.lengthSqr() > 0.001D) {
                    direction = direction.normalize();

                    if (invertPull) {
                        direction = direction.scale(-1.0D);
                    }

                    target.setDeltaMovement(
                            target.getDeltaMovement().add(direction.scale(pullStrength))
                    );

                    target.hurtMarked = true;
                }
            }
        }

        for (LivingEntity target : armorStand.level().getEntitiesOfClass(
                LivingEntity.class,
                armorStand.getBoundingBox().inflate(targetCommandRadius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            currentTargets.add(target.getUUID());

            if (!previousTargets.contains(target.getUUID())) {
                runTargetCommands(target, targetFirstCommands);
            }

            runTargetCommands(target, targetCommands);
        }

        for (UUID oldTargetId : previousTargets) {
            if (currentTargets.contains(oldTargetId)) {
                continue;
            }

            Entity oldEntity = serverLevel.getEntity(oldTargetId);

            if (oldEntity instanceof LivingEntity oldLiving) {
                runTargetCommands(oldLiving, targetLastCommands);
            }
        }

        ARMOR_STAND_TARGETS.put(armorStand.getUUID(), currentTargets);
    }

    private static boolean cannotInteract(net.minecraft.world.entity.player.Player player) {
        return NoInteractionAbility.isBlocked(player)
                || player.hasEffect(net.stonedgoldfish.eopmod.effect.EOPEffects.STUN.get());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (NoInteractionAbility.blocksBlocks(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (NoInteractionAbility.blocksItems(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (NoInteractionAbility.blocksEntities(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (NoInteractionAbility.blocksBlocks(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        if (NoInteractionAbility.blocksEntities(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static boolean hasEopPower(ServerPlayer player, String powerKey) {
        ResourceLocation powerId = ResourceLocation.fromNamespaceAndPath("eop", powerKey);

        return net.threetag.palladium.power.SuperpowerUtil.hasSuperpower(player, powerId);
    }

    private static void applyArmorStandLastTickKnockback(ArmorStand armorStand) {
        float strength = armorStand.getPersistentData().getFloat("EOPKnockbackOnLastTick");

        if (strength <= 0.0F) {
            return;
        }

        if (!armorStand.getPersistentData().contains("EOPCaster")) {
            return;
        }

        if (!(armorStand.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID casterUuid = armorStand.getPersistentData().getUUID("EOPCaster");
        Entity casterEntity = serverLevel.getEntity(casterUuid);

        if (!(casterEntity instanceof LivingEntity caster)) {
            return;
        }

        float radius = armorStand.getPersistentData().getFloat("EOPAOERadius");

        Vec3 center = armorStand.position().add(0.0D, armorStand.getBbHeight() * 0.5D, 0.0D);

        for (LivingEntity target : armorStand.level().getEntitiesOfClass(
                LivingEntity.class,
                armorStand.getBoundingBox().inflate(radius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 direction = targetPos.subtract(center);

            if (direction.lengthSqr() < 0.001D) {
                direction = new Vec3(0.0D, 0.2D, 0.0D);
            } else {
                direction = direction.normalize();
            }

            target.setDeltaMovement(
                    target.getDeltaMovement().add(direction.scale(strength))
            );

            target.hurtMarked = true;
        }
    }

    private static void dealArmorStandAOEDamage(ArmorStand armorStand) {
        if (!armorStand.getPersistentData().contains("EOPCaster")) {
            return;
        }

        if (!(armorStand.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID casterUuid = armorStand.getPersistentData().getUUID("EOPCaster");
        Entity casterEntity = serverLevel.getEntity(casterUuid);

        if (!(casterEntity instanceof LivingEntity caster)) {
            return;
        }

        boolean enableDamage = armorStand.getPersistentData().getBoolean("EOPEnableDamage");

        if (!enableDamage) {
            return;
        }

        float damage = armorStand.getPersistentData().getFloat("EOPAOEDamage");

        if (damage <= 0.0F) {
            return;
        }

        float radius = armorStand.getPersistentData().getFloat("EOPAOERadius");
        String damageType = armorStand.getPersistentData().getString("EOPAOEDamageType");

        DamageSource source = createDamageSource(caster, damageType);

        for (LivingEntity target : armorStand.level().getEntitiesOfClass(
                LivingEntity.class,
                armorStand.getBoundingBox().inflate(radius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            target.hurt(source, damage);
        }
    }

    public static void runStandCommands(ArmorStand armorStand, String key) {
        String rawCommands = armorStand.getPersistentData().getString(key);

        if (rawCommands == null || rawCommands.isEmpty()) {
            return;
        }

        String[] commands = rawCommands.split("\\|\\|");

        for (String command : commands) {
            if (command.isBlank()) {
                continue;
            }

            armorStand.getServer().getCommands().performPrefixedCommand(
                    armorStand.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2),
                    command
            );
        }
    }

    private static void runTargetCommands(LivingEntity target, String rawCommands) {
        if (rawCommands == null || rawCommands.isEmpty()) {
            return;
        }

        String[] commands = rawCommands.split("\\|\\|");

        for (String command : commands) {
            if (command.isBlank()) {
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

    private static void runLastTargetCommandsForStand(ArmorStand armorStand) {
        Set<UUID> previousTargets = ARMOR_STAND_TARGETS.get(armorStand.getUUID());

        if (previousTargets == null || previousTargets.isEmpty()) {
            return;
        }

        String targetLastCommands = armorStand.getPersistentData().getString("EOPTargetLastTickCommands");

        if (!(armorStand.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (UUID targetId : previousTargets) {
            Entity entity = serverLevel.getEntity(targetId);

            if (entity instanceof LivingEntity livingEntity) {
                runTargetCommands(livingEntity, targetLastCommands);
            }
        }
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
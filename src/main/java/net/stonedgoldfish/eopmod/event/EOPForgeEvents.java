package net.stonedgoldfish.eopmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationType;
import net.stonedgoldfish.eopmod.network.DodgePacket;
import net.stonedgoldfish.eopmod.particle.EOPParticles;
import net.stonedgoldfish.eopmod.power.ability.AutoDodgeAbility;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.network.PacketDistributor;
import net.stonedgoldfish.eopmod.effect.EOPEffects;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.stonedgoldfish.eopmod.item.*;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.SyncAttackDamagePacket;
import net.stonedgoldfish.eopmod.power.ability.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.power.EOPPowerConstants;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.stonedgoldfish.eopmod.util.EOPTargeting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraft.world.item.Items;

import java.util.*;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EOPForgeEvents {

    private static final Map<UUID, Set<UUID>> ARMOR_STAND_TARGETS = new HashMap<>();

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {

        if (isNaturallyImmune(
                event.getEntity(),
                event.getEffectInstance().getEffect()
        )) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (ImmuneToEffectAbility.isImmuneTo(
                event.getEntity(),
                event.getEffectInstance().getEffect()
        )) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onFracturedHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) {
            return;
        }

        if (!entity.hasEffect(EOPEffects.FRACTURED.get())) {
            return;
        }

        entity.removeEffect(EOPEffects.FRACTURED.get());

        entity.hurt(
                entity.damageSources().explosion(null, null),
                12.0F
        );

        entity.addEffect(new MobEffectInstance(
                EOPEffects.SILENCED.get(),
                100,
                0,
                false,
                false,
                true
        ));

        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                100,
                2,
                false,
                false,
                true
        ));

        if (entity.level() instanceof ServerLevel serverLevel) {
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() * 0.8D;
            double z = entity.getZ();

            serverLevel.sendParticles(
                    EOPParticles.VOID_ENERGY.get(),
                    x,
                    y,
                    z,
                    200,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.45D
            );
        }
        entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                1.5F,
                1.2F
        );

        entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                2.0F,
                0.5F
        );
    }

    private static boolean isNaturallyImmune(LivingEntity entity, MobEffect effect) {

        if (entity.getMobType() == MobType.UNDEAD) {
            return effect == EOPEffects.BLEED.get();
        }

        if (entity.getType() == EntityType.IRON_GOLEM) {
            return effect == EOPEffects.BLEED.get();
        }

        if (entity.getType() == EntityType.SNOW_GOLEM) {
            return effect == EOPEffects.BLEED.get();
        }

        return false;
    }

    private static EOPAnimationType getRandomDodgeAnimation(LivingEntity living) {
        int choice = living.getRandom().nextInt(3);

        return switch (choice) {
            case 0 -> EOPAnimationType.AUTO_DODGE_1;
            case 1 -> EOPAnimationType.AUTO_DODGE_2;
            default -> EOPAnimationType.AUTO_DODGE_3;
        };
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity living = event.getEntity();

        if (!AutoDodgeAbility.canDodge(living)) {
            return;
        }

        if (event.getSource().getDirectEntity() instanceof Projectile projectile
                && AutoDodgeAbility.isProjectileBlacklisted(living, projectile)) {
            return;
        }

        event.setCanceled(true);

        if (living instanceof ServerPlayer player) {
            EOPNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new DodgePacket(getRandomDodgeAnimation(living))
            );
        }

        living.invulnerableTime = 0;
        living.hurtTime = 0;
        living.hurtDuration = 0;

        for (String command : AutoDodgeAbility.getCommands(living)) {
            if (command == null || command.isBlank()) {
                continue;
            }

            if (living.level().getServer() == null) {
                continue;
            }

            living.level().getServer().getCommands().performPrefixedCommand(
                    living.createCommandSourceStack()
                            .withPermission(4)
                            .withSuppressedOutput(),
                    command
            );
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {

        if (event.getSource().getEntity() instanceof Player caster) {
            boolean projectileHit =
                    event.getSource().getDirectEntity() != null
                            && event.getSource().getDirectEntity() != caster;

            CommandOnPunchAbility.runCommands(
                    caster,
                    event.getEntity(),
                    projectileHit
            );
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        float reduction = DamageReductionAbility.consumeHighestReduction(player);

        if (reduction <= 0.0F) {
            return;
        }

        float multiplier = 1.0F - (reduction / 100.0F);

        event.setAmount(event.getAmount() * multiplier);
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (NoNaturalRegenAbility.hasNoNaturalRegen(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {

        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (entity.tickCount % 20 != 0) {
            return;
        }

        boolean shouldBeLiving =
                !(entity instanceof ArmorStand)
                        && !entity.getTags().contains("EOP.Not.Living");

        Boolean current = EOPPalladiumProperties.LIVING_CREATURE.get(entity);

        if (current == null || current != shouldBeLiving) {
            EOPPalladiumProperties.LIVING_CREATURE.set(entity, shouldBeLiving);
        }

        if (!InvisibilityAbility.shouldClearMobTargets(entity)) {
            return;
        }

        List<Mob> mobs = entity.level().getEntitiesOfClass(
                Mob.class,
                entity.getBoundingBox().inflate(64)
        );

        for (Mob mob : mobs) {
            if (mob.getTarget() == entity) {
                mob.setTarget(null);
            }

            if (mob.getLastHurtByMob() == entity) {
                mob.setLastHurtByMob(null);
            }

            if (mob.getLastHurtMob() == entity) {
                mob.setLastHurtMob(null);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingSetAttackTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();

        if (target != null && InvisibilityAbility.shouldClearMobTargets(target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player != null && NoNaturalRegenAbility.shouldHideHungerBar(minecraft.player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!player.getTags().contains("gills")) {
            return;
        }

        if (player.isEyeInFluidType(net.minecraftforge.common.ForgeMod.WATER_TYPE.get())) {
            event.setCanBreathe(true);
            event.setCanRefillAir(true);
        } else {
            event.setCanBreathe(false);
            event.setCanRefillAir(false);
        }
    }

    private static int countEopPowers(net.minecraft.server.level.ServerPlayer player) {
        var handler = net.threetag.palladium.power.PowerManager
                .getPowerHandler(player)
                .orElse(null);

        if (handler == null) {
            return 0;
        }

        int count = 0;

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            net.minecraft.resources.ResourceLocation id =
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            "eop",
                            power.key()
                    );

            if (handler.getPowerHolders().containsKey(id)) {
                count++;
            }
        }

        return count;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        int currentAmount = countEopPowers(player);
        int storedAmount = EOPPalladiumProperties.getPowerAmount(player);

        if (currentAmount != storedAmount) {
            EOPPalladiumProperties.setPowerAmount(player, currentAmount);
        }

        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        EOPNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncAttackDamagePacket(attackDamage)
        );

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

        if (CustomFlightAbility.hasCustomFlight(player) && CustomFlightAbility.isFlying(player)) {
            player.setNoGravity(true);
            player.fallDistance = 0.0F;
        } else {
            player.setNoGravity(false);
        }
        boolean sprintFlying =
                CustomFlightAbility.hasCustomFlight(player)
                        && CustomFlightAbility.isFlying(player)
                        && player.isSprinting();

        boolean wasSprintFlyingServer =
                player.getPersistentData().getBoolean("EOPSprintFlying");

        if (sprintFlying != wasSprintFlyingServer) {
            player.refreshDimensions();
            player.getPersistentData().putBoolean("EOPSprintFlying", sprintFlying);
        }

    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!isArgonDecayItem(left)) {
            return;
        }

        ItemStack output = left.copy();
        CompoundTag tag = output.getOrCreateTag();

        long maxDecayTime = 216000L;
        long currentGameTime = event.getPlayer().level().getGameTime();

        long leftRemaining = getRemainingDecayTime(left, currentGameTime, maxDecayTime);

        if (right.getItem() == EOPItems.ARGON_CRYSTAL.get()) {
            long repairAmount = 6000L;

            long repairedRemaining = Math.min(maxDecayTime, leftRemaining + repairAmount);
            applyRemainingDecayTime(tag, currentGameTime, maxDecayTime, repairedRemaining);

            event.setOutput(output);
            event.setCost(1);
            event.setMaterialCost(1);
            return;
        }

        if (right.getItem() == left.getItem()) {
            long rightRemaining = getRemainingDecayTime(right, currentGameTime, maxDecayTime);

            long bonusRepair = maxDecayTime / 20L;
            long repairedRemaining = Math.min(maxDecayTime, leftRemaining + rightRemaining + bonusRepair);

            applyRemainingDecayTime(tag, currentGameTime, maxDecayTime, repairedRemaining);

            event.setOutput(output);
            event.setCost(2);
            event.setMaterialCost(1);
        }
    }
    private static boolean isArgonDecayItem(ItemStack stack) {
        return stack.getItem() instanceof ArgonArmorItem
                || stack.getItem() instanceof ArgonSwordItem
                || stack.getItem() instanceof ArgonPickaxeItem
                || stack.getItem() instanceof ArgonAxeItem
                || stack.getItem() instanceof ArgonShovelItem
                || stack.getItem() instanceof ArgonHoeItem;
    }

    private static long getRemainingDecayTime(ItemStack stack, long currentGameTime, long maxDecayTime) {
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            return maxDecayTime;
        }

        long createdTime = tag.getLong("ArgonCreatedTime");
        long decayTime = tag.getLong("ArgonDecayTime");

        if (decayTime <= 0L) {
            decayTime = maxDecayTime;
        }

        long age = currentGameTime - createdTime;

        return Math.max(0L, decayTime - age);
    }

    private static void applyRemainingDecayTime(
            CompoundTag tag,
            long currentGameTime,
            long maxDecayTime,
            long remainingTime
    ) {
        long clampedRemaining = Math.min(maxDecayTime, Math.max(0L, remainingTime));

        long newCreatedTime = currentGameTime - (maxDecayTime - clampedRemaining);

        tag.putLong("ArgonCreatedTime", newCreatedTime);
        tag.putLong("ArgonDecayTime", maxDecayTime);
    }

    @SubscribeEvent
    public static void onMilkBucketUse(PlayerInteractEvent.RightClickItem event) {

        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getItemStack().getItem() != Items.MILK_BUCKET) {
            return;
        }

        if (hasMilkProtectedEffect(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static boolean hasMilkProtectedEffect(Player player) {
        return player.hasEffect(EOPEffects.STUN.get())
                || player.hasEffect(EOPEffects.SNARE.get())
                || player.hasEffect(EOPEffects.SILENCED.get())
                || player.hasEffect(EOPEffects.LUNAR_CLOAK.get());
    }

    @SubscribeEvent
    public static void onLivingVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (InvisibilityAbility.isInvisible(event.getEntity())) {
            event.modifyVisibility(0.0D);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        AreaLightAbility.removeAllLights(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            AreaLightAbility.removeLights(level, event.getEntity().getUUID());
        }
    }
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AreaLightAbility.removeLights(level, event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            AreaLightAbility.removeLights(level, event.getEntity().getUUID());
        }
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

        if (!(casterEntity instanceof LivingEntity caster) || !caster.isAlive()) {
            runStandCommands(armorStand, "EOPStandLastTickCommands");
            runLastTargetCommandsForStand(armorStand);
            ARMOR_STAND_TARGETS.remove(armorStand.getUUID());

            armorStand.discard();
            return;
        }

        if (armorStand.getPersistentData().getBoolean("EOPDestroyBlocks")
                && EOPGameRules.isDestructionMode(armorStand.getServer())) {

            destroyBlocksAroundArmorStand(
                    armorStand,
                    armorStand.getPersistentData().getFloat("EOPDestroyBlockRadius")
            );
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

    private static void destroyBlocksAroundArmorStand(ArmorStand armorStand, float radius) {
        if (radius <= 0.0F) {
            return;
        }

        int blockRadius = (int) Math.ceil(radius);
        BlockPos center = armorStand.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-blockRadius, -blockRadius, -blockRadius),
                center.offset(blockRadius, blockRadius, blockRadius)
        )) {
            if (pos.distSqr(center) > radius * radius) {
                continue;
            }

            var state = armorStand.level().getBlockState(pos);

            if (state.isAir()) {
                continue;
            }

            if (state.getDestroySpeed(armorStand.level(), pos) < 0.0F) {
                continue;
            }

            armorStand.level().destroyBlock(pos, false, armorStand);
        }
    }

    private static boolean cannotInteract(net.minecraft.world.entity.player.Player player) {
        return NoInteractionAbility.isBlocked(player)
                || player.hasEffect(net.stonedgoldfish.eopmod.effect.EOPEffects.STUN.get());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (cannotInteract(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (cannotInteract(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (cannotInteract(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (cannotInteract(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        if (cannotInteract(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    static boolean hasEopPower(ServerPlayer player, String powerKey) {
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

            target.setDeltaMovement(Vec3.ZERO);

            target.setDeltaMovement(direction.scale(strength));

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
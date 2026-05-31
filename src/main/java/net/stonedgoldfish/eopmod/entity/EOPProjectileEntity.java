package net.stonedgoldfish.eopmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.stonedgoldfish.eopmod.util.EOPTargeting;

import java.util.ArrayList;
import java.util.List;

import static net.stonedgoldfish.eopmod.power.ability.SpawnArmorStandAbility.*;

public class EOPProjectileEntity extends ThrowableProjectile implements ItemSupplier {

    private static final EntityDataAccessor<ItemStack> APPEARANCE_ITEM =
            SynchedEntityData.defineId(EOPProjectileEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final EntityDataAccessor<String> RENDER_LAYERS =
            SynchedEntityData.defineId(EOPProjectileEntity.class, EntityDataSerializers.STRING);

    private static ListTag stringArrayToListTag(String[] array) {
        ListTag list = new ListTag();

        if (array == null) {
            return list;
        }

        for (String value : array) {
            if (value != null) {
                list.add(StringTag.valueOf(value));
            }
        }

        return list;
    }

    private static String[] readStringArray(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return new String[]{};
        }

        ListTag list = tag.getList(key, Tag.TAG_STRING);
        String[] result = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            result[i] = list.getString(i);
        }

        return result;
    }

    private float damage = 6.0F;
    private String damageType = "minecraft:generic";
    private int lifetime = 100;
    private float gravity = 0.0F;
    private boolean dieOnEntityHit = true;
    private boolean dieOnBlockHit = true;
    private boolean ignoreLiquids = false;

    private int setEntityOnFireSeconds = 0;
    private float explosionRadius = 0.0F;
    private boolean explosionCausesFire = false;
    private String explosionBlockInteraction = "KEEP";
    private float knockbackStrength = 0.0F;
    private String[] commandsOnBlockHit = new String[]{};
    private String[] commandsOnEntityHit = new String[]{};
    private String[] commandsForTargets = new String[]{};
    private float commandsForTargetsRadius = 0.0F;

    private boolean spawnArmorStandOnBlockHit = false;
    private boolean spawnArmorStandOnEntityHit = false;
    private int armorStandLifetime = 100;
    private float armorStandAoeDamage = 0.0F;
    private float armorStandAoeRadius = 3.0F;
    private String armorStandAoeDamageType = "minecraft:magic";
    private boolean armorStandEnableDamage = true;
    private boolean armorStandDamageOnLastTick = false;
    private float armorStandKnockbackOnLastTick = 0.0F;
    private float armorStandTargetCommandRadius = 3.0F;
    private float armorStandPullStrength = 0.0F;
    private boolean armorStandInvertPull = false;
    private String armorStandPower = "";
    private String[] armorStandFirstTickCommands = new String[]{};
    private String[] armorStandCommands = new String[]{};
    private String[] armorStandLastTickCommands = new String[]{};
    private String[] armorStandTargetFirstTickCommands = new String[]{};
    private String[] armorStandTargetCommands = new String[]{};
    private String[] armorStandTargetLastTickCommands = new String[]{};
    private String armorStandLoopingSound = "";
    private float armorStandLoopingSoundVolume = 1.0F;
    private float armorStandLoopingSoundPitch = 1.0F;
    private boolean armorStandDestroyBlocks = false;
    private float armorStandDestroyBlockRadius = 0.0F;

    private ListTag appearances = new ListTag();

    public EOPProjectileEntity(EntityType<? extends EOPProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(APPEARANCE_ITEM, ItemStack.EMPTY);
        this.entityData.define(RENDER_LAYERS, "");
    }

    @Override
    public ItemStack getItem() {
        return this.entityData.get(APPEARANCE_ITEM);
    }

    public List<ResourceLocation> getRenderLayers() {
        List<ResourceLocation> layers = new ArrayList<>();

        String raw = this.entityData.get(RENDER_LAYERS);

        if (raw == null || raw.isBlank()) {
            return layers;
        }

        for (String entry : raw.split("\\|\\|")) {
            ResourceLocation id = ResourceLocation.tryParse(entry);

            if (id != null) {
                layers.add(id);
            }
        }

        return layers;
    }

    @Override
    public void tick() {
        Vec3 motionBeforeTick = this.getDeltaMovement();

        super.tick();

        if (ignoreLiquids && (this.isInWaterOrBubble() || this.isInLava())) {
            this.setDeltaMovement(motionBeforeTick);
        }

        spawnAppearanceParticles();

        if (!this.level().isClientSide && this.tickCount >= lifetime) {
            this.discard();
        }
    }

    @Override
    protected float getGravity() {
        return gravity;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide) {
            return;
        }

        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();
        if (hitEntity instanceof EnderDragonPart dragonPart) {
            Entity parent = dragonPart.getParent();

            if (parent instanceof LivingEntity dragon) {
                dragon.hurt(createDamageSource(owner instanceof LivingEntity caster ? caster : dragon), this.damage);

                runCommands(commandsOnEntityHit);
                createFilteredExplosion();
                runCommandsForTargets();

                if (dieOnEntityHit) {
                    if (spawnArmorStandOnEntityHit) {
                        spawnArmorStandFromProjectile();
                    }
                    this.discard();
                }
            }

            return;
        }

        if (!(hitEntity instanceof LivingEntity target)) {
            if (dieOnEntityHit) {
                createFilteredExplosion();
                runCommands(commandsOnEntityHit);
                if (spawnArmorStandOnEntityHit) {
                    spawnArmorStandFromProjectile();
                }
                this.discard();
            }
            return;
        }

        if (!(owner instanceof LivingEntity caster)) {
            if (dieOnEntityHit) {
                createFilteredExplosion();
                runCommands(commandsOnEntityHit);
                if (spawnArmorStandOnEntityHit) {
                    spawnArmorStandFromProjectile();
                }
                this.discard();
            }
            return;
        }

        if (!EOPTargeting.isValidTarget(caster, target)) {
            return;
        }

        target.hurt(createDamageSource(caster), this.damage);

        if (setEntityOnFireSeconds > 0) {
            target.setSecondsOnFire(setEntityOnFireSeconds);
        }

        applyKnockback(target);

        runCommands(commandsOnEntityHit);
        createFilteredExplosion();
        runCommandsForTargets();

        if (dieOnEntityHit) {
            if (spawnArmorStandOnEntityHit) {
                spawnArmorStandFromProjectile();
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level().isClientSide) {
            return;
        }

        runCommands(commandsOnBlockHit);
        createFilteredExplosion();
        runCommandsForTargets();

        if (dieOnBlockHit) {
            if (spawnArmorStandOnBlockHit) {
                spawnArmorStandFromProjectile();
            }
            this.discard();
        }
    }

    private void spawnArmorStandFromProjectile() {
        Entity owner = this.getOwner();

        if (!(owner instanceof LivingEntity caster)) {
            return;
        }

        net.minecraft.world.entity.decoration.ArmorStand armorStand =
                net.stonedgoldfish.eopmod.util.EOPArmorStandSpawner.spawnBasic(
                        caster,
                        this.level(),
                        this.position(),
                        this.getYRot()
                );

        net.stonedgoldfish.eopmod.util.EOPArmorStandSpawner.applyCommonData(
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
        ResourceLocation sound = ResourceLocation.tryParse(armorStandLoopingSound);

        if (sound != null && !armorStandLoopingSound.isEmpty()) {
            net.stonedgoldfish.eopmod.network.EOPNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> armorStand),
                    new net.stonedgoldfish.eopmod.network.PlayArmorStandLoopingSoundPacket(
                            armorStand.getId(),
                            sound,
                            armorStandLoopingSoundVolume,
                            armorStandLoopingSoundPitch
                    )
            );
        }
        net.stonedgoldfish.eopmod.event.EOPForgeEvents.runStandCommands(
                armorStand,
                "EOPStandFirstTickCommands"
        );
    }

    private void spawnAppearanceParticles() {
        if (this.level().isClientSide) {
            return;
        }

        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        if (appearances == null || appearances.isEmpty()) {
            return;
        }

        for (int i = 0; i < appearances.size(); i++) {
            CompoundTag tag = appearances.getCompound(i);

            if (!tag.getString("Type").equals("particles")) {
                continue;
            }

            ResourceLocation particleId = ResourceLocation.tryParse(tag.getString("ParticleType"));

            var particleType = particleId != null
                    ? BuiltInRegistries.PARTICLE_TYPE.get(particleId)
                    : ParticleTypes.FLAME;

            if (!(particleType instanceof SimpleParticleType simpleParticleType)) {
                continue;
            }

            int amount = tag.contains("Amount") ? tag.getInt("Amount") : 1;
            float spread = tag.contains("Spread") ? tag.getFloat("Spread") : 0.1F;
            float speed = tag.contains("Speed") ? tag.getFloat("Speed") : 0.02F;

            serverLevel.sendParticles(
                    simpleParticleType,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    amount,
                    spread,
                    spread,
                    spread,
                    speed
            );
        }
    }

    private void readAppearances(CompoundTag tag) {
        if (!tag.contains("Appearances", Tag.TAG_LIST)) {
            return;
        }

        this.appearances = tag.getList("Appearances", Tag.TAG_COMPOUND);

        List<String> syncedLayers = new ArrayList<>();

        for (int i = 0; i < appearances.size(); i++) {
            CompoundTag appearanceTag = appearances.getCompound(i);
            String type = appearanceTag.getString("Type");

            if (type.equals("item")) {
                Tag itemTag = appearanceTag.get("Item");

                if (itemTag instanceof CompoundTag compoundTag) {
                    this.entityData.set(APPEARANCE_ITEM, ItemStack.of(compoundTag));
                } else if (itemTag instanceof StringTag stringTag) {
                    ResourceLocation itemId = ResourceLocation.tryParse(stringTag.getAsString());

                    if (itemId != null) {
                        this.entityData.set(
                                APPEARANCE_ITEM,
                                new ItemStack(BuiltInRegistries.ITEM.get(itemId))
                        );
                    }
                }
            }

            if (type.equals("renderLayer")) {
                Tag layerTag = appearanceTag.get("RenderLayer");

                if (layerTag instanceof StringTag stringTag) {
                    ResourceLocation layer = ResourceLocation.tryParse(stringTag.getAsString());

                    if (layer != null) {
                        syncedLayers.add(layer.toString());
                    }
                } else if (layerTag instanceof ListTag listTag) {
                    for (Tag layerEntry : listTag) {
                        if (layerEntry instanceof StringTag stringTag) {
                            ResourceLocation layer = ResourceLocation.tryParse(stringTag.getAsString());

                            if (layer != null) {
                                syncedLayers.add(layer.toString());
                            }
                        }
                    }
                }
            }
        }

        this.entityData.set(RENDER_LAYERS, String.join("||", syncedLayers));
    }

    private void createFilteredExplosion() {
        if (explosionRadius <= 0.0F) {
            return;
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                Math.min(4.0F, explosionRadius / 2.0F),
                1.0F
        );

        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    Math.max(1, (int) (explosionRadius * 4)),
                    explosionRadius * 0.35D,
                    explosionRadius * 0.35D,
                    explosionRadius * 0.35D,
                    0.08D
            );
        }

        if (explosionCausesFire && EOPGameRules.isDestructionMode(this.level().getServer())) {
            placeFireAroundExplosion();
        }

        handleExplosionBlocks();
        handleExplosionKnockback();
        handleExplosionDamage();
    }

    private void handleExplosionDamage() {
        Entity owner = this.getOwner();

        if (!(owner instanceof LivingEntity caster)) {
            return;
        }

        float explosionDamage = explosionRadius * 2.0F;

        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(explosionRadius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            double distance = target.distanceTo(this);
            double falloff = 1.0D - Math.min(distance / explosionRadius, 1.0D);

            float finalDamage = (float) (explosionDamage * falloff);

            if (finalDamage > 0.0F) {
                target.hurt(createDamageSource(caster), finalDamage);
            }
        }
    }

    private void handleExplosionKnockback() {
        if (knockbackStrength <= 0.0F) {
            return;
        }

        Entity owner = this.getOwner();

        if (!(owner instanceof LivingEntity caster)) {
            return;
        }

        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(explosionRadius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            applyKnockback(target);
        }
    }

    private void applyKnockback(LivingEntity target) {
        if (knockbackStrength <= 0.0F) {
            return;
        }

        Vec3 direction = target.position()
                .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(this.position());

        if (direction.lengthSqr() < 0.001D) {
            direction = new Vec3(0.0D, 0.2D, 0.0D);
        } else {
            direction = direction.normalize();
        }

        target.setDeltaMovement(
                target.getDeltaMovement().add(direction.scale(knockbackStrength))
        );

        target.hurtMarked = true;
    }

    private void handleExplosionBlocks() {
        String mode = explosionBlockInteraction == null
                ? "KEEP"
                : explosionBlockInteraction.toUpperCase();

        if (!EOPGameRules.isDestructionMode(this.level().getServer())) {
            mode = "KEEP";
        }

        if (mode.equals("KEEP")) {
            return;
        }

        boolean dropBlocks = mode.equals("BREAK");

        int radius = (int) Math.ceil(explosionRadius);
        BlockPos center = this.blockPosition();

        java.util.Random random = new java.util.Random();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            double distance = Math.sqrt(pos.distSqr(center));

            if (distance > explosionRadius) {
                continue;
            }

            var state = this.level().getBlockState(pos);

            if (state.isAir()) {
                continue;
            }

            if (state.getDestroySpeed(this.level(), pos) < 0.0F) {
                continue;
            }

            double norm = distance / explosionRadius;

            double breakChance = 1.0D - (norm * norm);
            breakChance += (random.nextDouble() - 0.5D) * (norm * 0.8D);

            if (breakChance < 0.15D) {
                continue;
            }

            if (random.nextDouble() <= breakChance) {
                this.level().destroyBlock(pos, dropBlocks, this);
            }
        }
    }

    private void placeFireAroundExplosion() {
        int radius = (int) Math.ceil(explosionRadius);
        BlockPos center = this.blockPosition();

        java.util.Random random = new java.util.Random();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius),
                center.offset(radius, 1, radius)
        )) {
            if (random.nextFloat() > 0.25F) {
                continue;
            }

            if (pos.distSqr(center) > explosionRadius * explosionRadius) {
                continue;
            }

            BlockPos firePos = pos.above();

            if (!this.level().getBlockState(firePos).isAir()) {
                continue;
            }

            if (this.level().getBlockState(pos).isAir()) {
                continue;
            }

            this.level().setBlockAndUpdate(
                    firePos,
                    net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState()
            );
        }
    }

    private void runCommandsForTargets() {
        if (commandsForTargets == null || commandsForTargets.length == 0) {
            return;
        }

        Entity owner = this.getOwner();

        if (!(owner instanceof LivingEntity caster)) {
            return;
        }

        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(commandsForTargetsRadius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            runCommandsAsTarget(target, commandsForTargets);
        }
    }

    private void runCommandsAsTarget(LivingEntity target, String[] commands) {
        if (commands == null || commands.length == 0 || target.getServer() == null) {
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

    private void runCommands(String[] commands) {
        if (commands == null || commands.length == 0) {
            return;
        }

        if (this.getServer() == null) {
            return;
        }

        for (String command : commands) {
            if (command == null || command.isBlank()) {
                continue;
            }

            this.getServer().getCommands().performPrefixedCommand(
                    this.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2),
                    command
            );
        }
    }

    private DamageSource createDamageSource(LivingEntity caster) {
        ResourceLocation damageLocation = ResourceLocation.tryParse(this.damageType);

        if (damageLocation == null) {
            return caster.damageSources().generic();
        }

        ResourceKey<DamageType> damageTypeKey = ResourceKey.create(
                Registries.DAMAGE_TYPE,
                damageLocation
        );

        return new DamageSource(
                caster.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(damageTypeKey),
                this,
                caster
        );
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", this.damage);
        tag.putString("DamageType", this.damageType);
        tag.putInt("Lifetime", this.lifetime);
        tag.putFloat("Gravity", this.gravity);
        tag.putBoolean("DieOnEntityHit", this.dieOnEntityHit);
        tag.putBoolean("DieOnBlockHit", this.dieOnBlockHit);
        tag.putBoolean("IgnoreLiquids", this.ignoreLiquids);

        tag.putInt("SetEntityOnFireSeconds", this.setEntityOnFireSeconds);
        tag.putFloat("ExplosionRadius", this.explosionRadius);
        tag.putBoolean("ExplosionCausesFire", this.explosionCausesFire);
        tag.putString("ExplosionBlockInteraction", this.explosionBlockInteraction);
        tag.putFloat("KnockbackStrength", this.knockbackStrength);
        ListTag targetCommands = new ListTag();
        tag.putFloat("CommandsForTargetsRadius", this.commandsForTargetsRadius);
        for (String command : this.commandsForTargets) {
            targetCommands.add(StringTag.valueOf(command));
        }
        tag.put("CommandsForTargets", targetCommands);
        ListTag blockCommands = new ListTag();
        for (String command : this.commandsOnBlockHit) {
            blockCommands.add(StringTag.valueOf(command));
        }
        tag.put("CommandsOnBlockHit", blockCommands);

        ListTag entityCommands = new ListTag();
        for (String command : this.commandsOnEntityHit) {
            entityCommands.add(StringTag.valueOf(command));
        }
        tag.put("CommandsOnEntityHit", entityCommands);

        tag.put("Appearances", this.appearances);

        tag.putBoolean("SpawnArmorStandOnBlockHit", this.spawnArmorStandOnBlockHit);
        tag.putBoolean("SpawnArmorStandOnEntityHit", this.spawnArmorStandOnEntityHit);

        tag.putInt("ArmorStandLifetime", this.armorStandLifetime);
        tag.putFloat("ArmorStandAOEDamage", this.armorStandAoeDamage);
        tag.putFloat("ArmorStandAOERadius", this.armorStandAoeRadius);
        tag.putString("ArmorStandAOEDamageType", this.armorStandAoeDamageType);
        tag.putBoolean("ArmorStandEnableDamage", this.armorStandEnableDamage);
        tag.putBoolean("ArmorStandDamageOnLastTick", this.armorStandDamageOnLastTick);
        tag.putFloat("ArmorStandKnockbackOnLastTick", this.armorStandKnockbackOnLastTick);
        tag.putFloat("ArmorStandTargetCommandRadius", this.armorStandTargetCommandRadius);
        tag.putFloat("ArmorStandPullStrength", this.armorStandPullStrength);
        tag.putBoolean("ArmorStandInvertPull", this.armorStandInvertPull);
        tag.putString("ArmorStandPower", this.armorStandPower);

        tag.put("ArmorStandFirstTickCommands", stringArrayToListTag(this.armorStandFirstTickCommands));
        tag.put("ArmorStandCommands", stringArrayToListTag(this.armorStandCommands));
        tag.put("ArmorStandLastTickCommands", stringArrayToListTag(this.armorStandLastTickCommands));

        tag.put("ArmorStandTargetFirstTickCommands", stringArrayToListTag(this.armorStandTargetFirstTickCommands));
        tag.put("ArmorStandTargetCommands", stringArrayToListTag(this.armorStandTargetCommands));
        tag.put("ArmorStandTargetLastTickCommands", stringArrayToListTag(this.armorStandTargetLastTickCommands));
        tag.putString("ArmorStandLoopingSound", this.armorStandLoopingSound);
        tag.putFloat("ArmorStandLoopingSoundVolume", this.armorStandLoopingSoundVolume);
        tag.putFloat("ArmorStandLoopingSoundPitch", this.armorStandLoopingSoundPitch);
        tag.putBoolean("ArmorStandDestroyBlocks", this.armorStandDestroyBlocks);
        tag.putFloat("ArmorStandDestroyBlockRadius", this.armorStandDestroyBlockRadius);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Damage")) {
            this.damage = tag.getFloat("Damage");
        }

        if (tag.contains("DamageType")) {
            this.damageType = tag.getString("DamageType");
        }

        if (tag.contains("Lifetime")) {
            this.lifetime = tag.getInt("Lifetime");
        }

        if (tag.contains("Gravity")) {
            this.gravity = tag.getFloat("Gravity");
        }

        if (tag.contains("CommandsForTargetsRadius")) {
            this.commandsForTargetsRadius = tag.getFloat("CommandsForTargetsRadius");
        }

        if (tag.contains("DieOnEntityHit")) {
            this.dieOnEntityHit = tag.getBoolean("DieOnEntityHit");
        }

        if (tag.contains("DieOnBlockHit")) {
            this.dieOnBlockHit = tag.getBoolean("DieOnBlockHit");
        }

        if (tag.contains("IgnoreLiquids")) {
            this.ignoreLiquids = tag.getBoolean("IgnoreLiquids");
        }

        if (tag.contains("SetEntityOnFireSeconds")) {
            this.setEntityOnFireSeconds = tag.getInt("SetEntityOnFireSeconds");
        }

        if (tag.contains("ExplosionRadius")) {
            this.explosionRadius = tag.getFloat("ExplosionRadius");
        }

        if (tag.contains("ExplosionCausesFire")) {
            this.explosionCausesFire = tag.getBoolean("ExplosionCausesFire");
        }

        if (tag.contains("ExplosionBlockInteraction")) {
            this.explosionBlockInteraction = tag.getString("ExplosionBlockInteraction");
        }

        if (tag.contains("KnockbackStrength")) {
            this.knockbackStrength = tag.getFloat("KnockbackStrength");
        }

        if (tag.contains("CommandsForTargets", Tag.TAG_LIST)) {
            ListTag list = tag.getList("CommandsForTargets", Tag.TAG_STRING);
            this.commandsForTargets = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                this.commandsForTargets[i] = list.getString(i);
            }
        }

        if (tag.contains("CommandsOnBlockHit", Tag.TAG_LIST)) {
            ListTag list = tag.getList("CommandsOnBlockHit", Tag.TAG_STRING);
            this.commandsOnBlockHit = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                this.commandsOnBlockHit[i] = list.getString(i);
            }
        }

        if (tag.contains("CommandsOnEntityHit", Tag.TAG_LIST)) {
            ListTag list = tag.getList("CommandsOnEntityHit", Tag.TAG_STRING);
            this.commandsOnEntityHit = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                this.commandsOnEntityHit[i] = list.getString(i);
            }
        }

        if (tag.contains("SpawnArmorStandOnBlockHit")) {
            this.spawnArmorStandOnBlockHit = tag.getBoolean("SpawnArmorStandOnBlockHit");
        }

        if (tag.contains("SpawnArmorStandOnEntityHit")) {
            this.spawnArmorStandOnEntityHit = tag.getBoolean("SpawnArmorStandOnEntityHit");
        }

        if (tag.contains("ArmorStandLifetime")) {
            this.armorStandLifetime = tag.getInt("ArmorStandLifetime");
        }

        if (tag.contains("ArmorStandAOEDamage")) {
            this.armorStandAoeDamage = tag.getFloat("ArmorStandAOEDamage");
        }

        if (tag.contains("ArmorStandAOERadius")) {
            this.armorStandAoeRadius = tag.getFloat("ArmorStandAOERadius");
        }

        if (tag.contains("ArmorStandAOEDamageType")) {
            this.armorStandAoeDamageType = tag.getString("ArmorStandAOEDamageType");
        }

        if (tag.contains("ArmorStandEnableDamage")) {
            this.armorStandEnableDamage = tag.getBoolean("ArmorStandEnableDamage");
        }

        if (tag.contains("ArmorStandDamageOnLastTick")) {
            this.armorStandDamageOnLastTick = tag.getBoolean("ArmorStandDamageOnLastTick");
        }

        if (tag.contains("ArmorStandKnockbackOnLastTick")) {
            this.armorStandKnockbackOnLastTick = tag.getFloat("ArmorStandKnockbackOnLastTick");
        }

        if (tag.contains("ArmorStandTargetCommandRadius")) {
            this.armorStandTargetCommandRadius = tag.getFloat("ArmorStandTargetCommandRadius");
        }

        if (tag.contains("ArmorStandPullStrength")) {
            this.armorStandPullStrength = tag.getFloat("ArmorStandPullStrength");
        }

        if (tag.contains("ArmorStandInvertPull")) {
            this.armorStandInvertPull = tag.getBoolean("ArmorStandInvertPull");
        }

        if (tag.contains("ArmorStandPower")) {
            this.armorStandPower = tag.getString("ArmorStandPower");
        }

        this.armorStandFirstTickCommands = readStringArray(tag, "ArmorStandFirstTickCommands");
        this.armorStandCommands = readStringArray(tag, "ArmorStandCommands");
        this.armorStandLastTickCommands = readStringArray(tag, "ArmorStandLastTickCommands");

        this.armorStandTargetFirstTickCommands = readStringArray(tag, "ArmorStandTargetFirstTickCommands");
        this.armorStandTargetCommands = readStringArray(tag, "ArmorStandTargetCommands");
        this.armorStandTargetLastTickCommands = readStringArray(tag, "ArmorStandTargetLastTickCommands");
        if (tag.contains("ArmorStandLoopingSound")) {
            this.armorStandLoopingSound = tag.getString("ArmorStandLoopingSound");
        }
        if (tag.contains("ArmorStandLoopingSoundVolume")) {
            this.armorStandLoopingSoundVolume =
                    tag.getFloat("ArmorStandLoopingSoundVolume");
        }

        if (tag.contains("ArmorStandLoopingSoundPitch")) {
            this.armorStandLoopingSoundPitch =
                    tag.getFloat("ArmorStandLoopingSoundPitch");
        }
        if (tag.contains("ArmorStandDestroyBlocks")) {
            this.armorStandDestroyBlocks = tag.getBoolean("ArmorStandDestroyBlocks");
        }

        if (tag.contains("ArmorStandDestroyBlockRadius")) {
            this.armorStandDestroyBlockRadius = tag.getFloat("ArmorStandDestroyBlockRadius");
        }

        readAppearances(tag);
    }
}
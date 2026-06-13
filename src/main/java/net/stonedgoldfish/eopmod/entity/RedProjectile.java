package net.stonedgoldfish.eopmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.stonedgoldfish.eopmod.util.EOPTargeting;


public class RedProjectile extends ThrowableProjectile {

    private float damage = 8.0F;
    private String damageType = "minecraft:magic";

    private int lifetime = 40;
    private float gravity = 0.0F;

    private boolean dieOnEntityHit = true;
    private boolean dieOnBlockHit = true;
    private boolean ignoreLiquids = false;

    private int setEntityOnFireSeconds = 0;

    private float explosionRadius = 0.0F;
    private boolean explosionCausesFire = false;
    private String explosionBlockInteraction = "KEEP";

    private String[] commandsOnBlockHit = new String[]{};
    private String[] commandsForTargets = new String[]{};
    private float commandsForTargetsRadius = 0.0F;

    public RedProjectile(EntityType<? extends RedProjectile> type, Level level) {
        super(type, level);
    }

    public RedProjectile(Level level, LivingEntity owner) {
        super(EOPEntities.RED.get(), owner, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void setDieOnEntityHit(boolean dieOnEntityHit) {
        this.dieOnEntityHit = dieOnEntityHit;
    }

    public void setDieOnBlockHit(boolean dieOnBlockHit) {
        this.dieOnBlockHit = dieOnBlockHit;
    }

    public void setIgnoreLiquids(boolean ignoreLiquids) {
        this.ignoreLiquids = ignoreLiquids;
    }

    public void setSetEntityOnFireSeconds(int seconds) {
        this.setEntityOnFireSeconds = seconds;
    }

    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    public void setExplosionCausesFire(boolean explosionCausesFire) {
        this.explosionCausesFire = explosionCausesFire;
    }

    public void setExplosionBlockInteraction(String explosionBlockInteraction) {
        this.explosionBlockInteraction = explosionBlockInteraction;
    }

    public void setCommandsOnBlockHit(String[] commandsOnBlockHit) {
        this.commandsOnBlockHit = commandsOnBlockHit == null ? new String[]{} : commandsOnBlockHit;
    }

    public void setCommandsForTargets(String[] commandsForTargets) {
        this.commandsForTargets = commandsForTargets == null ? new String[]{} : commandsForTargets;
    }

    public void setCommandsForTargetsRadius(float commandsForTargetsRadius) {
        this.commandsForTargetsRadius = commandsForTargetsRadius;
    }

    @Override
    public void tick() {
        Vec3 motionBeforeTick = this.getDeltaMovement();

        super.tick();
        checkWideEntityCollision();

        if (this.ignoreLiquids && (this.isInWaterOrBubble() || this.isInLava())) {
            this.setDeltaMovement(motionBeforeTick);
        }

        if (!this.level().isClientSide && this.lifetime > 0 && this.tickCount >= this.lifetime) {
            this.discard();
        }
    }

    private void checkWideEntityCollision() {
        if (this.level().isClientSide) {
            return;
        }

        Entity owner = this.getOwner();

        if (!(owner instanceof LivingEntity caster)) {
            return;
        }

        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(0.25D)
        )) {
            if (target == caster) {
                continue;
            }

            if (!target.isAlive()) {
                continue;
            }

            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            this.onHitEntity(new EntityHitResult(target));
            return;
        }
    }

    @Override
    protected float getGravity() {
        return this.gravity;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide) {
            return;
        }

        Entity hitEntity = result.getEntity();
        Entity owner = this.getOwner();

        if (owner instanceof LivingEntity caster) {
            LivingEntity target = null;

            if (hitEntity instanceof EnderDragonPart dragonPart) {
                target = dragonPart.getParent();
            } else if (hitEntity instanceof LivingEntity living) {
                target = living;
            }

            if (target != null && EOPTargeting.isValidTarget(caster, target)) {
                target.hurt(createDamageSource(caster), this.damage);

                if (this.setEntityOnFireSeconds > 0) {
                    target.setSecondsOnFire(this.setEntityOnFireSeconds);
                }
            }
        }

        createFilteredExplosion();
        runCommandsForTargets();

        if (this.dieOnEntityHit) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level().isClientSide) {
            return;
        }

        runCommands(this.commandsOnBlockHit);
        createFilteredExplosion();
        runCommandsForTargets();

        if (this.dieOnBlockHit) {
            this.discard();
        }
    }

    private void runCommandsForTargets() {
        if (this.commandsForTargets == null || this.commandsForTargets.length == 0) {
            return;
        }

        if (this.commandsForTargetsRadius <= 0.0F) {
            return;
        }

        Entity owner = this.getOwner();

        if (!(owner instanceof LivingEntity caster)) {
            return;
        }

        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(this.commandsForTargetsRadius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            runCommandsAsTarget(target, this.commandsForTargets);
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
        if (commands == null || commands.length == 0 || this.getServer() == null) {
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

    private void createFilteredExplosion() {
        if (this.explosionRadius <= 0.0F) {
            return;
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                Math.min(4.0F, this.explosionRadius / 2.0F),
                1.0F
        );

        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    Math.max(1, (int) (this.explosionRadius * 4)),
                    this.explosionRadius * 0.35D,
                    this.explosionRadius * 0.35D,
                    this.explosionRadius * 0.35D,
                    0.08D
            );
        }

        if (this.explosionCausesFire && EOPGameRules.isDestructionMode(this.level().getServer())) {
            placeFireAroundExplosion();
        }

        handleExplosionBlocks();
        handleExplosionDamage();
    }

    private void handleExplosionDamage() {
        Entity owner = this.getOwner();

        if (!(owner instanceof LivingEntity caster)) {
            return;
        }

        float explosionDamage = this.explosionRadius * 2.0F;

        for (LivingEntity target : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(this.explosionRadius)
        )) {
            if (!EOPTargeting.isValidTarget(caster, target)) {
                continue;
            }

            double distance = target.distanceTo(this);
            double falloff = 1.0D - Math.min(distance / this.explosionRadius, 1.0D);

            float finalDamage = (float) (explosionDamage * falloff);

            if (finalDamage > 0.0F) {
                target.hurt(createDamageSource(caster), finalDamage);
            }
        }
    }

    private void handleExplosionBlocks() {
        String mode = this.explosionBlockInteraction == null
                ? "KEEP"
                : this.explosionBlockInteraction.toUpperCase();

        if (!EOPGameRules.isDestructionMode(this.level().getServer())) {
            mode = "KEEP";
        }

        if (mode.equals("KEEP")) {
            return;
        }

        boolean dropBlocks = mode.equals("BREAK");

        int radius = (int) Math.ceil(this.explosionRadius);
        BlockPos center = this.blockPosition();

        java.util.Random random = new java.util.Random();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            double distance = Math.sqrt(pos.distSqr(center));

            if (distance > this.explosionRadius) {
                continue;
            }

            var state = this.level().getBlockState(pos);

            if (state.isAir()) {
                continue;
            }

            if (state.getDestroySpeed(this.level(), pos) < 0.0F) {
                continue;
            }

            double norm = distance / this.explosionRadius;

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
        int radius = (int) Math.ceil(this.explosionRadius);
        BlockPos center = this.blockPosition();

        java.util.Random random = new java.util.Random();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius),
                center.offset(radius, 1, radius)
        )) {
            if (random.nextFloat() > 0.25F) {
                continue;
            }

            if (pos.distSqr(center) > this.explosionRadius * this.explosionRadius) {
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

    private DamageSource createDamageSource(LivingEntity caster) {
        ResourceLocation damageLocation = ResourceLocation.tryParse(this.damageType);

        if (damageLocation == null) {
            return caster.damageSources().magic();
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

        tag.putFloat("CommandsForTargetsRadius", this.commandsForTargetsRadius);
        tag.put("CommandsOnBlockHit", stringArrayToListTag(this.commandsOnBlockHit));
        tag.put("CommandsForTargets", stringArrayToListTag(this.commandsForTargets));
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

        if (tag.contains("CommandsForTargetsRadius")) {
            this.commandsForTargetsRadius = tag.getFloat("CommandsForTargetsRadius");
        }

        this.commandsOnBlockHit = readStringArray(tag, "CommandsOnBlockHit");
        this.commandsForTargets = readStringArray(tag, "CommandsForTargets");
    }
}
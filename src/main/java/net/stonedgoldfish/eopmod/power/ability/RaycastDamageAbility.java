package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.stonedgoldfish.eopmod.util.EOPTargeting;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;

public class RaycastDamageAbility extends Ability {

    public static final PalladiumProperty<Float> DAMAGE = new FloatProperty("damage").configurable("Amount of damage dealt to the hit entity");
    public static final PalladiumProperty<Float> RANGE = new FloatProperty("range").configurable("Maximum raycast distance");
    public static final PalladiumProperty<String> DAMAGE_TYPE = new StringProperty("damage_type").configurable("Damage type used by this ability");
    public static final PalladiumProperty<Integer> SET_ON_FIRE = new IntegerProperty("set_on_fire").configurable("Sets the hit target on fire");
    public static final PalladiumProperty<String[]> COMMANDS_ON_TARGET = new StringArrayProperty("commands_on_target").configurable("Commands executed as the hit target");
    public static final PalladiumProperty<String[]> COMMANDS_ON_ALLIES = new StringArrayProperty("commands_on_allies").configurable("Commands executed as the first allied entity hit by the raycast");
    public static final PalladiumProperty<String[]> COMMANDS_ON_BLOCK_HIT = new StringArrayProperty("commands_on_block_hit").configurable("Commands executed at the block hit location");
    public static final PalladiumProperty<Boolean> CAUSE_FIRE = new BooleanProperty("cause_fire").configurable("If true, sets blocks on fire at the hit location");
    public static final PalladiumProperty<Boolean> CREATE_EXPLOSION = new BooleanProperty("create_explosion").configurable("If true, creates an explosion effect");
    public static final PalladiumProperty<Boolean> EXPLOSION_CAUSES_FIRE = new BooleanProperty("explosion_causes_fire").configurable("If true, the explosion creates fire");
    public static final PalladiumProperty<Float> EXPLOSION_RADIUS = new FloatProperty("explosion_radius").configurable("Radius of the explosion");
    public static final PalladiumProperty<Boolean> EXPLOSION_DROP_BLOCKS = new BooleanProperty("explosion_drop_blocks").configurable("If true, destroyed explosion blocks will drop items");
    public static final PalladiumProperty<Boolean> SMELT_BLOCK = new BooleanProperty("smelt_block").configurable("If true, attempts to smelt the hit block");
    private static final java.util.Map<java.util.UUID, net.minecraft.core.BlockPos> LAST_SMELT_HIT = new java.util.HashMap<>();

    public RaycastDamageAbility() {
        this.withProperty(ICON, new ItemIcon(Items.SPECTRAL_ARROW));
        this.withProperty(DAMAGE, 6.0F);
        this.withProperty(RANGE, 20.0F);
        this.withProperty(DAMAGE_TYPE, "minecraft:magic");
        this.withProperty(SET_ON_FIRE, 0);
        this.withProperty(COMMANDS_ON_TARGET, new String[]{});
        this.withProperty(COMMANDS_ON_ALLIES, new String[]{});
        this.withProperty(COMMANDS_ON_BLOCK_HIT, new String[]{});
        this.withProperty(CAUSE_FIRE, false);
        this.withProperty(CREATE_EXPLOSION, false);
        this.withProperty(EXPLOSION_CAUSES_FIRE, false);
        this.withProperty(EXPLOSION_RADIUS, 3.0F);
        this.withProperty(EXPLOSION_DROP_BLOCKS, false);
        this.withProperty(SMELT_BLOCK, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) return;

        float damage = entry.getProperty(DAMAGE);
        float range = entry.getProperty(RANGE);
        String damageTypeId = entry.getProperty(DAMAGE_TYPE);
        int fireSeconds = entry.getProperty(SET_ON_FIRE);
        String[] commandsOnTarget = entry.getProperty(COMMANDS_ON_TARGET);
        String[] commandsOnAllies = entry.getProperty(COMMANDS_ON_ALLIES);
        String[] commandsOnBlockHit = entry.getProperty(COMMANDS_ON_BLOCK_HIT);
        boolean causeFire = entry.getProperty(CAUSE_FIRE);
        boolean createExplosion = entry.getProperty(CREATE_EXPLOSION);
        boolean explosionCausesFire = entry.getProperty(EXPLOSION_CAUSES_FIRE);
        float explosionRadius = entry.getProperty(EXPLOSION_RADIUS);
        boolean explosionDropBlocks = entry.getProperty(EXPLOSION_DROP_BLOCKS);
        boolean smeltBlock = entry.getProperty(SMELT_BLOCK);

        Vec3 start = entity.getEyePosition();
        Vec3 look = entity.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));

        BlockHitResult blockHit = entity.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

        double entityRange = range;

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            entityRange = start.distanceTo(blockHit.getLocation());
        }

        Vec3 entityEnd = start.add(look.scale(entityRange));

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                entity.level(),
                entity,
                start,
                entityEnd,
                entity.getBoundingBox().expandTowards(look.scale(entityRange)).inflate(1.0D),
                target -> target instanceof LivingEntity && target != entity && target.isAlive()
        );

        Vec3 explosionPos = null;

        if (blockHit.getType() != HitResult.Type.BLOCK) {
            LAST_SMELT_HIT.remove(entity.getUUID());
        }

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            runCommandsAtBlock(entity, blockHit, commandsOnBlockHit);
            if (smeltBlock) {
                net.minecraft.core.BlockPos hitPos = blockHit.getBlockPos();

                net.minecraft.core.BlockPos lastPos = LAST_SMELT_HIT.get(entity.getUUID());

                if (!hitPos.equals(lastPos)) {
                    smeltHitBlock(entity, blockHit);
                    LAST_SMELT_HIT.put(entity.getUUID(), hitPos.immutable());
                }
            }
            explosionPos = blockHit.getLocation();
        }

        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            explosionPos = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);

            boolean ally = !EOPTargeting.isValidTarget(entity, target);

            if (ally) {
                runCommandsAsTarget(target, commandsOnAllies);
            } else {
                target.hurt(createDamageSource(entity, damageTypeId), damage);

                if (fireSeconds > 0) {
                    target.setSecondsOnFire(fireSeconds);
                }

                runCommandsAsTarget(target, commandsOnTarget);
            }
        }

        if (causeFire
                && explosionPos != null
                && EOPGameRules.isDestructionMode(entity.level().getServer())) {

            placeFireAtHit(entity, explosionPos);
        }

        if (createExplosion && explosionPos != null) {
            createBlockOnlyExplosion(
                    entity,
                    explosionPos,
                    explosionRadius,
                    explosionDropBlocks,
                    EOPGameRules.isDestructionMode(entity.level().getServer()),
                    explosionCausesFire
            );
        }
    }

    private static void smeltHitBlock(LivingEntity entity, BlockHitResult hit) {

        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }

        if (!EOPGameRules.isDestructionMode(level.getServer())) {
            return;
        }

        net.minecraft.core.BlockPos pos = hit.getBlockPos();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);

        if (state.isAir()) {
            return;
        }

        net.minecraft.world.item.ItemStack input =
                new net.minecraft.world.item.ItemStack(state.getBlock());

        var recipe = level.getRecipeManager().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.SMELTING,
                new net.minecraft.world.SimpleContainer(input),
                level
        );

        if (recipe.isEmpty()) {
            return;
        }

        net.minecraft.world.item.ItemStack result =
                recipe.get().getResultItem(level.registryAccess());

        if (result.isEmpty()) {
            return;
        }

        net.minecraft.world.level.block.Block block =
                net.minecraft.world.level.block.Block.byItem(result.getItem());

        if (block == net.minecraft.world.level.block.Blocks.AIR) {
            return;
        }

        level.setBlockAndUpdate(
                pos,
                block.defaultBlockState()
        );
    }

    private static void placeFireAtHit(LivingEntity entity, Vec3 hitPos) {

        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }

        net.minecraft.core.BlockPos pos =
                net.minecraft.core.BlockPos.containing(hitPos);

        net.minecraft.core.BlockPos firePos;

        if (level.getBlockState(pos).isAir()) {
            firePos = pos;
        } else {
            firePos = pos.above();
        }

        if (!level.getBlockState(firePos).isAir()) {
            return;
        }

        level.setBlockAndUpdate(
                firePos,
                net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState()
        );
    }

    private static void runCommandsAtBlock(LivingEntity caster, BlockHitResult hit, String[] commands) {
        if (commands == null || commands.length == 0 || caster.getServer() == null) return;

        Vec3 pos = hit.getLocation();

        for (String command : commands) {
            if (command == null || command.isBlank()) continue;

            caster.getServer().getCommands().performPrefixedCommand(
                    caster.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2)
                            .withPosition(pos),
                    command
            );
        }
    }

    private static void runCommandsAsTarget(LivingEntity target, String[] commands) {
        if (commands == null || commands.length == 0 || target.getServer() == null) return;

        for (String command : commands) {
            if (command == null || command.isBlank()) continue;

            target.getServer().getCommands().performPrefixedCommand(
                    target.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2),
                    command
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
            Vec3 centerPos,
            float radius,
            boolean dropBlocks,
            boolean destroyBlocks,
            boolean createFire
    ) {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        net.minecraft.core.BlockPos center = net.minecraft.core.BlockPos.containing(centerPos);
        int r = (int) Math.ceil(radius);
        java.util.Random random = new java.util.Random();

        if (destroyBlocks) {
            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
                double distance = Math.sqrt(pos.distSqr(center));
                if (distance > radius) continue;

                var state = level.getBlockState(pos);
                if (state.isAir()) continue;
                if (state.getDestroySpeed(level, pos) < 0.0F) continue;

                double norm = distance / radius;
                double breakChance = 1.0D - (norm * norm);
                breakChance += (random.nextDouble() - 0.5D) * (norm * 0.8D);

                if (breakChance < 0.15D) continue;

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

            if ((offsetX * offsetX) + (offsetY * offsetY) + (offsetZ * offsetZ) > radius * radius) continue;

            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, centerPos.x + offsetX, centerPos.y + offsetY, centerPos.z + offsetZ, 1, 0, 0, 0, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, centerPos.x + offsetX, centerPos.y + offsetY, centerPos.z + offsetZ, 2, 0.2D, 0.2D, 0.2D, 0.02D);
        }

        level.playSound(
                null,
                centerPos.x,
                centerPos.y,
                centerPos.z,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.BLOCKS,
                Math.min(4.0F, radius / 2.0F),
                1.0F
        );

        if (createFire && destroyBlocks) {
            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
                if (random.nextFloat() > 0.25F) continue;
                if (pos.distSqr(center) > radius * radius) continue;

                net.minecraft.core.BlockPos firePos = pos.above();

                if (!level.getBlockState(firePos).isAir()) continue;
                if (level.getBlockState(pos).isAir()) continue;

                level.setBlockAndUpdate(firePos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
            }
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Deals configurable raycast damage to the first entity or block the user is looking at.";
    }
}
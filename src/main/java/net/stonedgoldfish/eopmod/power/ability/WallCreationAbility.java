package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.FakeLaunchedBlockPacket;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;

import java.util.*;

public class WallCreationAbility extends Ability {

    public static final PalladiumProperty<Float> RANGE = new FloatProperty("range").configurable("Raycast range");
    public static final PalladiumProperty<Integer> SIZE = new IntegerProperty("size").configurable("Square wall size");
    public static final PalladiumProperty<String> BLOCK = new StringProperty("block").configurable("Block used for the wall");
    public static final PalladiumProperty<Integer> MAX_HEIGHT = new IntegerProperty("max_height").configurable("Maximum height each wall can reach");

    private static final int RISING_TIME = 7;
    private static final double RISING_MARKER = 999.0D;

    private static final Map<UUID, List<WallData>> ACTIVE_WALLS = new HashMap<>();
    private static final Map<UUID, List<PendingBlockPlacement>> PENDING_BLOCKS = new HashMap<>();

    private record WallData(
            BlockPos baseCenter,
            int currentHeight,
            int tickTimer
    ) {}

    private record PendingBlockPlacement(
            BlockPos pos,
            BlockState state,
            int ticksLeft
    ) {}

    public WallCreationAbility() {
        this.withProperty(ICON, new ItemIcon(Items.STONE));
        this.withProperty(RANGE, 12.0F);
        this.withProperty(SIZE, 3);
        this.withProperty(BLOCK, "minecraft:stone");
        this.withProperty(MAX_HEIGHT, 20);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        UUID uuid = entity.getUUID();

        processPendingBlocks(entity, uuid);

        if (!enabled) {
            return;
        }

        float range = entry.getProperty(RANGE);
        int size = Math.max(1, entry.getProperty(SIZE));
        BlockState wallState = getBlockState(entry.getProperty(BLOCK));

        List<WallData> walls = ACTIVE_WALLS.computeIfAbsent(
                uuid,
                key -> new ArrayList<>()
        );

        extendAllWalls(
                entity,
                walls,
                size,
                wallState,
                entry.getProperty(MAX_HEIGHT)
        );

        BlockPos newWallCenter = getRaycastWallStart(entity, range);

        if (newWallCenter == null) {
            return;
        }

        if (hasWallAt(walls, newWallCenter)) {
            return;
        }

        boolean created = createWallLayer(entity, newWallCenter, size, wallState, 0);

        if (created) {
            walls.add(new WallData(newWallCenter, 1, 0));
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        ACTIVE_WALLS.remove(entity.getUUID());
    }

    private static void processPendingBlocks(LivingEntity entity, UUID uuid) {
        List<PendingBlockPlacement> pendingBlocks = PENDING_BLOCKS.get(uuid);

        if (pendingBlocks == null || pendingBlocks.isEmpty()) {
            return;
        }

        Iterator<PendingBlockPlacement> iterator = pendingBlocks.iterator();
        List<PendingBlockPlacement> updated = new ArrayList<>();

        while (iterator.hasNext()) {
            PendingBlockPlacement pending = iterator.next();
            int ticksLeft = pending.ticksLeft() - 1;

            if (ticksLeft > 0) {
                updated.add(new PendingBlockPlacement(
                        pending.pos(),
                        pending.state(),
                        ticksLeft
                ));

                iterator.remove();
                continue;
            }

            BlockState currentState = entity.level().getBlockState(pending.pos());

            boolean destructionMode = EOPGameRules.isDestructionMode(
                    entity.level().getServer()
            );

            if (canReplaceForWall(currentState, destructionMode)
                    && !hasLivingEntityInside(entity, pending.pos())) {
                entity.level().setBlockAndUpdate(pending.pos(), pending.state());
            }

            iterator.remove();
        }

        pendingBlocks.addAll(updated);

        if (pendingBlocks.isEmpty()) {
            PENDING_BLOCKS.remove(uuid);
        }
    }

    private static void extendAllWalls(
            LivingEntity entity,
            List<WallData> walls,
            int size,
            BlockState state,
            int maxHeight
    ) {
        for (int i = 0; i < walls.size(); i++) {
            WallData wall = walls.get(i);

            if (wall.currentHeight() >= maxHeight) {
                continue;
            }

            int timer = wall.tickTimer() + 1;

            if (timer < RISING_TIME + 2) {
                walls.set(
                        i,
                        new WallData(
                                wall.baseCenter(),
                                wall.currentHeight(),
                                timer
                        )
                );
                continue;
            }

            BlockPos nextCenter = wall.baseCenter().above(wall.currentHeight());

            boolean placedAny = createWallLayer(
                    entity,
                    nextCenter,
                    size,
                    state,
                    wall.currentHeight()
            );

            if (placedAny) {
                walls.set(
                        i,
                        new WallData(
                                wall.baseCenter(),
                                wall.currentHeight() + 1,
                                0
                        )
                );
            } else {
                walls.set(
                        i,
                        new WallData(
                                wall.baseCenter(),
                                wall.currentHeight(),
                                0
                        )
                );
            }
        }
    }

    private static boolean hasWallAt(List<WallData> walls, BlockPos center) {
        for (WallData wall : walls) {
            if (wall.baseCenter.equals(center)) {
                return true;
            }
        }

        return false;
    }

    private static BlockPos getRaycastWallStart(LivingEntity entity, float range) {
        Vec3 start = entity.getEyePosition();
        Vec3 end = start.add(entity.getLookAngle().normalize().scale(range));

        BlockHitResult hit = entity.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        if (hit.getDirection() != Direction.UP) {
            return null;
        }

        BlockPos hitPos = hit.getBlockPos();

        if (entity.level().getBlockState(hitPos).isAir()) {
            return null;
        }

        return hitPos.above();
    }

    private static boolean createWallLayer(
            LivingEntity entity,
            BlockPos center,
            int size,
            BlockState state,
            int height
    ) {
        int half = size / 2;
        boolean placedAny = false;

        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                BlockPos floorPos = center.offset(x, -1, z);
                BlockPos placePos = center.offset(x, 0, z);

                if (height >= 2 && hasLivingEntityInColumn(entity, placePos)) {
                    continue;
                }

                if (!shouldPlaceImperfectBlock(entity, x, z, half, height)) {
                    continue;
                }

                if (entity.level().getBlockState(floorPos).isAir()) {
                    continue;
                }

                BlockState existingState = entity.level().getBlockState(placePos);

                boolean destructionMode = EOPGameRules.isDestructionMode(
                        entity.level().getServer()
                );

                if (!canReplaceForWall(existingState, destructionMode)) {
                    continue;
                }

                if (hasLivingEntityInside(entity, placePos)) {
                    continue;
                }

                if (hasPendingBlock(entity.getUUID(), placePos)) {
                    continue;
                }

                sendRisingBlockAnimation(entity, placePos, state);

                PENDING_BLOCKS
                        .computeIfAbsent(entity.getUUID(), key -> new ArrayList<>())
                        .add(new PendingBlockPlacement(
                                placePos,
                                state,
                                RISING_TIME
                        ));

                placedAny = true;
            }
        }

        return placedAny;
    }

    private static boolean hasPendingBlock(UUID uuid, BlockPos pos) {
        List<PendingBlockPlacement> pendingBlocks = PENDING_BLOCKS.get(uuid);

        if (pendingBlocks == null) {
            return false;
        }

        for (PendingBlockPlacement pending : pendingBlocks) {
            if (pending.pos().equals(pos)) {
                return true;
            }
        }

        return false;
    }

    private static void sendRisingBlockAnimation(
            LivingEntity entity,
            BlockPos pos,
            BlockState state
    ) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        EOPNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_CHUNK.with(
                        () -> serverLevel.getChunkAt(pos)
                ),
                new FakeLaunchedBlockPacket(
                        state,
                        Vec3.atLowerCornerOf(pos).add(0.0D, -1.0D, 0.0D),
                        new Vec3(0.0D, RISING_MARKER, 0.0D)
                )
        );
    }

    private static boolean shouldPlaceImperfectBlock(
            LivingEntity entity,
            int x,
            int z,
            int half,
            int height
    ) {
        if (height <= 0) {
            return true;
        }

        double heightPenalty = Math.min(0.55D, height * 0.055D);

        boolean edgeBlock =
                Math.abs(x) == half
                        || Math.abs(z) == half;

        double edgePenalty = edgeBlock ? 0.18D : 0.0D;

        double cornerPenalty =
                Math.abs(x) == half && Math.abs(z) == half
                        ? 0.18D
                        : 0.0D;

        double placeChance = 1.0D - heightPenalty - edgePenalty - cornerPenalty;

        placeChance = Math.max(0.25D, placeChance);

        return entity.getRandom().nextDouble() <= placeChance;
    }

    private static boolean hasLivingEntityInColumn(LivingEntity caster, BlockPos pos) {
        AABB column = new AABB(
                pos.getX(),
                pos.getY() - 2,
                pos.getZ(),
                pos.getX() + 1,
                pos.getY() + 1,
                pos.getZ() + 1
        );

        return !caster.level().getEntitiesOfClass(
                LivingEntity.class,
                column,
                living -> living.isAlive()
        ).isEmpty();
    }

    private static BlockState getBlockState(String blockId) {
        ResourceLocation id = ResourceLocation.tryParse(blockId);

        if (id == null) {
            return Blocks.STONE.defaultBlockState();
        }

        Block block = BuiltInRegistries.BLOCK.get(id);

        if (block == Blocks.AIR) {
            return Blocks.STONE.defaultBlockState();
        }

        return block.defaultBlockState();
    }

    private static boolean canReplaceForWall(BlockState state, boolean destructionMode) {
        if (state.isAir()) {
            return true;
        }

        if (!destructionMode) {
            return false;
        }

        return state.canBeReplaced()
                || state.getCollisionShape(null, BlockPos.ZERO).isEmpty()
                || state.is(net.minecraft.tags.BlockTags.LEAVES)
                || state.is(net.minecraft.tags.BlockTags.FLOWERS);
    }

    private static boolean hasLivingEntityInside(LivingEntity caster, BlockPos pos) {
        AABB box = new AABB(pos);

        return !caster.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                living -> living.isAlive()
        ).isEmpty();
    }

    @Override
    public String getDocumentationDescription() {
        return "Creates and extends multiple vertical walls with rising block animations";
    }
}
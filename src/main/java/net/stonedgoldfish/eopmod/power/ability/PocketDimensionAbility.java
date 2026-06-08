package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.ITeleporter;
import net.stonedgoldfish.eopmod.block.EOPBlocks;
import net.stonedgoldfish.eopmod.util.EOPTargeting;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

import java.util.List;
import java.util.function.Function;

import static net.stonedgoldfish.eopmod.worldgen.dimension.EOPDimensions.POCKET_DIMENSION;

public class PocketDimensionAbility extends Ability {

    public PocketDimensionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENDER_EYE));
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().dimension().equals(POCKET_DIMENSION)) {
            savePocketLocation(player);

            List<LivingEntity> companions = getNearbyFriendlyEntities(player, 3.0D);
            ServerLevel returnLevel = getSavedReturnLevel(player);
            CompoundTag returnTag = player.getPersistentData().getCompound("EOPPocketReturn");

            teleportBack(player);

            if (returnLevel != null) {
                teleportCompanions(
                        companions,
                        returnLevel,
                        returnTag.getDouble("X"),
                        returnTag.getDouble("Y"),
                        returnTag.getDouble("Z")
                );
            }

            return;
        }

        ServerLevel targetLevel = player.server.getLevel(POCKET_DIMENSION);

        if (targetLevel == null) {
            return;
        }

        saveReturnLocation(player);

        List<LivingEntity> companions = getNearbyFriendlyEntities(player, 3.0D);

        createPocketRoom(targetLevel, BlockPos.containing(1, 2, 1));

        SavedLocation pocketLocation = getPocketLocation(player);

        teleportCompanions(
                companions,
                targetLevel,
                pocketLocation.x,
                pocketLocation.y,
                pocketLocation.z
        );

        player.teleportTo(
                targetLevel,
                pocketLocation.x,
                pocketLocation.y,
                pocketLocation.z,
                pocketLocation.yRot,
                pocketLocation.xRot
        );

        player.fallDistance = 0.0F;
    }

    private static List<LivingEntity> getNearbyFriendlyEntities(ServerPlayer player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);

        return player.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> {
                    if (entity == player || !entity.isAlive()) {
                        return false;
                    }

                    if (!EOPTargeting.isFriendlyTarget(player, entity)) {
                        return false;
                    }

                    if (entity instanceof ServerPlayer teammate) {
                        return teammate.isShiftKeyDown();
                    }

                    return true;
                }
        );
    }

    private static void teleportCompanions(
            List<LivingEntity> companions,
            ServerLevel targetLevel,
            double x,
            double y,
            double z
    ) {
        for (LivingEntity companion : companions) {
            teleportLivingAcrossDimensions(
                    companion,
                    targetLevel,
                    x,
                    y,
                    z
            );
        }
    }

    private static void teleportLivingAcrossDimensions(
            LivingEntity entity,
            ServerLevel targetLevel,
            double x,
            double y,
            double z
    ) {
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(
                    targetLevel,
                    x,
                    y,
                    z,
                    player.getYRot(),
                    player.getXRot()
            );

            player.fallDistance = 0.0F;
            return;
        }

        if (entity.level() == targetLevel) {
            entity.teleportTo(x, y, z);
            entity.fallDistance = 0.0F;
            return;
        }

        Entity changed = entity.changeDimension(
                targetLevel,
                new ITeleporter() {
                    @Override
                    public Entity placeEntity(
                            Entity entity,
                            ServerLevel currentWorld,
                            ServerLevel destWorld,
                            float yaw,
                            Function<Boolean, Entity> repositionEntity
                    ) {
                        Entity newEntity = repositionEntity.apply(false);

                        newEntity.teleportTo(x, y, z);
                        newEntity.setYRot(entity.getYRot());
                        newEntity.setXRot(entity.getXRot());
                        newEntity.fallDistance = 0.0F;

                        return newEntity;
                    }
                }
        );

        if (changed != null) {
            changed.teleportTo(x, y, z);
            changed.fallDistance = 0.0F;
        }
    }

    private static void savePocketLocation(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();

        tag.putDouble("X", player.getX());
        tag.putDouble("Y", player.getY());
        tag.putDouble("Z", player.getZ());
        tag.putFloat("YRot", player.getYRot());
        tag.putFloat("XRot", player.getXRot());

        player.getPersistentData().put("EOPPocketLocation", tag);
    }

    private static SavedLocation getPocketLocation(ServerPlayer player) {
        if (!player.getPersistentData().contains("EOPPocketLocation")) {
            return new SavedLocation(
                    1.5D,
                    2.0D,
                    1.5D,
                    player.getYRot(),
                    player.getXRot()
            );
        }

        CompoundTag tag = player.getPersistentData().getCompound("EOPPocketLocation");

        return new SavedLocation(
                tag.getDouble("X"),
                tag.getDouble("Y"),
                tag.getDouble("Z"),
                tag.getFloat("YRot"),
                tag.getFloat("XRot")
        );
    }

    private static void saveReturnLocation(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();

        tag.putString("Dimension", player.level().dimension().location().toString());
        tag.putDouble("X", player.getX());
        tag.putDouble("Y", player.getY());
        tag.putDouble("Z", player.getZ());
        tag.putFloat("YRot", player.getYRot());
        tag.putFloat("XRot", player.getXRot());

        player.getPersistentData().put("EOPPocketReturn", tag);
    }

    private static void teleportBack(ServerPlayer player) {
        ServerLevel returnLevel = getSavedReturnLevel(player);

        if (returnLevel == null) {
            return;
        }

        CompoundTag tag = player.getPersistentData().getCompound("EOPPocketReturn");

        player.teleportTo(
                returnLevel,
                tag.getDouble("X"),
                tag.getDouble("Y"),
                tag.getDouble("Z"),
                tag.getFloat("YRot"),
                tag.getFloat("XRot")
        );

        player.fallDistance = 0.0F;
    }

    private static ServerLevel getSavedReturnLevel(ServerPlayer player) {
        if (!player.getPersistentData().contains("EOPPocketReturn")) {
            return null;
        }

        CompoundTag tag = player.getPersistentData().getCompound("EOPPocketReturn");

        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("Dimension"));

        if (dimensionId == null) {
            return null;
        }

        ResourceKey<Level> dimensionKey =
                ResourceKey.create(Registries.DIMENSION, dimensionId);

        return player.server.getLevel(dimensionKey);
    }

    private static void createPocketRoom(ServerLevel level, BlockPos center) {
        int halfSize = 12;
        int roomHeight = 20;

        int floorY = center.getY() - 1;
        int minX = center.getX() - halfSize;
        int maxX = center.getX() + halfSize;
        int minZ = center.getZ() - halfSize;
        int maxZ = center.getZ() + halfSize;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                level.setBlockAndUpdate(
                        new BlockPos(x, floorY - 1, z),
                        Blocks.BARRIER.defaultBlockState()
                );

                level.setBlockAndUpdate(
                        new BlockPos(x, floorY, z),
                        EOPBlocks.DIMENSION_BIT.get().defaultBlockState()
                );
            }
        }

        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int y = floorY; y <= floorY + roomHeight; y++) {
                for (int z = minZ - 1; z <= maxZ + 1; z++) {
                    boolean outsideX = x == minX - 1 || x == maxX + 1;
                    boolean outsideZ = z == minZ - 1 || z == maxZ + 1;
                    boolean ceiling = y == floorY + roomHeight;

                    if (outsideX || outsideZ || ceiling) {
                        level.setBlockAndUpdate(
                                new BlockPos(x, y, z),
                                Blocks.BARRIER.defaultBlockState()
                        );
                    }
                }
            }
        }
    }

    private record SavedLocation(
            double x,
            double y,
            double z,
            float yRot,
            float xRot
    ) {}

    @Override
    public String getDocumentationDescription() {
        return "Teleports the player and nearby friendly living entities to and from a pocket dimension.";
    }
}
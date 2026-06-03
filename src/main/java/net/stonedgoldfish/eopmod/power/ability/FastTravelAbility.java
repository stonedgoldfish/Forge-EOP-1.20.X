package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

public class FastTravelAbility extends Ability {

    public static final PalladiumProperty<Boolean> ONE_WAY =
            new BooleanProperty("one_way")
                    .configurable("If true, teleporting does not swap the saved location.");

    public static final PalladiumProperty<Boolean> TELEPORT_NEARBY_ENTITIES =
            new BooleanProperty("teleport_nearby_entities")
                    .configurable("If true, nearby entities teleport with the caster.");

    public static final PalladiumProperty<Float> NEARBY_RADIUS =
            new FloatProperty("nearby_radius")
                    .configurable("Radius for nearby entities teleported with the caster.");

    public static final PalladiumProperty<Boolean> DIMENSIONAL_TELEPORT =
            new BooleanProperty("dimensional_teleport")
                    .configurable("If true, teleporting between dimensions is allowed.");

    public FastTravelAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENDER_EYE));
        this.withProperty(ONE_WAY, false);
        this.withProperty(TELEPORT_NEARBY_ENTITIES, false);
        this.withProperty(NEARBY_RADIUS, 4.0F);
        this.withProperty(DIMENSIONAL_TELEPORT, false);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        if (entity.isShiftKeyDown()) {
            if (hasSavedLocation(entity)) {
                removeSavedLocation(entity);
                sendMessage(entity, "§cFast travel location removed.");

                level.playSound(
                        null,
                        entity.blockPosition(),
                        SoundEvents.NOTE_BLOCK_BASS.value(),
                        SoundSource.PLAYERS,
                        1.0F,
                        0.6F
                );
            } else {
                saveLocation(entity, level);
                sendMessage(entity, "§aFast travel location saved.");

                level.playSound(
                        null,
                        entity.blockPosition(),
                        SoundEvents.NOTE_BLOCK_PLING.value(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.4F
                );
            }

            return;
        }

        SavedLocation saved = getSavedLocation(entity);

        if (saved == null) {
            sendMessage(entity, "§cNo fast travel location saved.");
            return;
        }

        if (!entry.getProperty(DIMENSIONAL_TELEPORT)
                && !saved.dimension.equals(level.dimension().location().toString())) {
            sendMessage(entity, "§cCannot travel between dimensions.");
            return;
        }

        ServerLevel targetLevel = getTargetLevel(level, saved.dimension);

        if (targetLevel == null) {
            sendMessage(entity, "§cFast travel dimension could not be found.");
            return;
        }

        SavedLocation returnLocation = new SavedLocation(
                level.dimension().location().toString(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                entity.getYRot(),
                entity.getXRot()
        );

        if (entry.getProperty(TELEPORT_NEARBY_ENTITIES)) {
            teleportNearbyEntities(
                    entity,
                    level,
                    targetLevel,
                    saved,
                    entry.getProperty(NEARBY_RADIUS)
            );
        }

        teleportEntity(entity, targetLevel, saved);

        if (!entry.getProperty(ONE_WAY)) {
            saveLocation(entity, returnLocation);
        }

        targetLevel.playSound(
                null,
                saved.x,
                saved.y,
                saved.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private static void teleportNearbyEntities(
            LivingEntity caster,
            ServerLevel currentLevel,
            ServerLevel targetLevel,
            SavedLocation target,
            float radius
    ) {
        if (radius <= 0.0F) {
            return;
        }

        for (Entity nearby : currentLevel.getEntities(
                caster,
                caster.getBoundingBox().inflate(radius)
        )) {
            if (!(nearby instanceof LivingEntity living)) {
                continue;
            }

            if (living == caster) {
                continue;
            }

            if (!living.isAlive()) {
                continue;
            }

            if (living instanceof net.minecraft.world.entity.decoration.ArmorStand) {
                continue;
            }

            if (living instanceof ServerPlayer player && !player.isShiftKeyDown()) {
                continue;
            }

            double offsetX = nearby.getX() - caster.getX();
            double offsetY = nearby.getY() - caster.getY();
            double offsetZ = nearby.getZ() - caster.getZ();

            SavedLocation offsetTarget = new SavedLocation(
                    target.dimension,
                    target.x + offsetX,
                    target.y + offsetY,
                    target.z + offsetZ,
                    nearby.getYRot(),
                    nearby.getXRot()
            );

            teleportEntity(nearby, targetLevel, offsetTarget);
        }
    }

    private static void teleportEntity(Entity entity, ServerLevel targetLevel, SavedLocation target) {
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(
                    targetLevel,
                    target.x,
                    target.y,
                    target.z,
                    target.yRot,
                    target.xRot
            );

            player.fallDistance = 0.0F;
            return;
        }

        if (entity.level() == targetLevel) {
            entity.teleportTo(target.x, target.y, target.z);
            entity.setYRot(target.yRot);
            entity.setXRot(target.xRot);
            entity.fallDistance = 0.0F;
        }
    }

    private static ServerLevel getTargetLevel(ServerLevel currentLevel, String dimension) {
        ResourceLocation id = ResourceLocation.tryParse(dimension);

        if (id == null) {
            return null;
        }

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);

        return currentLevel.getServer().getLevel(key);
    }

    private static boolean hasSavedLocation(LivingEntity entity) {
        return entity.getPersistentData().contains("EOPFastTravel");
    }

    private static void removeSavedLocation(LivingEntity entity) {
        entity.getPersistentData().remove("EOPFastTravel");
    }

    private static void saveLocation(LivingEntity entity, ServerLevel level) {
        saveLocation(entity, new SavedLocation(
                level.dimension().location().toString(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                entity.getYRot(),
                entity.getXRot()
        ));
    }

    private static void saveLocation(LivingEntity entity, SavedLocation location) {
        CompoundTag tag = new CompoundTag();

        tag.putString("Dimension", location.dimension);
        tag.putDouble("X", location.x);
        tag.putDouble("Y", location.y);
        tag.putDouble("Z", location.z);
        tag.putFloat("YRot", location.yRot);
        tag.putFloat("XRot", location.xRot);

        entity.getPersistentData().put("EOPFastTravel", tag);
    }

    private static SavedLocation getSavedLocation(LivingEntity entity) {
        if (!hasSavedLocation(entity)) {
            return null;
        }

        CompoundTag tag = entity.getPersistentData().getCompound("EOPFastTravel");

        return new SavedLocation(
                tag.getString("Dimension"),
                tag.getDouble("X"),
                tag.getDouble("Y"),
                tag.getDouble("Z"),
                tag.getFloat("YRot"),
                tag.getFloat("XRot")
        );
    }

    private static void sendMessage(LivingEntity entity, String message) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal(message), true);
        }
    }

    private static class SavedLocation {
        private final String dimension;
        private final double x;
        private final double y;
        private final double z;
        private final float yRot;
        private final float xRot;

        private SavedLocation(String dimension, double x, double y, double z, float yRot, float xRot) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Saves a location while sneaking, removes it while sneaking again, and instantly teleports to it when used normally.";
    }
}
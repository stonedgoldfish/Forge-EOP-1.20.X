package net.stonedgoldfish.eopmod.power;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.threetag.palladium.event.PalladiumEvents;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.HashMap;
import java.util.Map;

public class EOPPalladiumProperties {

    private static final Map<String, PalladiumProperty<Integer>> XP_PROPERTIES = new HashMap<>();
    private static final Map<String, PalladiumProperty<Integer>> LEVEL_PROPERTIES = new HashMap<>();

    public static void init() {
        PalladiumEvents.REGISTER_PROPERTY.register(handler -> {
            if (handler.getEntity().getType() == EntityType.PLAYER) {
                for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
                    handler.register(getOrCreateXpProperty(power.key()), 0);
                    handler.register(getOrCreateLevelProperty(power.key()), 1);
                }
            }
        });
    }

    public static PalladiumProperty<Integer> getOrCreateXpProperty(String powerKey) {
        return XP_PROPERTIES.computeIfAbsent(
                powerKey,
                key -> new IntegerProperty(getXpPropertyName(key))
        );
    }

    public static PalladiumProperty<Integer> getOrCreateLevelProperty(String powerKey) {
        return LEVEL_PROPERTIES.computeIfAbsent(
                powerKey,
                key -> new IntegerProperty(getLevelPropertyName(key))
        );
    }

    public static String getXpPropertyName(String powerKey) {
        return "eop_" + powerKey + "_xp";
    }

    public static String getLevelPropertyName(String powerKey) {
        return "eop_" + powerKey + "_level";
    }

    public static int getXp(Entity entity, String powerKey) {
        return getOrCreateXpProperty(powerKey).get(entity);
    }

    public static void setXp(Entity entity, String powerKey, int amount) {
        getOrCreateXpProperty(powerKey).set(entity, amount);
    }

    public static int getLevel(Entity entity, String powerKey) {
        return getOrCreateLevelProperty(powerKey).get(entity);
    }

    public static void setLevel(Entity entity, String powerKey, int level) {
        getOrCreateLevelProperty(powerKey).set(entity, level);
    }
}
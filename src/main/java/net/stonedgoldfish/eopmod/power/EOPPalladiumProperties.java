package net.stonedgoldfish.eopmod.power;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.threetag.palladium.event.PalladiumEvents;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.BooleanProperty;

import java.util.HashMap;
import java.util.Map;

public class EOPPalladiumProperties {

    private static final Map<String, PalladiumProperty<Integer>> XP_PROPERTIES = new HashMap<>();
    private static final Map<String, PalladiumProperty<Integer>> LEVEL_PROPERTIES = new HashMap<>();
    private static final Map<String, PalladiumProperty<Integer>> SKILL_POINT_PROPERTIES = new HashMap<>();
    private static final Map<String, PalladiumProperty<Integer>> ENERGY_PROPERTIES = new HashMap<>();
    public static final PalladiumProperty<Boolean> LIVING_CREATURE = new BooleanProperty("EOP.LivingCreature");
    private static final PalladiumProperty<Boolean> CLIMB_EXTRA = new BooleanProperty("eop_climb_extra");
    public static final PalladiumProperty<Boolean> NIGHT_VISION_EXTRA = new BooleanProperty("eop_night_vision_extra");
    public static final PalladiumProperty<Boolean> SMELTING_EXTRA = new BooleanProperty("eop_smelting_extra");
    public static final PalladiumProperty<Boolean> FIRE_RESISTANCE_EXTRA = new BooleanProperty("eop_fire_resistance_extra");
    public static final PalladiumProperty<Boolean> ENTITY_SENSE_EXTRA = new BooleanProperty("eop_entity_sense_extra");
    public static final PalladiumProperty<Boolean> SUPER_JUMP_EXTRA = new BooleanProperty("eop_super_jump_extra");
    public static final PalladiumProperty<Boolean> ERASE_EXTRA = new BooleanProperty("eop_erase_extra");
    public static final PalladiumProperty<Boolean> EXTRA_REACH_EXTRA = new BooleanProperty("eop_extra_reach_extra");
    public static final PalladiumProperty<Boolean> SLOW_FALL_EXTRA = new BooleanProperty("eop_slow_fall_extra");
    public static final PalladiumProperty<Boolean> LIGHT_EXTRA = new BooleanProperty("eop_light_extra");
    public static final PalladiumProperty<Boolean> WATER_BREATHING_EXTRA = new BooleanProperty("eop_water_breathing_extra");
    public static final PalladiumProperty<Boolean> FROST_WALKER_EXTRA = new BooleanProperty("eop_frost_walker_extra");

    public static void init() {
        PalladiumEvents.REGISTER_PROPERTY.register(handler -> {
            if (handler.getEntity().getType() == EntityType.PLAYER) {
                for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
                    handler.register(getOrCreateXpProperty(power.key()), 0);
                    handler.register(getOrCreateLevelProperty(power.key()), 1);
                    handler.register(getOrCreateSkillPointProperty(power.key()), 0);
                    if (power.energy()) {
                        handler.register(getOrCreateEnergyProperty(power.key()), 0);
                    }
                    handler.register(CLIMB_EXTRA, false);
                    handler.register(NIGHT_VISION_EXTRA, false);
                    handler.register(SMELTING_EXTRA, false);
                    handler.register(FIRE_RESISTANCE_EXTRA, false);
                    handler.register(ENTITY_SENSE_EXTRA, false);
                    handler.register(SUPER_JUMP_EXTRA, false);
                    handler.register(ERASE_EXTRA, false);
                    handler.register(EXTRA_REACH_EXTRA, false);
                    handler.register(SLOW_FALL_EXTRA, false);
                    handler.register(LIGHT_EXTRA, false);
                    handler.register(WATER_BREATHING_EXTRA, false);
                    handler.register(FROST_WALKER_EXTRA, false);
                }
            }
            if (handler.getEntity() instanceof net.minecraft.world.entity.LivingEntity) {
                handler.register(EOPPalladiumProperties.LIVING_CREATURE, true);
            }
        });
    }

    public static PalladiumProperty<Integer> getOrCreateEnergyProperty(String powerKey) {
        return ENERGY_PROPERTIES.computeIfAbsent(
                powerKey,
                key -> new IntegerProperty(getEnergyPropertyName(key))
        );
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

    public static PalladiumProperty<Integer> getOrCreateSkillPointProperty(String powerKey) {
        return SKILL_POINT_PROPERTIES.computeIfAbsent(
                powerKey,
                key -> new IntegerProperty(getSkillPointPropertyName(key))
        );
    }

    public static String getSkillPointPropertyName(String powerKey) {
        return "eop_" + powerKey + "_skill_points";
    }

    public static String getEnergyPropertyName(String powerKey) {
        return "eop_" + powerKey + "_energy";
    }

    public static int getSkillPoints(Entity entity, String powerKey) {
        return getOrCreateSkillPointProperty(powerKey).get(entity);
    }

    public static void setSkillPoints(Entity entity, String powerKey, int amount) {
        getOrCreateSkillPointProperty(powerKey).set(entity, amount);
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

    public static int getEnergy(Entity entity, String powerKey) {
        return getOrCreateEnergyProperty(powerKey).get(entity);
    }

    public static void setEnergy(Entity entity, String powerKey, int amount) {
        getOrCreateEnergyProperty(powerKey).set(entity, amount);
    }

    public static boolean hasClimbExtra(net.minecraft.world.entity.Entity entity) {return CLIMB_EXTRA.get(entity);}
    public static void setClimbExtra(net.minecraft.world.entity.Entity entity, boolean value) {CLIMB_EXTRA.set(entity, value);}
    public static boolean hasNightVisionExtra(Entity entity) {return NIGHT_VISION_EXTRA.get(entity);}
    public static void setNightVisionExtra(Entity entity, boolean value) {NIGHT_VISION_EXTRA.set(entity, value);}
    public static boolean hasSmeltingExtra(Entity entity) {return SMELTING_EXTRA.get(entity);}
    public static void setSmeltingExtra(Entity entity, boolean value) {SMELTING_EXTRA.set(entity, value);}
    public static boolean hasFireResistanceExtra(Entity entity) {return FIRE_RESISTANCE_EXTRA.get(entity);}
    public static void setFireResistanceExtra(Entity entity, boolean value) {FIRE_RESISTANCE_EXTRA.set(entity, value);}
    public static boolean hasEntitySenseExtra(Entity entity) {return ENTITY_SENSE_EXTRA.get(entity);}
    public static void setEntitySenseExtra(Entity entity, boolean value) {ENTITY_SENSE_EXTRA.set(entity, value);}
    public static boolean hasSuperJumpExtra(Entity entity) {return SUPER_JUMP_EXTRA.get(entity);}
    public static void setSuperJumpExtra(Entity entity, boolean value) {SUPER_JUMP_EXTRA.set(entity, value);}
    public static boolean hasEraseExtra(Entity entity) {return ERASE_EXTRA.get(entity);}
    public static void setEraseExtra(Entity entity, boolean value) {ERASE_EXTRA.set(entity, value);}
    public static boolean hasExtraReachExtra(Entity entity) {return EXTRA_REACH_EXTRA.get(entity);}
    public static void setExtraReachExtra(Entity entity, boolean value) {EXTRA_REACH_EXTRA.set(entity, value);}
    public static boolean hasSlowFallExtra(Entity entity) {return SLOW_FALL_EXTRA.get(entity);}
    public static void setSlowFallExtra(Entity entity, boolean value) {SLOW_FALL_EXTRA.set(entity, value);}
    public static boolean hasLightExtra(Entity entity) {return LIGHT_EXTRA.get(entity);}
    public static void setLightExtra(Entity entity, boolean value) {LIGHT_EXTRA.set(entity, value);}
    public static boolean hasWaterBreathingExtra(Entity entity) {return WATER_BREATHING_EXTRA.get(entity);}
    public static void setWaterBreathingExtra(Entity entity, boolean value) {WATER_BREATHING_EXTRA.set(entity, value);}
    public static boolean hasFrostWalkerExtra(Entity entity) {return FROST_WALKER_EXTRA.get(entity);}
    public static void setFrostWalkerExtra(Entity entity, boolean value) {FROST_WALKER_EXTRA.set(entity, value);}

}
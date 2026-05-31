package net.stonedgoldfish.eopmod.power.ability;

import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladiumcore.registry.DeferredRegister;
import net.threetag.palladiumcore.registry.RegistrySupplier;

public class EOPAbilities {

    public static final DeferredRegister<Ability> ABILITIES =
            DeferredRegister.create(EOPMod.MOD_ID, Ability.REGISTRY);

    public static final RegistrySupplier<Ability> IMMUNE_TO_EFFECT =
            ABILITIES.register("immune_to_effect", ImmuneToEffectAbility::new);
    public static final RegistrySupplier<Ability> NO_NATURAL_REGEN =
            ABILITIES.register("no_natural_regen", NoNaturalRegenAbility::new);
    public static final RegistrySupplier<Ability> GILLS =
            ABILITIES.register("gills", GillsAbility::new);
    public static final RegistrySupplier<Ability> CUSTOM_FLIGHT =
            ABILITIES.register("custom_flight", CustomFlightAbility::new);
    public static final RegistrySupplier<Ability> DASH =
            ABILITIES.register("dash", DashAbility::new);
    public static final RegistrySupplier<Ability> EXTINGUISH_FIRE =
            ABILITIES.register("extinguish_fire", ExtinguishFireAbility::new);
    public static final RegistrySupplier<Ability> AOE_DAMAGE =
            ABILITIES.register("aoe_damage", AOEDamageAbility::new);
    public static final RegistrySupplier<Ability> LINEAR_DAMAGE =
            ABILITIES.register("linear_damage", LinearDamageAbility::new);
    public static final RegistrySupplier<Ability> SPAWN_ARMOR_STAND =
            ABILITIES.register("spawn_armor_stand", SpawnArmorStandAbility::new);
    public static final RegistrySupplier<Ability> SILENT_STEPS =
            ABILITIES.register("silent_steps", SilentStepsAbility::new);
    public static final RegistrySupplier<Ability> NO_INTERACTION =
            ABILITIES.register("no_interaction", NoInteractionAbility::new);
    public static final RegistrySupplier<Ability> SCREEN_SHAKE =
            ABILITIES.register("screen_shake", ScreenShakeAbility::new);
    public static final RegistrySupplier<Ability> MULTI_ATTRIBUTE_MODIFIER =
            ABILITIES.register("multi_attribute_modifier", MultiAttributeModifierAbility::new);
    public static final RegistrySupplier<Ability> TRIGGER_ANIMATION =
            ABILITIES.register("trigger_animation", TriggerAnimationAbility::new);
    public static final RegistrySupplier<Ability> AREA_LIGHT =
            ABILITIES.register("area_light", AreaLightAbility::new);
    public static final RegistrySupplier<Ability> INFINITY_OXYGEN =
            ABILITIES.register("infinite_oxygen", InfiniteAirAbility::new);
    public static final RegistrySupplier<Ability> NO_MOVEMENT =
            ABILITIES.register("no_movement", NoMovementAbility::new);
    public static final RegistrySupplier<Ability> PARTICLE_PATTERN =
            ABILITIES.register("particle_pattern", ParticlePatternAbility::new);
    public static final RegistrySupplier<Ability> ENERGY_REGEN =
            ABILITIES.register("energy_regen", EnergyRegenAbility::new);
    public static final RegistrySupplier<Ability> FORWARD_MOTION =
            ABILITIES.register("forward_motion", ForwardMotionAbility::new);
    public static final RegistrySupplier<Ability> MAX_HUNGER =
            ABILITIES.register("max_hunger", MaxHungerAbility::new);
    public static final RegistrySupplier<Ability> HUNGER_RESISTANCE =
            ABILITIES.register("hunger_resistance", HungerResistanceAbility::new);
    public static final RegistrySupplier<Ability> SMELT =
            ABILITIES.register("smelt", SmeltAbility::new);
    public static final RegistrySupplier<Ability> AOE_COMMANDS =
            ABILITIES.register("aoe_commands", AOECommandAbility::new);
    public static final RegistrySupplier<Ability> RAYCAST_DAMAGE =
            ABILITIES.register("raycast_damage", RaycastDamageAbility::new);

    public static void init() {

    }
}
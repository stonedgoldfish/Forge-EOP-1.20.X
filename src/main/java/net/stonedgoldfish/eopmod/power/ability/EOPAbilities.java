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

    public static void init() {

    }
}
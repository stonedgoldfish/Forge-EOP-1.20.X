package net.stonedgoldfish.eopmod.power.ability;

import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladiumcore.registry.DeferredRegister;
import net.threetag.palladiumcore.registry.RegistrySupplier;

public class ModAbilities {

    public static final DeferredRegister<Ability> ABILITIES =
            DeferredRegister.create(EOPMod.MOD_ID, Ability.REGISTRY);

    public static final RegistrySupplier<Ability> SAY_HI =
            ABILITIES.register("say_hi", SayHiAbility::new);
    public static final RegistrySupplier<Ability> IMMUNE_TO_EFFECT =
            ABILITIES.register("immune_to_effect", ImmuneToEffectAbility::new);

    public static void init() {

    }
}
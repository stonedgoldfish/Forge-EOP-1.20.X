package net.stonedgoldfish.eopmod.power.condition;

import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladiumcore.registry.DeferredRegister;
import net.threetag.palladiumcore.registry.RegistrySupplier;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPConditions {

    public static final DeferredRegister<ConditionSerializer> CONDITIONS =
            DeferredRegister.create(EOPMod.MOD_ID, ConditionSerializer.REGISTRY);

    public static final RegistrySupplier<ConditionSerializer> TOGGLE_ACTIVATION =
            CONDITIONS.register("toggle_activation", ToggleActivationCondition.Serializer::new);
}
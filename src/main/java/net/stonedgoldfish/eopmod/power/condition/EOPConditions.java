package net.stonedgoldfish.eopmod.power.condition;

import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladiumcore.registry.DeferredRegister;
import net.threetag.palladiumcore.registry.RegistrySupplier;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPConditions {

    public static final DeferredRegister<ConditionSerializer> CONDITIONS =
            DeferredRegister.create(EOPMod.MOD_ID, ConditionSerializer.REGISTRY);

    public static final RegistrySupplier<ConditionSerializer> ACTIVATION =
            CONDITIONS.register("activation", ActivationCondition.Serializer::new);
    public static final RegistrySupplier<ConditionSerializer> ACTION =
            CONDITIONS.register("action", ActionCondition.Serializer::new);
    public static final RegistrySupplier<ConditionSerializer> HELD =
            CONDITIONS.register("held", HeldCondition.Serializer::new);
    public static final RegistrySupplier<ConditionSerializer> POWER_SELECTED =
            CONDITIONS.register("power_selected", PowerSelectedCondition.Serializer::new);
}
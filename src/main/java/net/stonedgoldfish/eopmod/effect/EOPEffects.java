package net.stonedgoldfish.eopmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EOPMod.MOD_ID);

    public static final RegistryObject<MobEffect> LUNAR_CLOAK =
            MOB_EFFECTS.register("lunar_cloak", LunarCloakEffect::new);
    public static final RegistryObject<MobEffect> PLANE_SHIFT =
            MOB_EFFECTS.register("plane_shift", PlaneShiftEffect::new);
    public static final RegistryObject<MobEffect> STUN =
            MOB_EFFECTS.register("stun", StunEffect::new);
    public static final RegistryObject<MobEffect> SNARE =
            MOB_EFFECTS.register("snare", SnareEffect::new);
    public static final RegistryObject<MobEffect> SILENCED =
            MOB_EFFECTS.register("silenced", SilencedEffect::new);
    public static final RegistryObject<MobEffect> BLEED =
            MOB_EFFECTS.register("bleed", BleedEffect::new);
    public static final RegistryObject<MobEffect> DISTORTED =
            MOB_EFFECTS.register("distorted", DistortedEffect::new);
    public static final RegistryObject<MobEffect> FRACTURED =
            MOB_EFFECTS.register("fractured", FracturedEffect::new);
    public static final RegistryObject<MobEffect> DISORIENTED =
            MOB_EFFECTS.register("disoriented", DisorientedEffect::new);
}
package net.stonedgoldfish.eopmod.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(
                    ForgeRegistries.PARTICLE_TYPES,
                    EOPMod.MOD_ID
            );

    public static final RegistryObject<SimpleParticleType> FALLING_BLOOD =
            PARTICLES.register("falling_blood", () ->
                    new SimpleParticleType(false)
            );
    public static final RegistryObject<SimpleParticleType> LANDING_BLOOD =
            PARTICLES.register("landing_blood", () ->
                    new SimpleParticleType(false)
            );
}
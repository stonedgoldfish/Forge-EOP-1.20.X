package net.stonedgoldfish.eopmod.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.particle.*;

@Mod.EventBusSubscriber(
        modid = EOPMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class EOPParticleEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                EOPParticles.FALLING_BLOOD.get(),
                FallingBloodParticle.Provider::new
        );
        event.registerSpriteSet(
                EOPParticles.LANDING_BLOOD.get(),
                LandingBloodParticle.Provider::new
        );
        event.registerSpriteSet(
                EOPParticles.VOID_ENERGY.get(),
                VoidEnergyParticle.Provider::new
        );
        event.registerSpriteSet(
                EOPParticles.LIGHT_ENERGY.get(),
                VoidEnergyParticle.Provider::new
        );
        event.registerSpriteSet(
                EOPParticles.DARK_ENERGY.get(),
                VoidEnergyParticle.Provider::new
        );
        event.registerSpriteSet(
                EOPParticles.ASTRAL_ENERGY.get(),
                VoidEnergyParticle.Provider::new
        );
        event.registerSpriteSet(
                EOPParticles.STAR.get(),
                VoidEnergyParticle.Provider::new
        );
        event.registerSpriteSet(
                EOPParticles.GLITCH.get(),
                GlitchParticle.Provider::new
        );
    }
}
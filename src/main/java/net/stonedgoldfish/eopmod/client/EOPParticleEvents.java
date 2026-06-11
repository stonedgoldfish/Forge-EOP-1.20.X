package net.stonedgoldfish.eopmod.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.particle.FallingBloodParticle;
import net.stonedgoldfish.eopmod.particle.EOPParticles;
import net.stonedgoldfish.eopmod.particle.LandingBloodParticle;

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
    }
}
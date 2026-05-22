package net.stonedgoldfish.eopmod.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.client.render.EOPProjectileRenderer;
import net.stonedgoldfish.eopmod.entity.EOPEntities;

@Mod.EventBusSubscriber(
        modid = EOPMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class EOPEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                EOPEntities.BASIC_PROJECTILE.get(),
                EOPProjectileRenderer::new
        );
    }
}
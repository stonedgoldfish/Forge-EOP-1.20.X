package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.ability.InfiniteAirAbility;

@Mod.EventBusSubscriber(
        modid = EOPMod.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class EOPAirSupplyClient {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {

        if (!event.getOverlay().id().getPath().equals("air_level")) {
            return;
        }

        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (InfiniteAirAbility.hasAirImmunity(player)) {
            event.setCanceled(true);
        }
    }
}
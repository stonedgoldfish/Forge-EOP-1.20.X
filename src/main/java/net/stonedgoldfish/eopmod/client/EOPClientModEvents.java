package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.client.screen.GeneticSequencerScreen;
import net.stonedgoldfish.eopmod.menu.EOPMenus;

@Mod.EventBusSubscriber(
        modid = EOPMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class EOPClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(
                    EOPMenus.GENETIC_SEQUENCER_MENU.get(),
                    GeneticSequencerScreen::new
            );
        });
    }
}
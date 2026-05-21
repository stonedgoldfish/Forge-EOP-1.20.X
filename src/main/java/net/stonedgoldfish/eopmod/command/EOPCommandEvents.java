package net.stonedgoldfish.eopmod.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID)
public class EOPCommandEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        EOPCommands.register(event.getDispatcher());
    }
}
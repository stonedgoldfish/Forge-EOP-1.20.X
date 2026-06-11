package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.SyncSelectedPowerPacket;
import net.threetag.palladium.client.screen.AbilityBarRenderer;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class EOPSelectedPowerClientSync {

    private static String lastSelectedPower = null;
    private static int forceSyncTicks = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) {
            lastSelectedPower = null;
            forceSyncTicks = 0;
            return;
        }

        if (forceSyncTicks > 0) {
            forceSyncTicks--;
            lastSelectedPower = null;
        }

        if (AbilityBarRenderer.ABILITY_LISTS == null) {
            return;
        }

        if (AbilityBarRenderer.SELECTED < 0
                || AbilityBarRenderer.SELECTED >= AbilityBarRenderer.ABILITY_LISTS.size()) {
            return;
        }

        var selected = AbilityBarRenderer.ABILITY_LISTS.get(AbilityBarRenderer.SELECTED);

        if (selected == null || selected.getPower() == null) {
            return;
        }

        String selectedPower = selected.getPower().getId().toString();

        if (selectedPower.equals(lastSelectedPower)) {
            return;
        }

        lastSelectedPower = selectedPower;

        EOPNetwork.CHANNEL.sendToServer(
                new SyncSelectedPowerPacket(selectedPower)
        );
    }

    @SubscribeEvent
    public static void onPlayerRespawn(ClientPlayerNetworkEvent.Clone event) {
        lastSelectedPower = null;
        forceSyncTicks = 20;
    }

    @SubscribeEvent
    public static void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        lastSelectedPower = null;
        forceSyncTicks = 0;
    }
}
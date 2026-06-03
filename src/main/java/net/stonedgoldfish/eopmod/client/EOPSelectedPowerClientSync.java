package net.stonedgoldfish.eopmod.client;

import net.minecraftforge.api.distmarker.Dist;
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
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
}
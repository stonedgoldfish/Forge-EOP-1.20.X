package net.stonedgoldfish.eopmod.compat.curios;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.ability.SavedRestrictSlotsAbility;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EOPCuriosEvents {

    @SubscribeEvent
    public static void onCurioEquip(CurioEquipEvent event) {
        String key = "curios:" + event.getSlotContext().identifier();

        if (SavedRestrictSlotsAbility.isRestricted(event.getEntity(), key)) {
            event.setResult(Event.Result.DENY);
        }
    }
}
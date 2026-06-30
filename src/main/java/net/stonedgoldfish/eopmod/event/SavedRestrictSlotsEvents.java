package net.stonedgoldfish.eopmod.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.ability.SavedRestrictSlotsAbility;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SavedRestrictSlotsEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SavedRestrictSlotsAbility.runWithHandRestrictionBypass(() ->
                    SavedRestrictSlotsAbility.restoreSavedItems(player)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SavedRestrictSlotsAbility.runWithHandRestrictionBypass(() ->
                    SavedRestrictSlotsAbility.restoreSavedItems(player)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        SavedRestrictSlotsAbility.runWithHandRestrictionBypass(() -> {
            SavedRestrictSlotsAbility.restoreSavedItems(event.getOriginal());
            SavedRestrictSlotsAbility.restoreSavedItems(event.getEntity());
        });
    }
}
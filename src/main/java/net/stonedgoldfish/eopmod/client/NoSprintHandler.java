package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.ability.ChargeAbility;
import net.stonedgoldfish.eopmod.power.ability.SinkAbility;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class NoSprintHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!isSprintBlocked(minecraft.player)) {
            return;
        }

        minecraft.options.keySprint.setDown(false);
        minecraft.player.setSprinting(false);
    }

    public static boolean isSprintBlocked(net.minecraft.world.entity.player.Player player) {
        return ChargeAbility.isCharging(player)
                || SinkAbility.shouldBlockSprint(player);
    }
}
package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.effect.EOPEffects;
import net.stonedgoldfish.eopmod.power.ability.ChargeAbility;
import net.stonedgoldfish.eopmod.power.ability.NoClickAbility;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class NoClickInputHandler {

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (minecraft.isPaused()) {
            return;
        }

        Player player = minecraft.player;

        if (isLeftClickBlocked(player) && event.getButton() == 0) {
            event.setCanceled(true);
            minecraft.options.keyAttack.setDown(false);
        }

        if (isRightClickBlocked(player) && event.getButton() == 1) {
            event.setCanceled(true);
            minecraft.options.keyUse.setDown(false);
        }
    }

    @SubscribeEvent
    public static void onInteractionKeyInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;

        if (event.isAttack() && isLeftClickBlocked(player)) {
            event.setSwingHand(false);
            event.setCanceled(true);
            minecraft.options.keyAttack.setDown(false);
        }

        if (event.isUseItem() && isRightClickBlocked(player)) {
            event.setSwingHand(false);
            event.setCanceled(true);
            minecraft.options.keyUse.setDown(false);
        }
    }

    public static boolean isLeftClickBlocked(Player player) {
        return player.hasEffect(EOPEffects.STUN.get())
                || ChargeAbility.isCharging(player)
                || NoClickAbility.blocksLeftClick(player);
    }

    public static boolean isRightClickBlocked(Player player) {
        return player.hasEffect(EOPEffects.STUN.get())
                || ChargeAbility.isCharging(player)
                || NoClickAbility.blocksRightClick(player);
    }
}
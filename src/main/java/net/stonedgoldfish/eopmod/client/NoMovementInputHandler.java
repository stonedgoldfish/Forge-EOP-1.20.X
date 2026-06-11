package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.effect.EOPEffects;
import net.stonedgoldfish.eopmod.power.ability.ChargeAbility;
import net.stonedgoldfish.eopmod.power.ability.NoMovementAbility;
import net.stonedgoldfish.eopmod.power.ability.SinkAbility;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class NoMovementInputHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;

        if (isForwardBlocked(player)) {
            event.getInput().up = false;
            minecraft.options.keyUp.setDown(false);
        }

        if (isBackwardBlocked(player)) {
            event.getInput().down = false;
            minecraft.options.keyDown.setDown(false);
        }

        if (isCrouchBlocked(player)) {
            event.getInput().shiftKeyDown = false;
            minecraft.options.keyShift.setDown(false);
            player.setShiftKeyDown(false);
        }

        if (isJumpBlocked(player)) {
            event.getInput().jumping = false;
        }

        if (isSprintBlocked(player)) {
            minecraft.options.keySprint.setDown(false);
            player.setSprinting(false);
        }
    }

    public static boolean isForwardBlocked(Player player) {
        return ChargeAbility.isCharging(player);
    }

    public static boolean isBackwardBlocked(Player player) {
        return ChargeAbility.isCharging(player);
    }

    public static boolean isCrouchBlocked(Player player) {
        return ChargeAbility.isCharging(player);
    }

    public static boolean isJumpBlocked(Player player) {
        return player.hasEffect(EOPEffects.STUN.get())
                || player.hasEffect(EOPEffects.SNARE.get())
                || NoMovementAbility.isFrozen(player)
                || ChargeAbility.isCharging(player)
                || SinkAbility.shouldBlockJump(player);
    }

    public static boolean isSprintBlocked(Player player) {
        return ChargeAbility.isCharging(player)
                || SinkAbility.shouldBlockSprint(player);
    }
}
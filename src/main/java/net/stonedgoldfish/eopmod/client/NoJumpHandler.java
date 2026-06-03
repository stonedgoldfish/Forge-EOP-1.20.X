package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
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
public class NoJumpHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!isJumpBlocked(minecraft.player)) {
            return;
        }

        event.getInput().jumping = false;
    }

    public static boolean isJumpBlocked(net.minecraft.world.entity.player.Player player) {
        return player.hasEffect(EOPEffects.STUN.get())
                || player.hasEffect(EOPEffects.SNARE.get())
                || NoMovementAbility.isFrozen(player)
                || ChargeAbility.isCharging(player)
                || SinkAbility.shouldBlockJump(player);
    }
}
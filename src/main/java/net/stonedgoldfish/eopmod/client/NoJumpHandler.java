package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.effect.EOPEffects;
import net.stonedgoldfish.eopmod.power.ability.NoMovementAbility;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class NoJumpHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!minecraft.player.hasEffect(EOPEffects.STUN.get())
                && !minecraft.player.hasEffect(EOPEffects.SNARE.get())
                && !NoMovementAbility.isFrozen(minecraft.player)) {
            return;
        }

        event.getInput().jumping = false;
    }
}
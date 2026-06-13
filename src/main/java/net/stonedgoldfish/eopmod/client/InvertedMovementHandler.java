package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.effect.DistortedEffect;
import net.stonedgoldfish.eopmod.effect.EOPEffects;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class InvertedMovementHandler {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!shouldInvertMovement(minecraft.player)) {
            return;
        }

        boolean up = event.getInput().up;
        boolean down = event.getInput().down;
        boolean left = event.getInput().left;
        boolean right = event.getInput().right;

        event.getInput().up = down;
        event.getInput().down = up;
        event.getInput().left = right;
        event.getInput().right = left;

        event.getInput().forwardImpulse *= -1F;
        event.getInput().leftImpulse *= -1F;
    }

    public static boolean shouldInvertMovement(Player player) {
        return player.hasEffect(EOPEffects.DISORIENTED.get())
                || DistortedEffect.isInverted(player);
    }
}
package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.player.LocalPlayer;
import net.stonedgoldfish.eopmod.effect.DistortedEffect;
import net.stonedgoldfish.eopmod.effect.EOPEffects;

public class InvertedMouseHandler {

    public static boolean shouldInvertMouse(LocalPlayer player) {
        return player != null
                && (player.hasEffect(EOPEffects.DISORIENTED.get())
                || DistortedEffect.isInverted(player));
    }
}
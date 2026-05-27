package net.stonedgoldfish.eopmod.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class EOPFlightSoundHandler {

    private static EOPFlightSound cachedSound = null;

    public static void start(Player player) {
        if (cachedSound != null) {
            cachedSound.stop = true;
        }

        cachedSound = new EOPFlightSound(player);
        Minecraft.getInstance().getSoundManager().play(cachedSound);
    }

    public static void clear() {
        cachedSound = null;
    }
}
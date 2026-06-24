package net.stonedgoldfish.eopmod.client.screen;

import net.minecraft.client.Minecraft;

public class GeneticSequencerClient {

    public static void openScreen() {
        Minecraft.getInstance().setScreen(new GeneticSequencerScreen());
    }
}
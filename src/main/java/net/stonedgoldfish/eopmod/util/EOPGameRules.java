package net.stonedgoldfish.eopmod.util;

import net.minecraft.server.MinecraftServer;

public class EOPGameRules {

    public static boolean isDestructionMode(MinecraftServer server) {
        return EOPWorldData.get(server.overworld()).isDestructionMode();
    }

    public static void setDestructionMode(MinecraftServer server, boolean value) {
        EOPWorldData.get(server.overworld()).setDestructionMode(value);
    }

    public static boolean isInfiniteEnergy(MinecraftServer server) {
        return EOPWorldData.get(server.overworld()).isInfiniteEnergy();
    }

    public static void setInfiniteEnergy(MinecraftServer server, boolean value) {
        EOPWorldData.get(server.overworld()).setInfiniteEnergy(value);
    }
}
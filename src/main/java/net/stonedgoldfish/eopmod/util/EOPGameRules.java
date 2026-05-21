package net.stonedgoldfish.eopmod.util;

public class EOPGameRules {

    private static boolean destructionMode = true;

    public static boolean isDestructionMode() {
        return destructionMode;
    }

    public static void setDestructionMode(boolean value) {
        destructionMode = value;
    }
}
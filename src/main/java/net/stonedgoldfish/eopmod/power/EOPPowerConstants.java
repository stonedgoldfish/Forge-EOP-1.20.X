package net.stonedgoldfish.eopmod.power;

public class EOPPowerConstants {

    public static final int BASE_MAX_XP = 100;
    public static final int XP_INCREASE_PER_LEVEL = 20;

    public static int getMaxXpForLevel(int level) {
        return BASE_MAX_XP + ((level - 1) * XP_INCREASE_PER_LEVEL);
    }
    public static final int MAX_LEVEL = 25;

}
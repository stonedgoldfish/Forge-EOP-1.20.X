package net.stonedgoldfish.eopmod.power;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EOPFusionRegistry {

    private static final Map<String, String> FUSIONS = new HashMap<>();

    static {
        register("flame", "marine", "flame_marine");
    }

    public static void register(String firstPower, String secondPower, String resultPower) {
        FUSIONS.put(makeKey(firstPower, secondPower), resultPower);
    }

    public static Optional<String> getFusionResult(String firstPower, String secondPower) {
        return Optional.ofNullable(FUSIONS.get(makeKey(firstPower, secondPower)));
    }

    private static String makeKey(String firstPower, String secondPower) {
        if (firstPower.compareTo(secondPower) <= 0) {
            return firstPower + "+" + secondPower;
        }

        return secondPower + "+" + firstPower;
    }
}
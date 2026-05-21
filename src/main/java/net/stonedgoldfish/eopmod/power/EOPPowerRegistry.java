package net.stonedgoldfish.eopmod.power;

import net.minecraft.util.RandomSource;

import java.util.*;

public class EOPPowerRegistry {

    public record EOPPower(
            String display,
            String key,
            int weight,
            String score,
            int titleColor,
            String powerClass,
            String subclass,
            String combatStyle,
            String difficulty
    ) {}

    private static final List<EOPPower> POWERS = new ArrayList<>();
    private static final Map<String, EOPPower> BY_KEY = new HashMap<>();

    static {
        register("Astral_Energy_Manipulation", "beyonder", 5, "AEM", 0xf7ff00, "Controller", "Zoner", "Melee/Ranged", "Easy");
        register("Plane_Manipulation", "drifter", 2, "PM", 0xccffcc, "Controller", "Disrupter", "Ranged", "Easy");
        register("Diamond_Mimicry", "fortress", 5, "DM", 0x00EDE8, "Tank", "Defender", "Melee", "Easy");
        register("Hypervelocity", "speedster", 5, "HV", 0x5994FF, "Assassin", "Scout", "Melee", "Easy");
        register("Neogenesis", "mender", 10, "NG", 0xFA82FF, "Support", "Healer", "Melee", "Easy");
        register("Hydrokinesis", "marine", 10, "HK", 0x534FFF, "Support", "Adaptable", "Melee/Ranged", "Easy");
    }

    private static void register(
            String display,
            String key,
            int weight,
            String score,
            int titleColor,
            String powerClass,
            String subclass,
            String combatStyle,
            String difficulty
    ) {
        EOPPower power = new EOPPower(
                display,
                key,
                weight,
                score,
                titleColor,
                powerClass,
                subclass,
                combatStyle,
                difficulty
        );

        POWERS.add(power);
        BY_KEY.put(key, power);
    }

    public static List<EOPPower> getAll() {
        return Collections.unmodifiableList(POWERS);
    }

    public static EOPPower getByKey(String key) {
        return BY_KEY.get(key);
    }

    public static boolean exists(String key) {
        return BY_KEY.containsKey(key);
    }

    public static String getScoreKey(String key) {
        EOPPower power = getByKey(key);
        return power != null && power.score() != null ? power.score() : key;
    }

    public static int getTitleColor(String key) {
        EOPPower power = getByKey(key);
        return power != null ? power.titleColor() : 0xFFFFFF;
    }

    public static EOPPower getRandomWeighted(RandomSource random) {
        int totalWeight = 0;

        for (EOPPower power : POWERS) {
            totalWeight += power.weight();
        }

        int chosen = random.nextInt(totalWeight);

        for (EOPPower power : POWERS) {
            chosen -= power.weight();

            if (chosen < 0) {
                return power;
            }
        }

        return POWERS.get(0);
    }
}
package net.stonedgoldfish.eopmod.power;

import net.minecraft.util.RandomSource;
import java.util.*;

public class EOPPowerRegistry {

    public record EOPPower(
            String display,
            String key,
            int weight,
            boolean energy,
            boolean hasAwakening,
            boolean fusionPower,
            boolean soloPower,
            boolean fusionComponent,
            int titleColor,
            String powerClass,
            String subclass,
            String combatStyle,
            String difficulty
    ) {}

    private static final List<EOPPower> POWERS = new ArrayList<>();
    private static final Map<String, EOPPower> BY_KEY = new HashMap<>();

    static {
        register("Astral_Energy_Manipulation", "beyonder", 5, true, true, false, false, false, 0xf7ff00, "Controller", "Zoner", "Mixed", "Easy");
        register("Plane_Manipulation", "drifter", 2, true, false, false, false, false, 0xccffcc, "Controller", "Disrupter", "Ranged", "Easy");
        register("Diamond_Mimicry", "fortress", 5, false, false, false, false, false, 0x00EDE8, "Tank", "Defender", "Melee", "Easy");
        register("Hypervelocity", "speedster", 5, true, false, false, false, false, 0x5994FF, "Assassin", "Scout", "Melee", "Easy");
        register("Neogenesis", "mender", 10, true, false, false, false, false, 0xFA82FF, "Support", "Healer", "Melee", "Easy");
        register("Hydrokinesis", "marine", 10, true, false, false, false, true, 0x534FFF, "Support", "Adaptable", "Melee/Ranged", "Easy");
    }

    private static void register(
            String display,
            String key,
            int weight,
            boolean energy,
            boolean hasAwakening,
            boolean fusionPower,
            boolean soloPower,
            boolean fusionComponent,
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
                energy,
                hasAwakening,
                fusionPower,
                soloPower,
                fusionComponent,
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

    public static int getTitleColor(String key) {
        EOPPower power = getByKey(key);
        return power != null ? power.titleColor() : 0xFFFFFF;
    }

    public static boolean hasAwakening(String key) {
        EOPPower power = getByKey(key);
        return power != null && power.hasAwakening();
    }

    public static boolean isSoloPower(String key) {
        EOPPower power = getByKey(key);
        return power != null && power.soloPower();
    }

    public static boolean isFusionComponent(String key) {
        EOPPower power = getByKey(key);
        return power != null && power.fusionComponent();
    }

    public static boolean isFusionPower(String key) {
        EOPPower power = getByKey(key);
        return power != null && power.fusionPower();
    }

    public static boolean hasEnergySystem(String key) {
        EOPPower power = getByKey(key);
        return power != null && power.energy();
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
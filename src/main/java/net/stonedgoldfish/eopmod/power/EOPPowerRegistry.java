package net.stonedgoldfish.eopmod.power;

import net.minecraft.util.RandomSource;

import java.util.*;

public class EOPPowerRegistry {

    public record EOPPower(
            String display,
            String key,
            int weight,
            String score,
            int titleColor
    ) {}

    private static final List<EOPPower> POWERS = new ArrayList<>();
    private static final Map<String, EOPPower> BY_KEY = new HashMap<>();

    static {
        register("Astral_Energy_Manipulation", "beyonder", 5, "AEM", 0xf7ff00);
        register("Plane_Manipulation", "drifter", 2, "PM", 0xccffcc);
        register("Diamond_Mimicry", "fortress", 5, "DM", 0x00EDE8);
        register("Hypervelocity", "speedster", 5, "HV", 0x5994FF);
        register("Neogenesis", "mender", 10, "NG", 0xFA82FF);
        register("Enhanced_Physical_Prowess", "champion", 2, "EPP", 0xFFFFFF);
        register("Metallurgic_Infusion", "smith", 10, "MI", 0x1C521C);
        register("Hydrokinesis", "marine", 10, "HK", 0x534FFF);
        register("Adaptation", "darwin", 2, null, 0xBD02A6);
        register("Genetic_Manipulaton", "mimic", 5, null, 0xFFFFFF);
        register("Neurochemical_Manipulation", "phobia", 5, "NM", 0xBABABA);
        register("Polymorphic_Cellular_Control", "experiment", 5, "PCC", 0x009106);
        register("Mycological_Mastery", "parasite", 10, "MM", 0xcf8e2a);
        register("Electrokinesis", "spark", 5, "EK", 0x5994FF);
        register("Pyrokinesis", "flame", 5, "PK", 0xfa9802);
        register("Cryokinesis", "icicle", 2, "CK", 0x00EDE8);
        register("Weather_Manipulation", "tempest", 2, "WM", 0x91b6cf);
        register("Reality_Manipulation", "warper", 1, "RM", 0xD90000);
        register("Lycanthropy", "canine", 10, "L", 0x575757);
        register("Geokinesis", "rock", 5, "GK", 0x786748);
        register("Stone_Mimicry", "thing", 10, "SM", 0xbdb7ac);
        register("Light_Manipulation", "photon", 10, "LM", 0xFFFFFF);
        register("Elasticity", "contortionist", 10, "E", 0x656999);
        register("Power_Erasure", "nullifier", 10, null, 0x800000);
        register("Erosion", "corrosive", 2, null, 0x6a6262);
        register("Quantum_Intangibility", "node", 10, null, 0xd1cd46);
        register("Magmakinesis", "magma", 2, "MK", 0x610e00);
        register("Vampirism", "bloodsucker", 5, "V", 0xff0000);
        register("Radiation_Emission", "reactor", 10, null, 0x84be51);
        register("Bone_Manipulation", "marrow", 10, "BM", 0xFFFFFF);
        register("Scaldweaving", "flame_marine", 5, "SW", 0xD6D6D6);
        register("Magmatic_Cellular_Control", "magma_experiment", 5, "MPCC", 0x943C2E);
        register("Plasma_Manipulation", "flame_spark", 2, "PLM", 0xF2FF00);
        register("Spatial_Manipulation", "honored_one", 2, null, 0x534FFF);
        register("Excision", "king", 2, null, 0x5b0000);
        register("Blood_Manipulation", "bloodweaver", 5, "BLM", 0xff0000);
        register("Unstoppable_Acceleration", "juggernaut", 10, null, 0xddbfa2);
        register("Nargacugamy", "monster", 5, "N", 0xaaabd8);
        register("Pyraphenyxis", "phoenix", 5, "P", 0x00EDE8);
        register("Poison_Secretion", "toxin", 5, "TS", 0x813494);
        register("Spirit_Manifestation", "veilbinder", 5, "SOM", 0xA7C5CC);
        register("Leoranthropy", "beast", 10, "LE", 0xeeab36);
        register("Sauranthropy", "reptile", 10, "SAU", 0x48bb36);
        register("Selacanthropy", "predator", 10, "SEL", 0x36639d);
        register("Tauroanthropy", "bull", 10, "TA", 0x76430d);
        register("Wendigonism", "cannibal", 5, "W", 0x904040);
        register("Chimeranthropy", "aberrant", 2, "CH", 0xb1a900);
        register("Smart_Atoms", "conqueror", 2, "SA", 0xFFFFFF);
        register("Magalathropy", "wyvern", 2, "ML", 0x825b83);
        register("Relocation", "threshold", 10, "TP", 0x800080);
        register("Vertigo_Induction", "deadfall", 10, null, 0x94b21c);
        register("Glass_Manipulation", "vitrum", 10, "GM", 0xADFFFA);
        register("Sanguine_Refraction", "bloodweaver_vitrum", 2, "SR", 0xCC2121);
        register("Clairvoyance", "oracle", 10, null, 0x3941D4);
        register("Spiritual_Energy_Manipulation", "ascendant", 2, null, 0x5237CE);
        register("Umbrakinesis", "pitch", 5, "UM", 0x3F1357);
    }

    private static void register(String display, String key, int weight, String score, int titleColor) {
        EOPPower power = new EOPPower(display, key, weight, score, titleColor);

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
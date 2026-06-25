package net.stonedgoldfish.eopmod.item;

import net.minecraft.world.item.Item;

public class GeneticChipItem extends Item {

    private final String powerKey;
    private final double successRate;

    public GeneticChipItem(Properties properties, String powerKey, double successRate) {
        super(properties);
        this.powerKey = powerKey;
        this.successRate = successRate;
    }

    public String getPowerKey() {
        return this.powerKey;
    }

    public double getSuccessRate() {
        return successRate;
    }
}
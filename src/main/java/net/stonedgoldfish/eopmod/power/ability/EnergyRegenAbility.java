package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.stonedgoldfish.eopmod.util.EOPGameRules;

public class EnergyRegenAbility extends Ability {

    public static final PalladiumProperty<Integer> AMOUNT =
            new IntegerProperty("amount")
                    .configurable("Energy gained each interval.");

    public static final PalladiumProperty<Integer> INTERVAL =
            new IntegerProperty("interval")
                    .configurable("Ticks between energy gain.");

    public static final PalladiumProperty<Boolean> ENABLE_DRAIN =
            new BooleanProperty("enabled_energy_drain")
                    .configurable("Enable passive energy drain.");

    public static final PalladiumProperty<Integer> DRAIN_AMOUNT =
            new IntegerProperty("drain_amount")
                    .configurable("Energy lost each interval.");

    public static final PalladiumProperty<Integer> DRAIN_INTERVAL =
            new IntegerProperty("drain_interval")
                    .configurable("Ticks between energy drain.");

    public static final PalladiumProperty<Integer> MAX =
            new IntegerProperty("max")
                    .configurable("Maximum energy cap.");

    public static final PalladiumProperty<Boolean> INVERT =
            new BooleanProperty("invert")
                    .configurable("If true, infiniteEnergy sets energy to 0 instead of max.");

    public EnergyRegenAbility() {
        this.withProperty(ICON, new ItemIcon(Items.LIGHT_BLUE_DYE));

        this.withProperty(AMOUNT, 5);
        this.withProperty(INTERVAL, 20);
        this.withProperty(ENABLE_DRAIN, false);
        this.withProperty(DRAIN_AMOUNT, 1);
        this.withProperty(DRAIN_INTERVAL, 20);
        this.withProperty(MAX, 100);
        this.withProperty(INVERT, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance instance, IPowerHolder holder, boolean enabled) {

        if (!enabled) return;
        if (!(entity instanceof ServerPlayer player)) return;

        String powerKey = holder.getPower().getId().getPath();

        EOPPowerRegistry.EOPPower power = EOPPowerRegistry.getByKey(powerKey);

        if (power == null || !power.energy()) {
            return;
        }

        int max = Math.max(0, instance.getProperty(MAX));
        boolean invert = instance.getProperty(INVERT);
        boolean abilityBeingUsed = player.getTags().contains("Ability.Being.Used");

        if (EOPGameRules.isInfiniteEnergy(player.getServer())) {
            EOPPalladiumProperties.setEnergy(player, powerKey, invert ? 0 : max);
            return;
        }

        String baseKey = "eop_energy_tick_" + powerKey;

        int current = EOPPalladiumProperties.getEnergy(player, powerKey);

        if (current < 0) {
            EOPPalladiumProperties.setEnergy(player, powerKey, 0);
            current = 0;
        }

        int interval = Math.max(1, instance.getProperty(INTERVAL));
        int regenTick = player.getPersistentData().getInt(baseKey + "_regen");
        regenTick++;

        if (regenTick >= interval) {
            regenTick = 0;

            if (!abilityBeingUsed) {
                int amount = Math.max(0, instance.getProperty(AMOUNT));
                int updated = Math.min(current + amount, max);

                EOPPalladiumProperties.setEnergy(player, powerKey, updated);
                current = updated;
            }
        }

        player.getPersistentData().putInt(baseKey + "_regen", regenTick);

        if (instance.getProperty(ENABLE_DRAIN)) {

            int drainInterval = Math.max(1, instance.getProperty(DRAIN_INTERVAL));
            int drainTick = player.getPersistentData().getInt(baseKey + "_drain");
            drainTick++;

            if (drainTick >= drainInterval) {
                drainTick = 0;

                if (!(invert && abilityBeingUsed)) {
                    int drainAmount = Math.max(0, instance.getProperty(DRAIN_AMOUNT));
                    int updated = Math.max(current - drainAmount, 0);

                    EOPPalladiumProperties.setEnergy(player, powerKey, updated);
                    current = updated;
                }
            }

            player.getPersistentData().putInt(baseKey + "_drain", drainTick);
        }

        if (current < 0) {
            EOPPalladiumProperties.setEnergy(player, powerKey, 0);
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Regenerates and optionally drains energy for a specific power with configurable rates and intervals.";
    }
}
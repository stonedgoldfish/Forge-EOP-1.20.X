package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HungerResistanceAbility extends Ability {

    public static final PalladiumProperty<Float> RESISTANCE =
            new FloatProperty("resistance")
                    .configurable("Percentage of hunger loss prevented. 0.0 = none, 1.0 = all.");

    private static final Map<UUID, Integer> LAST_HUNGER = new HashMap<>();
    public static void clearAll() {
        LAST_HUNGER.clear();
    }

    public HungerResistanceAbility() {
        this.withProperty(ICON, new ItemIcon(Items.GOLDEN_CARROT));
        this.withProperty(RESISTANCE, 0.5F);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {

        if (!enabled || !(entity instanceof Player player) || player.level().isClientSide) {
            return;
        }

        UUID uuid = player.getUUID();

        int currentHunger = player.getFoodData().getFoodLevel();
        int previousHunger = LAST_HUNGER.getOrDefault(uuid, currentHunger);

        if (currentHunger < previousHunger) {

            int hungerLost = previousHunger - currentHunger;

            float resistance = Math.max(0F, Math.min(1F, entry.getProperty(RESISTANCE)));

            int restored = Math.round(hungerLost * resistance);

            if (restored > 0) {
                player.getFoodData().setFoodLevel(
                        Math.min(20, currentHunger + restored)
                );
                currentHunger = player.getFoodData().getFoodLevel();
            }
        }

        LAST_HUNGER.put(uuid, currentHunger);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity instanceof Player player) {
            LAST_HUNGER.remove(player.getUUID());
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Reduces hunger loss while active.";
    }
}
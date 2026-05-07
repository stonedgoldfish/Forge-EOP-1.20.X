package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NoNaturalRegenAbility extends Ability {

    public static final PalladiumProperty<Boolean> REMOVE_HUNGER_BAR =
            new BooleanProperty("remove_hunger_bar")
                    .configurable("If true, keeps the player's hunger full and hides the hunger bar.");

    private static final Set<UUID> NO_NATURAL_REGEN = new HashSet<>();
    private static final Set<UUID> HIDDEN_HUNGER_BAR = new HashSet<>();

    public NoNaturalRegenAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ROTTEN_FLESH));
        this.withProperty(REMOVE_HUNGER_BAR, false);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled) {
            NO_NATURAL_REGEN.add(entity.getUUID());

            if (entry.getProperty(REMOVE_HUNGER_BAR) && entity instanceof Player) {
                HIDDEN_HUNGER_BAR.add(entity.getUUID());
            }
        }
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        if (entry.getProperty(REMOVE_HUNGER_BAR) && entity instanceof Player player) {
            if (!entity.level().isClientSide) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(20.0F);
                player.getFoodData().setExhaustion(0.0F);
            } else {
                HIDDEN_HUNGER_BAR.add(entity.getUUID());
            }
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        NO_NATURAL_REGEN.remove(entity.getUUID());
        HIDDEN_HUNGER_BAR.remove(entity.getUUID());
    }

    public static boolean hasNoNaturalRegen(LivingEntity entity) {
        return NO_NATURAL_REGEN.contains(entity.getUUID());
    }

    public static boolean shouldHideHungerBar(Player player) {
        return HIDDEN_HUNGER_BAR.contains(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Disables natural regeneration, with an optional setting to keep hunger full and hide the hunger bar.";
    }
}
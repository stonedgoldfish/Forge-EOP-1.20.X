package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

public class MaxHungerAbility extends Ability {

    public MaxHungerAbility() {
        this.withProperty(ICON, new ItemIcon(Items.COOKED_BEEF));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        if (player.getFoodData().getFoodLevel() < 20) {
            player.getFoodData().setFoodLevel(20);
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Keeps the player's hunger bar at maximum while active.";
    }
}
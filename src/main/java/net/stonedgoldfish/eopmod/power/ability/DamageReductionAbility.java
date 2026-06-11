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

public class DamageReductionAbility extends Ability {

    public static final PalladiumProperty<Float> REDUCTION = new FloatProperty("reduction").configurable("Incoming damage reduction percentage");
    private static final Map<UUID, Float> HIGHEST_REDUCTION = new HashMap<>();

    public DamageReductionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.SHIELD));
        this.withProperty(REDUCTION, 0.0F);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (!enabled) {
            return;
        }

        float reduction = Math.max(0.0F, Math.min(100.0F, entry.getProperty(REDUCTION)));

        HIGHEST_REDUCTION.merge(
                player.getUUID(),
                reduction,
                Math::max
        );
    }

    public static float consumeHighestReduction(Player player) {
        Float reduction = HIGHEST_REDUCTION.remove(player.getUUID());
        return reduction != null ? reduction : 0.0F;
    }

    @Override
    public String getDocumentationDescription() {
        return "Reduces incoming damage by a configurable percentage";
    }
}
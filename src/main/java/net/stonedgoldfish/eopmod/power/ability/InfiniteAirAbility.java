package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InfiniteAirAbility extends Ability {

    private static final Set<UUID> ACTIVE_PLAYERS = new HashSet<>();

    public InfiniteAirAbility() {
        this.withProperty(ICON, new ItemIcon(Items.TURTLE_HELMET));
    }

    @Override
    public void tick(
            LivingEntity entity,
            AbilityInstance entry,
            IPowerHolder holder,
            boolean enabled
    ) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        ACTIVE_PLAYERS.add(entity.getUUID());

        entity.setAirSupply(entity.getMaxAirSupply());
    }

    @Override
    public void lastTick(
            LivingEntity entity,
            AbilityInstance entry,
            IPowerHolder holder,
            boolean enabled
    ) {
        ACTIVE_PLAYERS.remove(entity.getUUID());
    }

    public static boolean hasAirImmunity(LivingEntity entity) {
        return ACTIVE_PLAYERS.contains(entity.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents the entity from losing air supply while active.";
    }
}
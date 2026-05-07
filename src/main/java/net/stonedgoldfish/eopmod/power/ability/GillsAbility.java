package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GillsAbility extends Ability {

    private static final Set<UUID> GILLS_PLAYERS = new HashSet<>();

    public GillsAbility() {
        this.withProperty(ICON, new ItemIcon(Items.TROPICAL_FISH));
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player) {
            GILLS_PLAYERS.add(entity.getUUID());
        }
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player) {
            GILLS_PLAYERS.add(entity.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide) {
            GILLS_PLAYERS.remove(entity.getUUID());
        }
    }

    public static boolean hasGills(Player player) {
        return GILLS_PLAYERS.contains(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Makes the player breathe underwater but suffocate on land.";
    }
}
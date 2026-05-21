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

public class NoInteractionAbility extends Ability {

    private static final Set<UUID> BLOCKED_PLAYERS = new HashSet<>();

    public NoInteractionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.BARRIER));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (enabled) {
            BLOCKED_PLAYERS.add(player.getUUID());
        } else {
            BLOCKED_PLAYERS.remove(player.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity instanceof Player player) {
            BLOCKED_PLAYERS.remove(player.getUUID());
        }
    }

    public static boolean isBlocked(Player player) {
        return BLOCKED_PLAYERS.contains(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents the player from interacting with blocks, entities, or items.";
    }
}
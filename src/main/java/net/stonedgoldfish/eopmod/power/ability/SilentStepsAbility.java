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

public class SilentStepsAbility extends Ability {

    private static final Set<UUID> SILENT_PLAYERS = new HashSet<>();

    public SilentStepsAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FEATHER));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (enabled) {
            SILENT_PLAYERS.add(player.getUUID());
        } else {
            SILENT_PLAYERS.remove(player.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity instanceof Player player) {
            SILENT_PLAYERS.remove(player.getUUID());
        }
    }

    public static boolean hasSilentSteps(Player player) {
        return SILENT_PLAYERS.contains(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Removes walking step sounds and sprint particles.";
    }
}
package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

import java.util.*;

public class NoMovementAbility extends Ability {

    private static final UUID NO_MOVEMENT_MODIFIER_UUID = UUID.fromString("0b12f4ce-9b7d-4d5c-9b78-2206d34c43ef");

    private static final AttributeModifier NO_MOVEMENT_MODIFIER =
            new AttributeModifier(
                    NO_MOVEMENT_MODIFIER_UUID,
                    "EOP no movement",
                    -1000.0D,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );

    private static final Map<UUID, Set<Integer>> ACTIVE_FREEZES = new HashMap<>();

    public NoMovementAbility() {
        this.withProperty(ICON, new ItemIcon(Items.COBWEB));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUUID();
        int instanceId = System.identityHashCode(entry);

        if (enabled) {
            ACTIVE_FREEZES
                    .computeIfAbsent(playerId, id -> new HashSet<>())
                    .add(instanceId);

            applyModifier(player);
        } else {
            removeInstance(player, playerId, instanceId);
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUUID();
        int instanceId = System.identityHashCode(entry);

        removeInstance(player, playerId, instanceId);
    }

    private static void removeInstance(Player player, UUID playerId, int instanceId) {
        Set<Integer> freezes = ACTIVE_FREEZES.get(playerId);

        if (freezes != null) {
            freezes.remove(instanceId);

            if (freezes.isEmpty()) {
                ACTIVE_FREEZES.remove(playerId);
            }
        }

        if (!isFrozen(player)) {
            removeModifier(player);
        }
    }

    private static void applyModifier(Player player) {
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speedAttribute == null) {
            return;
        }

        if (!speedAttribute.hasModifier(NO_MOVEMENT_MODIFIER)) {
            speedAttribute.addTransientModifier(NO_MOVEMENT_MODIFIER);
        }
    }

    private static void removeModifier(Player player) {
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speedAttribute != null && speedAttribute.hasModifier(NO_MOVEMENT_MODIFIER)) {
            speedAttribute.removeModifier(NO_MOVEMENT_MODIFIER_UUID);
        }
    }

    public static boolean isFrozen(Player player) {
        Set<Integer> freezes = ACTIVE_FREEZES.get(player.getUUID());
        return freezes != null && !freezes.isEmpty();
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents movement and jumping";
    }
}
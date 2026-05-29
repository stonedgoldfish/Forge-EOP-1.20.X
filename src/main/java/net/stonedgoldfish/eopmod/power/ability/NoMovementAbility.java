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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NoMovementAbility extends Ability {

    private static final UUID NO_MOVEMENT_MODIFIER_UUID =
            UUID.fromString("0b12f4ce-9b7d-4d5c-9b78-2206d34c43ef");

    private static final AttributeModifier NO_MOVEMENT_MODIFIER =
            new AttributeModifier(
                    NO_MOVEMENT_MODIFIER_UUID,
                    "EOP no movement",
                    -1000.0D,
                    AttributeModifier.Operation.MULTIPLY_BASE
            );

    private static final Set<UUID> FROZEN_PLAYERS = new HashSet<>();

    public NoMovementAbility() {
        this.withProperty(ICON, new ItemIcon(Items.COBWEB));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {

        if (!(entity instanceof Player player)) {
            return;
        }

        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speedAttribute == null) {
            return;
        }

        if (enabled) {
            FROZEN_PLAYERS.add(player.getUUID());

            if (!speedAttribute.hasModifier(NO_MOVEMENT_MODIFIER)) {
                speedAttribute.addTransientModifier(NO_MOVEMENT_MODIFIER);
            }
        } else {
            FROZEN_PLAYERS.remove(player.getUUID());

            if (speedAttribute.hasModifier(NO_MOVEMENT_MODIFIER)) {
                speedAttribute.removeModifier(NO_MOVEMENT_MODIFIER_UUID);
            }
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {

        if (!(entity instanceof Player player)) {
            return;
        }

        FROZEN_PLAYERS.remove(player.getUUID());

        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speedAttribute != null && speedAttribute.hasModifier(NO_MOVEMENT_MODIFIER)) {
            speedAttribute.removeModifier(NO_MOVEMENT_MODIFIER_UUID);
        }
    }

    public static boolean isFrozen(Player player) {
        return FROZEN_PLAYERS.contains(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents movement by heavily reducing movement speed and blocks jumping through the no jump handler.";
    }
}
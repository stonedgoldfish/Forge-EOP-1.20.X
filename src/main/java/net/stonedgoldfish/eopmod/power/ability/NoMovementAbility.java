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

    private static final UUID NO_MOVEMENT_SPEED_UUID =
            UUID.fromString("9f44f29e-3d7e-4e0f-8b53-d5d7d1d8a901");

    private static final AttributeModifier NO_MOVEMENT_SPEED_MODIFIER =
            new AttributeModifier(
                    NO_MOVEMENT_SPEED_UUID,
                    "No movement ability",
                    -1.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );

    private static final Set<UUID> FROZEN_PLAYERS = new HashSet<>();

    public NoMovementAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ICE));
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player player) {
            freeze(player);
        }
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player player) {
            freeze(player);

            player.setDeltaMovement(
                    0.0D,
                    Math.min(player.getDeltaMovement().y, 0.0D),
                    0.0D
            );

            player.hurtMarked = true;
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            unfreeze(player);
        }
    }

    private static void freeze(Player player) {
        FROZEN_PLAYERS.add(player.getUUID());

        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute != null && !attribute.hasModifier(NO_MOVEMENT_SPEED_MODIFIER)) {
            attribute.addTransientModifier(NO_MOVEMENT_SPEED_MODIFIER);
        }
    }

    private static void unfreeze(Player player) {
        FROZEN_PLAYERS.remove(player.getUUID());

        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute != null) {
            attribute.removeModifier(NO_MOVEMENT_SPEED_UUID);
        }
    }

    public static boolean isFrozen(Player player) {
        return FROZEN_PLAYERS.contains(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Sets movement speed to 0 and disables jumping.";
    }
}
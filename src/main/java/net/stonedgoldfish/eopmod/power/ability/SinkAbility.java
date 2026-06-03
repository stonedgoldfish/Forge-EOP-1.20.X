package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeMod;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SinkAbility extends Ability {

    private static final UUID STEP_HEIGHT_UUID =
            UUID.fromString("c92e48c1-ecfd-4470-8702-4d47db64e144");

    private static final Set<UUID> SINKING_PLAYERS = new HashSet<>();

    public SinkAbility() {
        this.withProperty(ICON, new ItemIcon(Items.SOUL_SAND));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (!enabled) {
            return;
        }

        SINKING_PLAYERS.add(player.getUUID());
        if (isFullySubmerged(player)) {
            player.setSprinting(false);
        }

        if (isFullySubmerged(player)) {
            applyStepHeight(player);
        } else {
            removeStepHeight(player);
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        SINKING_PLAYERS.remove(player.getUUID());
        removeStepHeight(player);
        player.setSprinting(false);
    }

    public static boolean isSinking(Player player) {
        return SINKING_PLAYERS.contains(player.getUUID());
    }

    private static void applyStepHeight(LivingEntity entity) {
        var attribute = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());

        if (attribute == null) {
            return;
        }

        attribute.removeModifier(STEP_HEIGHT_UUID);

        attribute.addTransientModifier(
                new AttributeModifier(
                        STEP_HEIGHT_UUID,
                        "eop_sink_step_height",
                        1.0D,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }

    private static void removeStepHeight(LivingEntity entity) {
        var attribute = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());

        if (attribute != null) {
            attribute.removeModifier(STEP_HEIGHT_UUID);
        }
    }

    public static boolean shouldBlockJump(Player player) {
        return isSinking(player)
                && (
                isFullySubmerged(player)
                        || isTouchingFluidWithTwoBlocksBelow(player)
        );
    }

    public static boolean isFullySubmerged(Player player) {
        return player.isInWaterOrBubble()
                && player.isEyeInFluidType(net.minecraftforge.common.ForgeMod.WATER_TYPE.get());
    }

    public static boolean shouldBlockSprint(Player player) {
        return isSinking(player)
                && isFullySubmerged(player);
    }

    public static boolean isTouchingFluidWithTwoBlocksBelow(Player player) {
        if (!player.isInWaterOrBubble()) {
            return false;
        }

        var level = player.level();

        net.minecraft.core.BlockPos feet = player.blockPosition();

        var fluidAtFeet = level.getFluidState(feet);

        if (fluidAtFeet.isEmpty()) {
            fluidAtFeet = level.getFluidState(feet.below());
        }

        if (fluidAtFeet.isEmpty()) {
            return false;
        }

        return level.getFluidState(feet.below()).is(fluidAtFeet.getType())
                && level.getFluidState(feet.below(2)).is(fluidAtFeet.getType());
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents sprinting and jumping while giving the player step assist.";
    }
}
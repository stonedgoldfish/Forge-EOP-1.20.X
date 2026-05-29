package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationPlaybackType;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationType;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringProperty;

public class TriggerAnimationAbility extends Ability {

    public static final PalladiumProperty<String> ANIMATION =
            new StringProperty("animation")
                    .configurable("Animation from EOPAnimationType to play.");

    public TriggerAnimationAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ARMOR_STAND));
        this.withProperty(ANIMATION, "DASH_FRONT");
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || !entity.level().isClientSide) {
            return;
        }

        String animationName = entry.getProperty(ANIMATION);

        try {
            EOPAnimationType animationType = EOPAnimationType.valueOf(animationName.toUpperCase());
            EOPAnimationHandler.play(animationType);

            if (animationType.playbackType == EOPAnimationPlaybackType.HOLD) {
                EOPAnimationHandler.setHolding(true);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {

        if (!entity.level().isClientSide) {
            return;
        }

        String animationName = entry.getProperty(ANIMATION);

        try {
            EOPAnimationType animationType =
                    EOPAnimationType.valueOf(animationName.toUpperCase());

            if (animationType.playbackType == EOPAnimationPlaybackType.HOLD) {
                EOPAnimationHandler.stopHolding();
            }

        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Triggers a client-side EOP animation.";
    }
}
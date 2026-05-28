package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.stonedgoldfish.eopmod.client.animation.anim.EOPDashAnimation;
import net.stonedgoldfish.eopmod.client.animation.anim.RightArmShootAnimation;
import net.stonedgoldfish.eopmod.client.animation.anim.RightArmSwipeAnimation;
import net.stonedgoldfish.eopmod.client.animation.anim.TransformAnimation;
import net.threetag.palladium.client.model.animation.PalladiumAnimation;

public class EOPPlayerAnimation extends PalladiumAnimation {

    public EOPPlayerAnimation(int priority) {
        super(priority);
    }

    @Override
    public void animate(
            Builder builder,
            AbstractClientPlayer player,
            HumanoidModel<?> model,
            FirstPersonContext firstPersonContext,
            float partialTicks
    ) {

        float anim = EOPAnimationHandler.getProgress(partialTicks);

        if (anim <= 0.001F) {
            return;
        }

        boolean firstPerson = firstPersonContext.firstPerson();

        switch (EOPAnimationHandler.getCurrentAnimation()) {

            case DASH_FRONT -> {
                if (!firstPerson) {
                    EOPDashAnimation.front(builder, anim);
                }
            }
            case DASH_LEFT -> {
                if (!firstPerson) {
                    EOPDashAnimation.left(builder, anim);
                }
            }
            case DASH_RIGHT -> {
                if (!firstPerson) {
                    EOPDashAnimation.right(builder, anim);
                }
            }
            case DASH_BACK -> {
                if (!firstPerson) {
                    EOPDashAnimation.back(builder, anim);
                }
            }

            case RIGHT_ARM_SWIPE -> {
                if (firstPerson) {
                    RightArmSwipeAnimation.animateFirstPerson(builder, anim);
                } else {
                    RightArmSwipeAnimation.animate(builder, anim);
                }
            }

            case SHOOT -> {
                if (firstPerson) {
                    RightArmShootAnimation.animateFirstPerson(builder, anim);
                } else {
                    RightArmShootAnimation.animate(builder, anim);
                }
            }

            case TRANSFORM -> {
                if (firstPerson) {
                    TransformAnimation.animateFirstPerson(
                            builder,
                            EOPAnimationHandler.getPhase(),
                            anim
                    );
                } else {
                    TransformAnimation.animate(
                            builder,
                            EOPAnimationHandler.getPhase(),
                            anim
                    );
                }
            }

            default -> {
            }
        }
    }
}
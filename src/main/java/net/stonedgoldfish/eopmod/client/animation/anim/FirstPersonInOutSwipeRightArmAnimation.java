package net.stonedgoldfish.eopmod.client.animation.anim;

import net.minecraft.util.Mth;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class FirstPersonInOutSwipeRightArmAnimation {

    public static void animateFirstPerson(
            Builder builder,
            EOPAnimationHandler.Phase phase,
            float anim
    ) {
        switch (phase) {
            case PHASE_1 -> animateFirstPersonPhaseOne(builder, anim);
            case PHASE_2 -> animateFirstPersonPhaseTwo(builder, anim);
            case RETURN -> animateFirstPersonReturn(builder, anim);
            default -> {
            }
        }
    }

    private static void animateFirstPersonPhaseOne(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-40F)
                .setYRotShortestDegrees(-35F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animateFirstPersonPhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -40F, 60F))
                .setYRotShortestDegrees(Mth.lerp(anim, -35F, -35F))
                .animate(Easing.INOUTCUBIC, 1.0F);
    }

    private static void animateFirstPersonReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(60F)
                .setYRotShortestDegrees(-35F)
                .animate(Easing.INOUTCUBIC, reverse);

    }
}
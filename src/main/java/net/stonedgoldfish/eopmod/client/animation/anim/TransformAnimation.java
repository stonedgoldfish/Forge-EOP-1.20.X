package net.stonedgoldfish.eopmod.client.animation.anim;

import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class TransformAnimation {

    public static void animate(
            Builder builder,
            EOPAnimationHandler.Phase phase,
            float anim
    ) {

        switch (phase) {

            case PHASE_1 -> animatePhaseOne(builder, anim);

            case PHASE_2,
                 RETURN -> animatePhaseTwo(builder, anim);

            default -> {
            }
        }
    }

    public static void animateFirstPerson(
            Builder builder,
            EOPAnimationHandler.Phase phase,
            float anim
    ) {

        switch (phase) {

            case PHASE_1 -> animateFirstPersonPhaseOne(builder, anim);

            case PHASE_2,
                 RETURN -> animateFirstPersonPhaseTwo(builder, anim);

            default -> {
            }
        }
    }

    // =========================
    // THIRD PERSON
    // =========================

    private static void animatePhaseOne(Builder builder, float anim) {

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-75F)
                .setYRotShortestDegrees(-15F)
                .setZRotShortestDegrees(10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-20F)
                .setYRotShortestDegrees(10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(-10F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animatePhaseTwo(Builder builder, float anim) {

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-145F)
                .setYRotShortestDegrees(-40F)
                .setZRotShortestDegrees(25F)
                .setZ(-2F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-60F)
                .setYRotShortestDegrees(20F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(-25F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    // =========================
    // FIRST PERSON
    // =========================

    private static void animateFirstPersonPhaseOne(
            Builder builder,
            float anim
    ) {

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-45F)
                .setYRotShortestDegrees(-10F)
                .setZRotShortestDegrees(5F)
                .setX(-1F)
                .setY(1F)
                .setZ(-2F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animateFirstPersonPhaseTwo(
            Builder builder,
            float anim
    ) {

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-95F)
                .setYRotShortestDegrees(-20F)
                .setZRotShortestDegrees(15F)
                .setX(-2F)
                .setY(2F)
                .setZ(-4F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
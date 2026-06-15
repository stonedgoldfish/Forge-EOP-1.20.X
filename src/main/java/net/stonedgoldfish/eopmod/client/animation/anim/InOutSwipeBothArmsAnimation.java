package net.stonedgoldfish.eopmod.client.animation.anim;

import net.minecraft.util.Mth;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class InOutSwipeBothArmsAnimation {

    public static void animate(
            Builder builder,
            EOPAnimationHandler.Phase phase,
            float anim
    ) {
        switch (phase) {
            case PHASE_1 -> animatePhaseOne(builder, anim);
            case PHASE_2 -> animatePhaseTwo(builder, anim);
            case RETURN -> animateReturn(builder, anim);
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
            case PHASE_2 -> animateFirstPersonPhaseTwo(builder, anim);
            case RETURN -> animateFirstPersonReturn(builder, anim);
            default -> {
            }
        }
    }

    private static void animatePhaseOne(Builder builder, float anim) {

        builder.get(PlayerModelPart.HEAD)
                .setZ(-2.8F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(11F)
                .setZ(-2.8F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-98F)
                .setYRotShortestDegrees(-42F)
                .setZRotShortestDegrees(7F)
                .setZ(-3.4F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-98F)
                .setYRotShortestDegrees(42F)
                .setZRotShortestDegrees(-7F)
                .setZ(-3.4F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animatePhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setZ(Mth.lerp(anim, -2.8F, -1.7F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(Mth.lerp(anim, 11F, 7F))
                .setZ(Mth.lerp(anim, -2.8F, -1.7F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -98F, 55F))
                .setYRotShortestDegrees(Mth.lerp(anim, -42F, -42F))
                .setZRotShortestDegrees(Mth.lerp(anim, 7F, 7F))
                .setZ(Mth.lerp(anim, -3.4F, -3.6F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -98F, 55F))
                .setYRotShortestDegrees(Mth.lerp(anim, 42F, 42F))
                .setZRotShortestDegrees(Mth.lerp(anim, -7F, -7F))
                .setZ(Mth.lerp(anim, -3.4F, -3.6F))
                .animate(Easing.INOUTCUBIC, 1.0F);

    }

    private static void animateReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.HEAD)
                .setZ(-1.7F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(7F)
                .setZ(-1.7F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(55F)
                .setYRotShortestDegrees(-42F)
                .setZRotShortestDegrees(7F)
                .setZ(-3.6F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(55F)
                .setYRotShortestDegrees(42F)
                .setZRotShortestDegrees(-7F)
                .setZ(-3.6F)
                .animate(Easing.INOUTCUBIC, reverse);
    }

    private static void animateFirstPersonPhaseOne(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-40F)
                .setYRotShortestDegrees(-35F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-40F)
                .setYRotShortestDegrees(35F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animateFirstPersonPhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -40F, 60F))
                .setYRotShortestDegrees(Mth.lerp(anim, -35F, -35F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -40F, 60F))
                .setYRotShortestDegrees(Mth.lerp(anim, 35F, 35F))
                .animate(Easing.INOUTCUBIC, 1.0F);
    }

    private static void animateFirstPersonReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(60F)
                .setYRotShortestDegrees(-35F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(60F)
                .setYRotShortestDegrees(35F)
                .animate(Easing.INOUTCUBIC, reverse);
    }
}
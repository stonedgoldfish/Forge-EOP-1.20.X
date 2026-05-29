package net.stonedgoldfish.eopmod.client.animation.anim;

import net.minecraft.util.Mth;
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

        builder.get(PlayerModelPart.BODY)
                .setY(10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.HEAD)
                .setXRotShortestDegrees(70F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-35F)
                .setYRotShortestDegrees(23F)
                .setZRotShortestDegrees(-28F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-35F)
                .setYRotShortestDegrees(-23F)
                .setZRotShortestDegrees(28F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(20F)
                .setZRotShortestDegrees(0F)
                .setY(5F)
                .setZ(-3.8F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(20F)
                .setZRotShortestDegrees(0F)
                .setY(5F)
                .setZ(-3.8F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animatePhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.BODY)
                .setY(Mth.lerp(anim, 10F, 10F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.HEAD)
                .setXRotShortestDegrees(Mth.lerp(anim, 70F, -15F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -35F, -30F))
                .setYRotShortestDegrees(Mth.lerp(anim, 23F, 0F))
                .setZRotShortestDegrees(Mth.lerp(anim, -28F, 62F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -35F, -30F))
                .setYRotShortestDegrees(Mth.lerp(anim, -23F, 0F))
                .setZRotShortestDegrees(Mth.lerp(anim, 28F, -62F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(Mth.lerp(anim, 20F, 0F))
                .setZRotShortestDegrees(Mth.lerp(anim, 0F, 15F))
                .setY(Mth.lerp(anim, 5F, 12F))
                .setZ(Mth.lerp(anim, -3.8F, 0F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(Mth.lerp(anim, 20F, 0F))
                .setZRotShortestDegrees(Mth.lerp(anim, 0F, -15F))
                .setY(Mth.lerp(anim, 5F, 12F))
                .setZ(Mth.lerp(anim, -3.8F, 0F))
                .animate(Easing.INOUTCUBIC, 1.0F);

    }

    private static void animateReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.BODY)
                .setY(10F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.HEAD)
                .setXRotShortestDegrees(-15F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-30F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(62F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-30F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(-62F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(0F)
                .setZRotShortestDegrees(15F)
                .setY(12F)
                .setZ(0F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(0F)
                .setZRotShortestDegrees(-15F)
                .setY(12F)
                .setZ(0F)
                .animate(Easing.INOUTCUBIC, reverse);
    }

    private static void animateFirstPersonPhaseOne(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-35F)
                .setYRotShortestDegrees(23F)
                .setZRotShortestDegrees(-28F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animateFirstPersonPhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -35F, -30F))
                .setYRotShortestDegrees(Mth.lerp(anim, 23F, 0F))
                .setZRotShortestDegrees(Mth.lerp(anim, -28F, 62F))
                .animate(Easing.INOUTCUBIC, 1.0F);
    }

    private static void animateFirstPersonReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-30F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(62F)
                .animate(Easing.INOUTCUBIC, reverse);
    }
}
package net.stonedgoldfish.eopmod.client.animation.anim;

import net.minecraft.util.Mth;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class Create2Animation {

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

    private static void animatePhaseOne(Builder builder, float anim) {

        builder.get(PlayerModelPart.BODY)
                .setY(7F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.HEAD)
                .setZ(2.8F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(-11F)
                .setZ(2.8F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(0F)
                .setYRotShortestDegrees(88F)
                .setZRotShortestDegrees(90F)
                .setZ(2.6F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(0F)
                .setYRotShortestDegrees(-88F)
                .setZRotShortestDegrees(-90F)
                .setZ(2.6F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(0F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(0F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(-10F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animatePhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.BODY)
                .setY(Mth.lerp(anim, 7F, 7F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.HEAD)
                .setZ(Mth.lerp(anim, 2.8F, -2.2F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(Mth.lerp(anim, -11F, 9F))
                .setZ(Mth.lerp(anim, 2.8F, -2.2F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, 0F, -89F))
                .setYRotShortestDegrees(Mth.lerp(anim, 88F, -22F))
                .setZRotShortestDegrees(Mth.lerp(anim, 90F, -1F))
                .setZ(Mth.lerp(anim, 2.6F, -2.3F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, 0F, -89F))
                .setYRotShortestDegrees(Mth.lerp(anim, -88F, 22F))
                .setZRotShortestDegrees(Mth.lerp(anim, -90F, 1F))
                .setZ(Mth.lerp(anim, 2.6F, -2.3F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(Mth.lerp(anim, 0F, 0F))
                .setYRotShortestDegrees(Mth.lerp(anim, 0F, 0F))
                .setZRotShortestDegrees(Mth.lerp(anim, 10F, 10F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(Mth.lerp(anim, 0F, 0F))
                .setYRotShortestDegrees(Mth.lerp(anim, 0F, 0F))
                .setZRotShortestDegrees(Mth.lerp(anim, -10F, -10F))
                .animate(Easing.INOUTCUBIC, 1.0F);

    }

    private static void animateReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.BODY)
                .setY(7F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.HEAD)
                .setZ(-2.2F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(9F)
                .setZ(-2.2F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-89F)
                .setYRotShortestDegrees(-22F)
                .setZRotShortestDegrees(-1F)
                .setZ(-2.3F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-89F)
                .setYRotShortestDegrees(22F)
                .setZRotShortestDegrees(1F)
                .setZ(-2.3F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(0F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(10F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(0F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(-10F)
                .animate(Easing.INOUTCUBIC, reverse);
    }
}
package net.stonedgoldfish.eopmod.client.animation.anim;

import net.minecraft.util.Mth;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class CreateAnimation {

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

        builder.get(PlayerModelPart.HEAD)
                .setZ(-1.5F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(-25F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-103F)
                .setZ(-2F)
                .setY(4F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(13F)
                .setZRotShortestDegrees(-11F)
                .setZ(2F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animatePhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setZ(Mth.lerp(anim, -1.5F, 1F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(Mth.lerp(anim, -25F, 12.5F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, -103F, -114F))
                .setZ(Mth.lerp(anim, -2F, 4.2F))
                .setY(Mth.lerp(anim, 4F, 3.8F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(Mth.lerp(anim, 13F, -14F))
                .setZRotShortestDegrees(Mth.lerp(anim, -11F, -14F))
                .setZ(Mth.lerp(anim, 2F, -1.3F))
                .animate(Easing.INOUTCUBIC, 1.0F);

    }

    private static void animateReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.HEAD)
                .setZ(1F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(12.5F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-114F)
                .setZ(4.2F)
                .setY(3.8F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-14F)
                .setZRotShortestDegrees(-14F)
                .setZ(-1.3F)
                .animate(Easing.INOUTCUBIC, reverse);
    }
}
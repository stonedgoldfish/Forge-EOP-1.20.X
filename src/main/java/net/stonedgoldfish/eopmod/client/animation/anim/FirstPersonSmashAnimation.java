package net.stonedgoldfish.eopmod.client.animation.anim;

import net.minecraft.util.Mth;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class FirstPersonSmashAnimation {

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
                .setYRotShortestDegrees(30F)
                .setZRotShortestDegrees(40F)
                .setX(-20F)
                .setZ(-10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setYRotShortestDegrees(-30F)
                .setZRotShortestDegrees(-40F)
                .setX(20F)
                .setZ(-10F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    private static void animateFirstPersonPhaseTwo(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setYRotShortestDegrees(Mth.lerp(anim, 30F, 30F))
                .setZRotShortestDegrees(Mth.lerp(anim, 40F, -50F))
                .setX(Mth.lerp(anim, -20F, 20F))
                .setZ(Mth.lerp(anim, -10F, 10F))
                .animate(Easing.INOUTCUBIC, 1.0F);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setYRotShortestDegrees(Mth.lerp(anim, -30F, -30F))
                .setZRotShortestDegrees(Mth.lerp(anim, -40F, 50F))
                .setX(Mth.lerp(anim, 20F, -20F))
                .setZ(Mth.lerp(anim, -10F, 10F))
                .animate(Easing.INOUTCUBIC, 1.0F);
    }

    private static void animateFirstPersonReturn(Builder builder, float anim) {
        float reverse = 1.0F - anim;

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setYRotShortestDegrees(30F)
                .setZRotShortestDegrees(-50F)
                .setX(20F)
                .setZ(10F)
                .animate(Easing.INOUTCUBIC, reverse);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setYRotShortestDegrees(-30F)
                .setZRotShortestDegrees(50F)
                .setX(-20F)
                .setZ(10F)
                .animate(Easing.INOUTCUBIC, reverse);
    }
}
package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class TransformAnimation {

    public static void animate(Builder builder, float anim) {
        builder.get(PlayerModelPart.BODY)
                .setY(20F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(13F)
                .setZRotShortestDegrees(60F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(13F)
                .setZRotShortestDegrees(-60F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setXRotShortestDegrees(13F)
                .setYRotShortestDegrees(13F)
                .setZRotShortestDegrees(11F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setXRotShortestDegrees(0F)
                .setYRotShortestDegrees(0F)
                .setZRotShortestDegrees(-11F)
                .animate(Easing.INOUTCUBIC, anim);

    }

    public static void animateFirstPerson(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setZRotShortestDegrees(10F)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setZRotShortestDegrees(-10F)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
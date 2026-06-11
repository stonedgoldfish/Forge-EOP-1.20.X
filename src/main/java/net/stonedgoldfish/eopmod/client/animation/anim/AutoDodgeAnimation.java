package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class AutoDodgeAnimation {

    public static void animateBack(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setY(2.3F)
                .setZ(10F)
                .setZRotShortestDegrees(10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(-47F)
                .setZ(10F)
                .setY(2F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setY(2.3F)
                .setZ(9.5F)
                .setXRotShortestDegrees(-59F)
                .setZRotShortestDegrees(18F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setY(2.3F)
                .setZ(9.5F)
                .setXRotShortestDegrees(-59F)
                .setZRotShortestDegrees(-18F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setZRotShortestDegrees(13F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setZRotShortestDegrees(-13F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void animateLeft(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setX(3F)
                .setYRotShortestDegrees(31F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(43F)
                .setX(3F)
                .setZ(2F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setX(-1.9F)
                .setZ(5.6F)
                .setXRotShortestDegrees(12F)
                .setYRotShortestDegrees(30F)
                .setZRotShortestDegrees(17F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setX(7F)
                .setZ(-2.1F)
                .setXRotShortestDegrees(-20F)
                .setYRotShortestDegrees(30F)
                .setZRotShortestDegrees(-20F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setX(1.8F)
                .setZ(4F)
                .setYRotShortestDegrees(40F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setX(4.8F)
                .setZ(1F)
                .setYRotShortestDegrees(13F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void animateRight(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setX(-3F)
                .setZ(2.2F)
                .setYRotShortestDegrees(-31F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(-43F)
                .setX(-3F)
                .setZ(2F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setX(-7F)
                .setZ(-2.1F)
                .setXRotShortestDegrees(-20F)
                .setYRotShortestDegrees(-30F)
                .setZRotShortestDegrees(20F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setX(1.9F)
                .setZ(5.6F)
                .setXRotShortestDegrees(12F)
                .setYRotShortestDegrees(-30F)
                .setZRotShortestDegrees(-17F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_LEG)
                .setX(-4.8F)
                .setZ(1F)
                .setYRotShortestDegrees(-13F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_LEG)
                .setX(-1.8F)
                .setZ(4F)
                .setYRotShortestDegrees(-40F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
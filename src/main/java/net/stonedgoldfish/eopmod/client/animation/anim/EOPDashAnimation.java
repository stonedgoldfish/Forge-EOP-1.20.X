package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class EOPDashAnimation {

    public static void front(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(25F)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(65F)
                .setZRotShortestDegrees(10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(65F)
                .setZRotShortestDegrees(-10F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void left(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setX(1F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(10F)
                .setZRotShortestDegrees(15F)
                .setYRotShortestDegrees(45F)
                .setX(2)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setZRotShortestDegrees(55F)
                .setXRotShortestDegrees(0F)
                .setZ(4)
                .setX(-3)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setZRotShortestDegrees(35F)
                .setXRotShortestDegrees(0F)
                .setZ(-4)
                .setX(3)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void right(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setX(-1F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(10F)
                .setZRotShortestDegrees(-15F)
                .setYRotShortestDegrees(-45F)
                .setX(-2)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setZRotShortestDegrees(-35F)
                .setXRotShortestDegrees(0F)
                .setZ(-4)
                .setX(-3)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setZRotShortestDegrees(-55F)
                .setXRotShortestDegrees(0F)
                .setZ(4)
                .setX(3)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void back(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setZ(3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(-28F)
                .setZ(3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-45F)
                .setZRotShortestDegrees(8F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-45F)
                .setZRotShortestDegrees(-8F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
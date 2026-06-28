package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class EOPClawDashAnimation {

    public static void front(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setXRotShortestDegrees(25F)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-105F)
                .setYRotShortestDegrees(-25F)
                .setZRotShortestDegrees(35F)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-105F)
                .setYRotShortestDegrees(25F)
                .setZRotShortestDegrees(-35F)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void left(Builder builder, float anim) {
        EOPDashAnimation.left(builder, anim);
    }

    public static void right(Builder builder, float anim) {
        EOPDashAnimation.right(builder, anim);
    }

    public static void back(Builder builder, float anim) {
        EOPDashAnimation.back(builder, anim);
    }
}
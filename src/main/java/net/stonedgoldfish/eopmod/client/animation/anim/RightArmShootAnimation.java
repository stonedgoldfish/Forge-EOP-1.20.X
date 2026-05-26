package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class RightArmShootAnimation {

    public static void animate(Builder builder, float anim) {
        builder.get(PlayerModelPart.HEAD)
                .setZ(-2F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-93F)
                .setYRotShortestDegrees(-1F)
                .setZRotShortestDegrees(17F)
                .setZ(-6.5F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(45F)
                .setZRotShortestDegrees(-17F)
                .setZ(3.6F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(-51F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void animateFirstPerson(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setZRotShortestDegrees(-35F)
                .setZ(-5F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class RedAnimation {

    public static void animate(Builder builder, float anim) {

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-85F)
                .setYRotShortestDegrees(-17F)
                .setZRotShortestDegrees(1.7F)
                .setZ(-1)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-85F)
                .setYRotShortestDegrees(47F)
                .setZRotShortestDegrees(0.5F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void animateFirstPerson(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setZRotShortestDegrees(-35F)
                .setZ(-2F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(-88F)
                .setZRotShortestDegrees(25F)
                .setZ(1F)
                .animate(Easing.INOUTCUBIC, anim);

    }
}
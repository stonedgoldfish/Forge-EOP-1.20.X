package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class RightArmHoldAnimation {

    public static void animate(Builder builder, float anim) {

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-90F)
                .setYRotShortestDegrees(-10F)
                .setZRotShortestDegrees(5F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void animateFirstPerson(Builder builder, float anim) {

        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-60F)
                .setYRotShortestDegrees(-5F)
                .setZRotShortestDegrees(2F)
                .setX(-1F)
                .setY(1F)
                .setZ(-2F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
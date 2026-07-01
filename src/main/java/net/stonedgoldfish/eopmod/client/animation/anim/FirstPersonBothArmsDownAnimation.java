package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class FirstPersonBothArmsDownAnimation {

    public static void animateFirstPerson(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setYRotShortestDegrees(30F)
                .setZRotShortestDegrees(-50F)
                .setX(20F)
                .setZ(10F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setYRotShortestDegrees(-30F)
                .setZRotShortestDegrees(50F)
                .setX(-20F)
                .setZ(10F)
                .animate(Easing.INOUTCUBIC, anim);

    }
}
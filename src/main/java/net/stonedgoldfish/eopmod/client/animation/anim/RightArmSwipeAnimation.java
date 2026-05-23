package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class RightArmSwipeAnimation {

    public static void animate(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-121F)
                .setYRotShortestDegrees(-49F)
                .setZRotShortestDegrees(12F)
                .setZ(-2.75F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.LEFT_ARM)
                .setXRotShortestDegrees(9F)
                .setZRotShortestDegrees(-23F)
                .setZ(2.75F)
                .animate(Easing.INOUTCUBIC, anim);

        builder.get(PlayerModelPart.CHEST)
                .setYRotShortestDegrees(-25F)
                .animate(Easing.INOUTCUBIC, anim);
    }

    public static void animateFirstPerson(Builder builder, float anim) {
        builder.get(PlayerModelPart.RIGHT_ARM)
                .setXRotShortestDegrees(-55F)
                .setYRotShortestDegrees(-15F)
                .setZRotShortestDegrees(10F)
                .setX(-2F)
                .setY(1F)
                .setZ(-3F)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
package net.stonedgoldfish.eopmod.client.animation.anim;

import net.threetag.palladium.client.model.animation.PalladiumAnimation.Builder;
import net.threetag.palladium.client.model.animation.PalladiumAnimation.PlayerModelPart;
import net.threetag.palladium.util.Easing;

public class ThirdPersonAnimation {

    public static void animate(Builder builder, float anim) {

        builder.get(PlayerModelPart.CHEST)
                .setX(0)
                .animate(Easing.INOUTCUBIC, anim);
    }
}
package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.threetag.palladium.client.model.animation.PalladiumAnimation;
import net.threetag.palladium.util.Easing;

public class EOPDashAnimation extends PalladiumAnimation {

    public EOPDashAnimation(int priority) {
        super(priority);
    }

    @Override
    public void animate(
            Builder builder,
            AbstractClientPlayer player,
            HumanoidModel<?> model,
            FirstPersonContext firstPersonContext,
            float partialTicks
    ) {
        if (firstPersonContext.firstPerson()) {
            return;
        }

        float dashAnim = EOPAnimationHandler.getProgress(partialTicks);

        if (dashAnim <= 0.001F) {
            return;
        }

        switch (EOPAnimationHandler.getCurrentAnimation()) {

            case DASH_FRONT -> {
                builder.get(PlayerModelPart.HEAD)
                        .setZ(-3F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.CHEST)
                        .setXRotShortestDegrees(25F)
                        .setZ(-3F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.RIGHT_ARM)
                        .setXRotShortestDegrees(65F)
                        .setZRotShortestDegrees(10F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.LEFT_ARM)
                        .setXRotShortestDegrees(65F)
                        .setZRotShortestDegrees(-10F)
                        .animate(Easing.INOUTCUBIC, dashAnim);
            }

            case DASH_LEFT -> {
                builder.get(PlayerModelPart.HEAD)
                        .setX(1F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.CHEST)
                        .setXRotShortestDegrees(10F)
                        .setZRotShortestDegrees(15F)
                        .setYRotShortestDegrees(45F)
                        .setX(2)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.RIGHT_ARM)
                        .setZRotShortestDegrees(55F)
                        .setXRotShortestDegrees(0F)
                        .setZ(4)
                        .setX(-3)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.LEFT_ARM)
                        .setZRotShortestDegrees(35F)
                        .setXRotShortestDegrees(0F)
                        .setZ(-4)
                        .setX(3)
                        .animate(Easing.INOUTCUBIC, dashAnim);
            }

            case DASH_RIGHT -> {
                builder.get(PlayerModelPart.HEAD)
                        .setX(-1F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.CHEST)
                        .setXRotShortestDegrees(10F)
                        .setZRotShortestDegrees(-15F)
                        .setYRotShortestDegrees(-45F)
                        .setX(-2)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.RIGHT_ARM)
                        .setZRotShortestDegrees(-35F)
                        .setXRotShortestDegrees(0F)
                        .setZ(-4)
                        .setX(-3)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.LEFT_ARM)
                        .setZRotShortestDegrees(-55F)
                        .setXRotShortestDegrees(0F)
                        .setZ(4)
                        .setX(3)
                        .animate(Easing.INOUTCUBIC, dashAnim);
            }

            case DASH_BACK -> {
                builder.get(PlayerModelPart.HEAD)
                        .setZ(3F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.CHEST)
                        .setXRotShortestDegrees(-28F)
                        .setZ(3F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.RIGHT_ARM)
                        .setXRotShortestDegrees(-45F)
                        .setZRotShortestDegrees(8F)
                        .animate(Easing.INOUTCUBIC, dashAnim);

                builder.get(PlayerModelPart.LEFT_ARM)
                        .setXRotShortestDegrees(-45F)
                        .setZRotShortestDegrees(-8F)
                        .animate(Easing.INOUTCUBIC, dashAnim);
            }

            default -> {
            }
        }
    }
}
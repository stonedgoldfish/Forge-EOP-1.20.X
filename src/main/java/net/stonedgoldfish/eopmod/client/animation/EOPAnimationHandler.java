package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.util.Mth;

public class EOPAnimationHandler {

    private static EOPAnimationType currentAnimation = EOPAnimationType.NONE;

    private static float progress = 0.0F;
    private static float previousProgress = 0.0F;
    private static float target = 0.0F;

    public static void play(EOPAnimationType animationType) {
        currentAnimation = animationType;

        progress = 0.0F;
        previousProgress = 0.0F;

        target = 1.0F;
    }

    public static void tick() {
        previousProgress = progress;

        float enterSpeed = getEnterSpeed(currentAnimation);
        float returnSpeed = getReturnSpeed(currentAnimation);

        if (target > progress) {
            progress = Mth.lerp(enterSpeed, progress, target);
        } else {
            progress = Mth.lerp(returnSpeed, progress, target);
        }

        if (target > 0.0F && progress > 0.85F) {
            target = 0.0F;
        }

        if (target == 0.0F && progress < 0.01F) {
            progress = 0.0F;
            previousProgress = 0.0F;

            currentAnimation = EOPAnimationType.NONE;
        }
    }

    private static float getEnterSpeed(EOPAnimationType type) {
        return switch (type) {

            case RIGHT_ARM_SWIPE -> 0.28F;
            case SHOOT -> 0.19F;
            case TRANSFORM -> 0.11F;
            case DASH_FRONT,
                 DASH_LEFT,
                 DASH_RIGHT,
                 DASH_BACK -> 0.35F;

            default -> 0.30F;
        };
    }

    private static float getReturnSpeed(EOPAnimationType type) {
        return switch (type) {

            case RIGHT_ARM_SWIPE -> 0.08F;
            case SHOOT -> 0.11F;
            case TRANSFORM -> 0.11F;
            case DASH_FRONT,
                 DASH_LEFT,
                 DASH_RIGHT,
                 DASH_BACK -> 0.12F;

            default -> 0.10F;
        };
    }

    public static float getProgress(float partialTicks) {
        return Mth.lerp(partialTicks, previousProgress, progress);
    }

    public static EOPAnimationType getCurrentAnimation() {
        return currentAnimation;
    }
}
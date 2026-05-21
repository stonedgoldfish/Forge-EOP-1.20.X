package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.util.Mth;

public class EOPAnimationHandler {

    private static EOPAnimationType currentAnimation = EOPAnimationType.NONE;

    private static float progress = 0.0F;
    private static float previousProgress = 0.0F;
    private static float target = 0.0F;

    private static float enterSpeed = 0.35F;
    private static float returnSpeed = 0.12F;

    public static void play(EOPAnimationType animationType) {
        currentAnimation = animationType;
        progress = 0.0F;
        previousProgress = 0.0F;
        target = 1.0F;
    }

    public static void tick() {
        previousProgress = progress;

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

    public static float getProgress(float partialTicks) {
        return Mth.lerp(partialTicks, previousProgress, progress);
    }

    public static EOPAnimationType getCurrentAnimation() {
        return currentAnimation;
    }
}
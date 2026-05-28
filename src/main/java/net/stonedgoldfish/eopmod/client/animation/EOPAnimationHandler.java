package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.util.Mth;

public class EOPAnimationHandler {

    public enum Phase {
        NONE,
        PHASE_1,
        PHASE_2,
        RETURN
    }

    private static EOPAnimationType currentAnimation = EOPAnimationType.NONE;
    private static Phase phase = Phase.NONE;

    private static float progress = 0.0F;
    private static float previousProgress = 0.0F;
    private static float target = 0.0F;

    public static void play(EOPAnimationType animationType) {
        currentAnimation = animationType;
        phase = Phase.PHASE_1;

        progress = 0.0F;
        previousProgress = 0.0F;
        target = 1.0F;
    }

    public static void tick() {
        previousProgress = progress;

        float speed = getSpeed(currentAnimation, phase);
        progress = Mth.lerp(speed, progress, target);

        if (currentAnimation.playbackType == EOPAnimationPlaybackType.TWO_PHASE) {
            tickTwoPhase();
        } else {
            tickOneShot();
        }

        if (phase == Phase.RETURN && progress < 0.01F) {
            stop();
        }
    }

    private static void tickOneShot() {
        if (phase == Phase.PHASE_1 && progress > 0.85F) {
            phase = Phase.RETURN;
            target = 0.0F;
        }
    }

    private static void tickTwoPhase() {
        if (phase == Phase.PHASE_1 && progress > 0.95F) {
            phase = Phase.PHASE_2;

            progress = 0.0F;
            previousProgress = 0.0F;
            target = 1.0F;
        }

        if (phase == Phase.PHASE_2 && progress > 0.95F) {
            phase = Phase.RETURN;
            target = 0.0F;
        }
    }

    private static void stop() {
        currentAnimation = EOPAnimationType.NONE;
        phase = Phase.NONE;

        progress = 0.0F;
        previousProgress = 0.0F;
        target = 0.0F;
    }

    private static float getSpeed(EOPAnimationType type, Phase phase) {
        return switch (type) {

            case RIGHT_ARM_SWIPE -> phase == Phase.RETURN ? 0.08F : 0.28F;

            case SHOOT -> phase == Phase.RETURN ? 0.11F : 0.19F;

            case TRANSFORM -> switch (phase) {
                case PHASE_1 -> 0.11F;
                case PHASE_2 -> 0.08F;
                case RETURN -> 0.11F;
                default -> 0.10F;
            };

            case DASH_FRONT,
                 DASH_LEFT,
                 DASH_RIGHT,
                 DASH_BACK -> phase == Phase.RETURN ? 0.12F : 0.35F;

            default -> 0.10F;
        };
    }

    public static float getProgress(float partialTicks) {
        return Mth.lerp(partialTicks, previousProgress, progress);
    }

    public static EOPAnimationType getCurrentAnimation() {
        return currentAnimation;
    }

    public static Phase getPhase() {
        return phase;
    }

    public static boolean isPlaying() {
        return currentAnimation != EOPAnimationType.NONE;
    }
}
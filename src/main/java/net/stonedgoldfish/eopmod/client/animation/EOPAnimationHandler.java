package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.util.Mth;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class EOPAnimationHandler {

    public enum Phase {
        NONE,
        PHASE_1,
        PHASE_2,
        RETURN
    }

    private static boolean forceThirdPerson = false;
    private static boolean restoreFirstPerson = false;
    private static CameraType previousCameraType = null;
    private static boolean cameraWasChanged = false;

    private static EOPAnimationType currentAnimation = EOPAnimationType.NONE;
    private static Phase phase = Phase.NONE;

    private static float progress = 0.0F;
    private static float previousProgress = 0.0F;
    private static float target = 0.0F;

    private static int tick = 0;
    private static int previousTick = 0;

    private static void startCameraTransition(EOPAnimationType animationType) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!shouldForceThirdPerson(animationType)) {
            return;
        }

        if (!cameraWasChanged) {
            previousCameraType = minecraft.options.getCameraType();
            cameraWasChanged = true;
        }

        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    public static void play(EOPAnimationType animationType) {
        currentAnimation = animationType;
        phase = Phase.PHASE_1;

        progress = 0.0F;
        previousProgress = 0.0F;
        target = 1.0F;

        tick = 0;
        previousTick = 0;

        startCameraTransition(animationType);
    }

    public static void tick() {
        if (currentAnimation == EOPAnimationType.NONE) {
            return;
        }

        if (currentAnimation.playbackType == EOPAnimationPlaybackType.TWO_PHASE) {
            tickTwoPhaseAnimation();
        } else if (currentAnimation.playbackType == EOPAnimationPlaybackType.HOLD) {
            tickHoldAnimation();
        } else {
            tickOneShotAnimation();
        }
    }

    private static boolean holding = false;

    private static void tickHoldAnimation() {

        previousProgress = progress;

        float enterSpeed = getEnterSpeed(currentAnimation);
        float returnSpeed = getReturnSpeed(currentAnimation);

        target = holding ? 1.0F : 0.0F;

        if (target > progress) {
            progress = Mth.lerp(enterSpeed, progress, target);
        } else {
            progress = Mth.lerp(returnSpeed, progress, target);
        }

        if (!holding && progress < 0.01F) {
            stop();
        }
    }

    public static void setHolding(boolean value) {
        holding = value;

        if (!holding && currentAnimation.playbackType == EOPAnimationPlaybackType.HOLD) {
            target = 0.0F;
        }
    }

    public static boolean isHolding() {
        return holding;
    }

    private static void tickOneShotAnimation() {
        previousProgress = progress;

        float enterSpeed = getEnterSpeed(currentAnimation);
        float returnSpeed = getReturnSpeed(currentAnimation);

        if (target > progress) {
            progress = Mth.lerp(enterSpeed, progress, target);
        } else {
            progress = Mth.lerp(returnSpeed, progress, target);
        }

        if (target > 0.0F && progress > 0.85F) {
            phase = Phase.RETURN;
            target = 0.0F;
        }

        if (target == 0.0F && progress < 0.01F) {
            stop();
        }
    }

    private static void tickTwoPhaseAnimation() {
        previousTick = tick;
        tick++;

        int phaseOneEnd = getPhaseOneEnd(currentAnimation);
        int phaseTwoEnd = getPhaseTwoEnd(currentAnimation);
        int duration = getDuration(currentAnimation);

        if (tick <= phaseOneEnd) {
            phase = Phase.PHASE_1;
        } else if (tick <= phaseTwoEnd) {
            phase = Phase.PHASE_2;
        } else {
            phase = Phase.RETURN;
        }

        if (tick >= duration) {
            stop();
        }
    }

    private static void stop() {
        stopCameraTransition();
        currentAnimation = EOPAnimationType.NONE;
        phase = Phase.NONE;

        progress = 0.0F;
        previousProgress = 0.0F;
        target = 0.0F;

        tick = 0;
        previousTick = 0;

        holding = false;
    }

    public static void stopHolding() {
        if (currentAnimation.playbackType == EOPAnimationPlaybackType.HOLD) {
            holding = false;
            target = 0.0F;

            stopCameraTransition();
        }
    }

    private static void stopCameraTransition() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (cameraWasChanged && previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
        }

        previousCameraType = null;
        cameraWasChanged = false;
    }

    private static boolean shouldForceThirdPerson(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM,
                 RIGHT_ARM_HOLD -> true;

            default -> false;
        };
    }

    public static float getProgress(float partialTicks) {
        if (currentAnimation == EOPAnimationType.NONE) {
            return 0.0F;
        }

        if (currentAnimation.playbackType == EOPAnimationPlaybackType.TWO_PHASE) {
            return getPhaseProgress(partialTicks);
        }

        return Mth.lerp(partialTicks, previousProgress, progress);
    }

    public static float getPhaseProgress(float partialTicks) {
        if (currentAnimation == EOPAnimationType.NONE) {
            return 0.0F;
        }

        if (currentAnimation.playbackType != EOPAnimationPlaybackType.TWO_PHASE) {
            return getProgress(partialTicks);
        }

        float interpolatedTick = Mth.lerp(partialTicks, previousTick, tick);

        int phaseOneEnd = getPhaseOneEnd(currentAnimation);
        int phaseTwoEnd = getPhaseTwoEnd(currentAnimation);
        int duration = getDuration(currentAnimation);

        if (phase == Phase.PHASE_1) {
            return Mth.clamp(interpolatedTick / phaseOneEnd, 0.0F, 1.0F);
        }

        if (phase == Phase.PHASE_2) {
            return Mth.clamp(
                    (interpolatedTick - phaseOneEnd) / (phaseTwoEnd - phaseOneEnd),
                    0.0F,
                    1.0F
            );
        }

        if (phase == Phase.RETURN) {
            return Mth.clamp(
                    (interpolatedTick - phaseTwoEnd) / (duration - phaseTwoEnd),
                    0.0F,
                    1.0F
            );
        }

        return 0.0F;
    }

    private static int getPhaseOneEnd(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 10;
            default -> getDuration(type) / 2;
        };
    }

    private static int getPhaseTwoEnd(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 16;
            default -> getDuration(type) - 5;
        };
    }

    private static int getDuration(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 31;
            default -> 20;
        };
    }

    private static float getEnterSpeed(EOPAnimationType type) {
        return switch (type) {
            case RIGHT_ARM_SWIPE -> 0.28F;
            case SHOOT -> 0.19F;
            case RIGHT_ARM_HOLD -> 0.05F;
            case TRANSFORM -> 0.50F;

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
            case RIGHT_ARM_HOLD -> 0.05F;
            case TRANSFORM -> 0.40F;

            case DASH_FRONT,
                 DASH_LEFT,
                 DASH_RIGHT,
                 DASH_BACK -> 0.12F;

            default -> 0.10F;
        };
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
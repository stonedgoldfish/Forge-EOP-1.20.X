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

        if (previousCameraType == CameraType.FIRST_PERSON) {
            EOPCameraTransition.start(getCameraProfile(animationType));
        }
    }

    private static EOPCameraTransition.CameraProfile getCameraProfile(EOPAnimationType type) {
        return switch (type) {

            case TRANSFORM -> new EOPCameraTransition.CameraProfile(
                    0.0D,
                    0.25D,
                    8.0D,
                    0.30F,
                    0.40F,
                    0.10F
            );

            case CREATE -> new EOPCameraTransition.CameraProfile(
                    -1.0D,
                    0.21D,
                    2.0D,
                    0.30F,
                    0.40F,
                    0.10F
            );

            case THIRD_PERSON -> new EOPCameraTransition.CameraProfile(
                    0.0D,
                    0.25D,
                    8.0D,
                    0.30F,
                    0.55F,
                    0.15F
            );

            default -> new EOPCameraTransition.CameraProfile(
                    0.0D,
                    0.0D,
                    4.0D,
                    0.30F,
                    0.20F,
                    0.15F
            );
        };
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

        if (cameraWasChanged && previousCameraType == CameraType.FIRST_PERSON) {
            EOPCameraTransition.stop();
        } else if (cameraWasChanged && previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
        }

        previousCameraType = null;
        cameraWasChanged = false;
    }

    private static boolean shouldForceThirdPerson(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM,
                 CREATE,
                 THIRD_PERSON -> true;

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
            return Mth.clamp(
                    (interpolatedTick / phaseOneEnd) * getPhaseOneSpeed(currentAnimation),
                    0.0F,
                    1.0F
            );
        }

        if (phase == Phase.PHASE_2) {
            return Mth.clamp(
                    ((interpolatedTick - phaseOneEnd) / (phaseTwoEnd - phaseOneEnd)) * getPhaseTwoSpeed(currentAnimation),
                    0.0F,
                    1.0F
            );
        }

        if (phase == Phase.RETURN) {
            return Mth.clamp(
                    ((interpolatedTick - phaseTwoEnd) / (duration - phaseTwoEnd)) * getReturnSpeedTwoPhase(currentAnimation),
                    0.0F,
                    1.0F
            );
        }

        return 0.0F;
    }

    private static int getPhaseOneDuration(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 10;
            case CREATE -> 12;
            default -> 10;
        };
    }

    private static int getPhaseTwoDuration(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 6;
            case CREATE -> 8;
            default -> 5;
        };
    }

    private static int getReturnDuration(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 15;
            case CREATE -> 10;
            default -> 5;
        };
    }

    private static float getPhaseOneSpeed(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 1.0F;
            case CREATE -> 1.0F;
            default -> 1.0F;
        };
    }

    private static float getPhaseTwoSpeed(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 1.0F;
            case CREATE -> 2.0F;
            default -> 1.0F;
        };
    }

    private static float getReturnSpeedTwoPhase(EOPAnimationType type) {
        return switch (type) {
            case TRANSFORM -> 1.0F;
            case CREATE -> 1.0F;
            default -> 1.0F;
        };
    }

    private static int getPhaseOneEnd(EOPAnimationType type) {
        return getPhaseOneDuration(type);
    }

    private static int getPhaseTwoEnd(EOPAnimationType type) {
        return getPhaseOneDuration(type) + getPhaseTwoDuration(type);
    }

    private static int getDuration(EOPAnimationType type) {
        return getPhaseOneDuration(type)
                + getPhaseTwoDuration(type)
                + getReturnDuration(type);
    }

    private static float getEnterSpeed(EOPAnimationType type) {
        return switch (type) {
            case RIGHT_ARM_SWIPE -> 0.28F;
            case SHOOT -> 0.19F;
            case THIRD_PERSON -> 0.05F;
            case RIGHT_ARM_LIFT -> 0.25F;

            case DASH_FRONT,
                 DASH_LEFT,
                 DASH_RIGHT,
                 DASH_BACK -> 0.35F;

            case AUTO_DODGE_1,
                 AUTO_DODGE_2,
                 AUTO_DODGE_3 -> 0.45F;

            default -> 0.30F;
        };
    }

    private static float getReturnSpeed(EOPAnimationType type) {
        return switch (type) {
            case RIGHT_ARM_SWIPE -> 0.08F;
            case SHOOT -> 0.11F;
            case THIRD_PERSON -> 0.05F;
            case RIGHT_ARM_LIFT -> 0.25F;

            case DASH_FRONT,
                 DASH_LEFT,
                 DASH_RIGHT,
                 DASH_BACK -> 0.12F;

            case AUTO_DODGE_1,
                 AUTO_DODGE_2,
                 AUTO_DODGE_3 -> 0.18F;

            default -> 0.10F;
        };
    }

    public static EOPAnimationType getCurrentAnimation() {
        return currentAnimation;
    }

    public static Phase getPhase() {
        return phase;
    }

}
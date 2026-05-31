package net.stonedgoldfish.eopmod.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

public class EOPCameraTransition {

    public record CameraProfile(
            double sideOffset,
            double heightOffset,
            double backDistance,
            float enterSpeed,
            float returnSpeed,
            float returnCutoff
    ) {}

    private static final CameraProfile DEFAULT_PROFILE =
            new CameraProfile(0.0D, 0.0D, 4.0D, 0.30F, 0.20F, 0.15F);

    private static CameraProfile profile = DEFAULT_PROFILE;

    private static boolean active = false;
    private static boolean returning = false;

    private static float progress = 0.0F;
    private static float previousProgress = 0.0F;

    public static void start(CameraProfile cameraProfile) {
        profile = cameraProfile;
        active = true;
        returning = false;
    }

    public static void stop() {
        returning = true;
    }

    public static void tick() {
        previousProgress = progress;

        if (active && !returning) {
            progress = Mth.lerp(profile.enterSpeed(), progress, 1.0F);
        }

        if (returning) {
            progress = Mth.lerp(profile.returnSpeed(), progress, 0.0F);

            if (progress < profile.returnCutoff()) {
                progress = 0.0F;
                previousProgress = 0.0F;
                active = false;
                returning = false;
                profile = DEFAULT_PROFILE;

                Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            }
        }
    }

    public static float getProgress(float partialTicks) {
        return Mth.lerp(partialTicks, previousProgress, progress);
    }

    public static boolean isActive() {
        return active;
    }

    public static CameraProfile getProfile() {
        return profile;
    }
}
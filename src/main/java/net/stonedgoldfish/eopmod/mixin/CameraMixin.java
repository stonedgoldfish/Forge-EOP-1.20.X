package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.stonedgoldfish.eopmod.client.animation.EOPCameraTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyArg(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;move(DDD)V"
            ),
            index = 0,
            require = 0
    )
    private double eopmod$cameraBackDistance(double original) {
        if (!EOPCameraTransition.isActive()) {
            return original;
        }

        float progress = EOPCameraTransition.getProgress(
                Minecraft.getInstance().getFrameTime()
        );

        return -EOPCameraTransition.getProfile().backDistance() * progress;
    }

    @ModifyArg(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;move(DDD)V"
            ),
            index = 1,
            require = 0
    )
    private double eopmod$cameraHeightOffset(double original) {
        if (!EOPCameraTransition.isActive()) {
            return original;
        }

        float progress = EOPCameraTransition.getProgress(
                Minecraft.getInstance().getFrameTime()
        );

        return EOPCameraTransition.getProfile().heightOffset() * progress;
    }

    @ModifyArg(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;move(DDD)V"
            ),
            index = 2,
            require = 0
    )
    private double eopmod$cameraSideOffset(double original) {
        if (!EOPCameraTransition.isActive()) {
            return original;
        }

        float progress = EOPCameraTransition.getProgress(
                Minecraft.getInstance().getFrameTime()
        );

        return EOPCameraTransition.getProfile().sideOffset() * progress;
    }
}
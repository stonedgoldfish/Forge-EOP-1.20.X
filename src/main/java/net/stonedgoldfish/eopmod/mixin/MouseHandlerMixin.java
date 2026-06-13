package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.stonedgoldfish.eopmod.client.InvertedMouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.client.MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    private void eop$invertMouseMovement(
            LocalPlayer player,
            double yaw,
            double pitch
    ) {
        if (InvertedMouseHandler.shouldInvertMouse(player)) {
            player.turn(-yaw, -pitch);
        } else {
            player.turn(yaw, pitch);
        }
    }
}
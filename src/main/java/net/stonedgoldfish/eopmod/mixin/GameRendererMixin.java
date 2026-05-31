package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.stonedgoldfish.eopmod.power.ability.ForwardMotionAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(
            method = "bobView",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eopmod$cancelCameraBobbing(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null &&
                ForwardMotionAbility.disablesCameraBobbing(minecraft.player)) {
            ci.cancel();
        }
    }
}
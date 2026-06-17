package net.stonedgoldfish.eopmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.stonedgoldfish.eopmod.client.render.DimensionalSkyHandler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Redirect(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"
            )
    )
    private void eop$tintSunAndMoonTexture(int slot, ResourceLocation texture) {
        Minecraft minecraft = Minecraft.getInstance();

        float influence = DimensionalSkyHandler.getSkyInfluence(
                minecraft,
                minecraft.getFrameTime()
        );

        boolean isSunOrMoon = texture.getPath().equals("textures/environment/sun.png")
                || texture.getPath().equals("textures/environment/moon_phases.png");

        if (isSunOrMoon && influence > 0.0F) {
            float flash = DimensionalSkyHandler.getLightningFlash(minecraft.getFrameTime());

            float r = Mth.lerp(influence, 1.0F, 0.45F);
            float g = Mth.lerp(influence, 1.0F, 0.95F);
            float b = Mth.lerp(influence, 1.0F, 0.55F);

            if (flash > 0.0F) {
                r = Mth.lerp(flash, r, 0.85F);
                g = Mth.lerp(flash, g, 1.0F);
                b = Mth.lerp(flash, b, 0.85F);
            }

            RenderSystem.setShaderColor(r, g, b, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        RenderSystem.setShaderTexture(slot, texture);
    }

    @Inject(
            method = "renderSky",
            at = @At("TAIL")
    )
    private void eop$resetSkyShaderColor(
            PoseStack poseStack,
            Matrix4f projectionMatrix,
            float partialTick,
            Camera camera,
            boolean isFoggy,
            Runnable setupFog,
            CallbackInfo ci
    ) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
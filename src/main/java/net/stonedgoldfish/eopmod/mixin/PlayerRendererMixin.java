package net.stonedgoldfish.eopmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Pose;
import net.stonedgoldfish.eopmod.power.ability.LavaSwimmingAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
            at = @At("TAIL")
    )
    private void eop$rotateLavaSwimming(
            AbstractClientPlayer player,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTicks,
            CallbackInfo ci
    ) {
        if (!LavaSwimmingAbility.hasLavaSwimming(player)) {
            return;
        }

        if (!player.isInLava()) {
            return;
        }

        if (!player.isSprinting() && player.getPose() != Pose.SWIMMING) {
            return;
        }

        poseStack.translate(0.0D, 0.9D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-player.getXRot()));
        poseStack.translate(0.0D, -0.9D, 0.0D);
    }
}
package net.stonedgoldfish.eopmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.stonedgoldfish.eopmod.client.renderlayer.gecko.EOPHeldItemTransforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            ),
            require = 0
    )
    private void eop$moveHeldItemAtCorrectHand(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            net.minecraft.world.entity.HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        boolean right = displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        var transform = EOPHeldItemTransforms.get(entity.getUUID(), right);

        if (transform == null) {
            return;
        }

        poseStack.translate(
                transform.position().x() / 16.0F,
                transform.position().y() / 16.0F,
                transform.position().z() / 16.0F
        );

        poseStack.mulPose(Axis.XP.rotation(transform.rotation().x()));
        poseStack.mulPose(Axis.YP.rotation(transform.rotation().y()));
        poseStack.mulPose(Axis.ZP.rotation(transform.rotation().z()));

        poseStack.scale(
                transform.scale().x(),
                transform.scale().y(),
                transform.scale().z()
        );
    }
}
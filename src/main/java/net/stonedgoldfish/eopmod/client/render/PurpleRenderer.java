package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.entity.PurpleProjectile;

public class PurpleRenderer extends EntityRenderer<PurpleProjectile> {

    private static final int FRAME_COUNT = 5;
    private static final int TICKS_PER_FRAME = 2;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/entity/projectile/purple.png"
            );

    public PurpleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            PurpleProjectile entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();
        int fullBright = LightTexture.FULL_BRIGHT;

        poseStack.translate(0.0D, entity.getBbHeight() * 0.5D, 0.0D);

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(7.5F, 7.5F, 7.5F);

        int frame = (entity.tickCount / TICKS_PER_FRAME) % FRAME_COUNT;

        float frameHeight = 1.0F / FRAME_COUNT;

        float vMin = frame * frameHeight;
        float vMax = vMin + frameHeight;

        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        var matrix = poseStack.last().pose();

        vertexConsumer.vertex(matrix, -0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, vMax)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, 0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, vMax)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, 0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, vMin)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, -0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, vMin)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PurpleProjectile entity) {
        return TEXTURE;
    }
}
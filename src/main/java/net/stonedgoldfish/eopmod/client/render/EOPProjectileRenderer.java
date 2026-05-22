package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.stonedgoldfish.eopmod.entity.EOPProjectileEntity;
import net.threetag.palladium.client.renderer.renderlayer.IPackRenderLayer;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager;
import net.threetag.palladium.util.context.DataContext;

public class EOPProjectileRenderer extends EntityRenderer<EOPProjectileEntity> {

    private final ItemRenderer itemRenderer;

    public EOPProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            EOPProjectileEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        if (!entity.getItem().isEmpty()) {
            if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)) {
                poseStack.pushPose();
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                this.itemRenderer.renderStatic(
                        entity.getItem(),
                        ItemDisplayContext.GROUND,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        buffer,
                        entity.level(),
                        entity.getId()
                );

                poseStack.popPose();
            }
        }

        for (ResourceLocation id : entity.getRenderLayers()) {
            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(
                    Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F
            ));

            poseStack.mulPose(Axis.ZP.rotationDegrees(
                    Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())
            ));

            poseStack.translate(-0.5F, entity.getBbHeight() / 2F, 0);

            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(270.0F));

            IPackRenderLayer layer = PackRenderLayerManager.getInstance().getLayer(id);

            if (layer != null) {
                layer.render(
                        DataContext.forEntity(entity),
                        poseStack,
                        buffer,
                        null,
                        packedLight,
                        0,
                        0,
                        partialTicks,
                        entity.tickCount,
                        0,
                        0
                );
            }

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EOPProjectileEntity entity) {
        return null;
    }
}
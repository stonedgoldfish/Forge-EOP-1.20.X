package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import org.joml.Matrix4f;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class DimensionalSeveranceHandler {

    private static final Map<UUID, Integer> BUBBLE_AGE = new HashMap<>();
    private static final ResourceLocation BUBBLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/entity/render/dimensional_severance.png"
            );
    private static final ResourceLocation SPLATTER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/entity/render/bubble_splatter.png"
            );

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();

        if (minecraft.player == null) {
            return;
        }

        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(
                LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(128.0D)
        )) {
            if (!hasBubbleAbility(entity)) {
                BUBBLE_AGE.remove(entity.getUUID());
                continue;
            }

            BUBBLE_AGE.putIfAbsent(entity.getUUID(), 0);
            BUBBLE_AGE.put(entity.getUUID(), BUBBLE_AGE.get(entity.getUUID()) + 1);

            renderBubble(entity, poseStack, event.getPartialTick(), BUBBLE_AGE.get(entity.getUUID()));
        }
    }

    private static boolean hasBubbleAbility(LivingEntity entity) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath("eop", "models/dimensional_severance"),
                "Screen.Shake"
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }

    private static void renderBubble(LivingEntity entity, PoseStack poseStack, float partialTick, int bubbleAge) {
        Minecraft minecraft = Minecraft.getInstance();

        double camX = minecraft.gameRenderer.getMainCamera().getPosition().x;
        double camY = minecraft.gameRenderer.getMainCamera().getPosition().y;
        double camZ = minecraft.gameRenderer.getMainCamera().getPosition().z;

        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick + entity.getBbHeight() * 0.5D;
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

        MultiBufferSource.BufferSource buffer =
                minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();

        poseStack.translate(
                x - camX,
                y - camY,
                z - camZ
        );

        float spin = (entity.tickCount + partialTick) * 8.0F;

        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        float appearTime = 60.0F;
        float appearProgress = Mth.clamp((bubbleAge + partialTick) / appearTime, 0.0F, 1.0F);

        float popScale = 1.0F + (1.0F - appearProgress) * 0.35F;
        float scale = easeOutBack(appearProgress) * popScale;

        poseStack.scale(scale, scale, scale);

        renderSphere(poseStack, buffer, 15.8F, 0.05F);
        renderSplatters(poseStack, buffer, entity.getUUID(), 15.81F, 0.85F);
        renderSplatters(poseStack, buffer, entity.getUUID(), 15.61F, 0.85F);

        poseStack.popPose();

        buffer.endBatch();
    }

    private static void renderSplatters(
            PoseStack poseStack,
            MultiBufferSource buffer,
            UUID uuid,
            float radius,
            float alpha
    ) {
        java.util.Random random = new java.util.Random(uuid.getMostSignificantBits());

        int count = 830;

        for (int i = 0; i < count; i++) {
            float phi = (float) Math.acos(2.0D * random.nextDouble() - 1.0D);
            float theta = (float) (2.0D * Math.PI * random.nextDouble());

            float splatterRadius = radius + 0.03F;

            float x = splatterRadius * Mth.sin(phi) * Mth.cos(theta);
            float y = splatterRadius * Mth.cos(phi);
            float z = splatterRadius * Mth.sin(phi) * Mth.sin(theta);

            float size = 1.5F;
            float rotation = 30.0F;

            renderSplatterQuad(
                    poseStack,
                    buffer,
                    x,
                    y,
                    z,
                    size,
                    rotation,
                    alpha
            );
        }
    }

    private static void renderSplatterQuad(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float x,
            float y,
            float z,
            float size,
            float rotation,
            float alpha
    ) {
        poseStack.pushPose();

        poseStack.translate(x, y, z);

        Vec3 normal = new Vec3(x, y, z).normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(normal.x, normal.z));
        float pitch = (float) Math.toDegrees(Math.asin(-normal.y));

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

        drawSplatterQuad(poseStack, buffer, size, alpha);

        poseStack.popPose();
    }

    private static void drawSplatterQuad(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float size,
            float alpha
    ) {
        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(SPLATTER_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        int a = (int) (alpha * 255.0F);

        vertexConsumer.vertex(matrix, -size, -size, 0.0F)
                .color(255, 255, 255, a)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, size, -size, 0.0F)
                .color(255, 255, 255, a)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, size, size, 0.0F)
                .color(255, 255, 255, a)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, -size, size, 0.0F)
                .color(255, 255, 255, a)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static float easeOutBack(float x) {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;

        return 1.0F + c3 * (float) Math.pow(x - 1.0F, 3.0D)
                + c1 * (float) Math.pow(x - 1.0F, 2.0D);
    }

    private static void renderSphere(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float radius,
            float alpha
    ) {
        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(BUBBLE_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        int a = (int) (alpha * 255.0F);

        int stacks = 16;
        int slices = 32;

        for (int stack = 0; stack < stacks; stack++) {
            float phi1 = (float) Math.PI * stack / stacks;
            float phi2 = (float) Math.PI * (stack + 1) / stacks;

            for (int slice = 0; slice < slices; slice++) {
                float theta1 = (float) (2.0D * Math.PI * slice / slices);
                float theta2 = (float) (2.0D * Math.PI * (slice + 1) / slices);

                addSphereVertex(vertexConsumer, matrix, radius, phi1, theta1, 0.0F, 0.0F, a);
                addSphereVertex(vertexConsumer, matrix, radius, phi2, theta1, 0.0F, 1.0F, a);
                addSphereVertex(vertexConsumer, matrix, radius, phi2, theta2, 1.0F, 1.0F, a);
                addSphereVertex(vertexConsumer, matrix, radius, phi1, theta2, 1.0F, 0.0F, a);
            }
        }
    }

    private static void addSphereVertex(
            com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer,
            Matrix4f matrix,
            float radius,
            float phi,
            float theta,
            float u,
            float v,
            int alpha
    ) {
        float x = radius * Mth.sin(phi) * Mth.cos(theta);
        float y = radius * Mth.cos(phi);
        float z = radius * Mth.sin(phi) * Mth.sin(theta);

        vertexConsumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(x, y, z)
                .endVertex();
    }
}
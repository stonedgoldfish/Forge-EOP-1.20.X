package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class DimensionalSeveranceHandler {

    private record BubbleRenderState(double x, double y, double z, UUID uuid, int renderAge) {}

    private static final Map<UUID, Float> BUBBLE_SCALE = new HashMap<>();
    private static final Map<UUID, BubbleRenderState> BUBBLE_LAST_STATE = new HashMap<>();
    private static final Map<UUID, Integer> BUBBLE_RENDER_AGE = new HashMap<>();

    private static final float GROW_SPEED = 0.018F;
    private static final float SHRINK_SPEED = 0.015F;

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

        if (minecraft.level == null || minecraft.player == null) {
            BUBBLE_SCALE.clear();
            BUBBLE_LAST_STATE.clear();
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        HashSet<UUID> seenThisFrame = new HashSet<>();

        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(
                LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(128.0D)
        )) {
            UUID uuid = entity.getUUID();
            seenThisFrame.add(uuid);

            boolean active = entity.isAlive() && hasBubbleAbility(entity);

            float currentScale = BUBBLE_SCALE.getOrDefault(uuid, 0.0F);

            if (active) {
                currentScale = Math.min(currentScale + GROW_SPEED, 1.0F);
            } else {
                currentScale = Math.max(currentScale - SHRINK_SPEED, 0.0F);
            }

            if (currentScale <= 0.001F) {
                BUBBLE_SCALE.remove(uuid);
                BUBBLE_LAST_STATE.remove(uuid);
                continue;
            }

            BUBBLE_SCALE.put(uuid, currentScale);

            double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
            double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick + entity.getBbHeight() * 0.5D;
            double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

            int renderAge = BUBBLE_RENDER_AGE.getOrDefault(uuid, 0) + 1;
            BUBBLE_RENDER_AGE.put(uuid, renderAge);

            BubbleRenderState state = new BubbleRenderState(
                    x,
                    y,
                    z,
                    uuid,
                    renderAge
            );

            BUBBLE_LAST_STATE.put(uuid, state);

            renderBubbleAt(state, poseStack, partialTick, currentScale);
        }

        for (UUID uuid : new HashSet<>(BUBBLE_SCALE.keySet())) {
            if (seenThisFrame.contains(uuid)) {
                continue;
            }

            BubbleRenderState state = BUBBLE_LAST_STATE.get(uuid);

            if (state == null) {
                BUBBLE_SCALE.remove(uuid);
                continue;
            }

            float currentScale = Math.max(BUBBLE_SCALE.getOrDefault(uuid, 0.0F) - SHRINK_SPEED, 0.0F);

            if (currentScale <= 0.001F) {
                BUBBLE_SCALE.remove(uuid);
                BUBBLE_LAST_STATE.remove(uuid);
                continue;
            }

            BUBBLE_SCALE.put(uuid, currentScale);

            int renderAge = BUBBLE_RENDER_AGE.getOrDefault(uuid, state.renderAge()) + 1;
            BUBBLE_RENDER_AGE.put(uuid, renderAge);

            state = new BubbleRenderState(
                    state.x(),
                    state.y(),
                    state.z(),
                    state.uuid(),
                    renderAge
            );
            renderBubbleAt(state, poseStack, partialTick, currentScale);
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

    private static void renderBubbleAt(
            BubbleRenderState state,
            PoseStack poseStack,
            float partialTick,
            float scaleProgress
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        double camX = minecraft.gameRenderer.getMainCamera().getPosition().x;
        double camY = minecraft.gameRenderer.getMainCamera().getPosition().y;
        double camZ = minecraft.gameRenderer.getMainCamera().getPosition().z;

        MultiBufferSource.BufferSource buffer =
                minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();

        poseStack.translate(
                state.x() - camX,
                state.y() - camY,
                state.z() - camZ
        );

        float spin = (state.renderAge() + partialTick) * 4.0F;

        poseStack.mulPose(Axis.YP.rotationDegrees(spin));

        float scale = easeOutBack(scaleProgress);
        poseStack.scale(scale, scale, scale);

        float bubbleRadius = 15.8F;

        renderSphere(poseStack, buffer, bubbleRadius, 0.05F);
        renderSplatters(poseStack, buffer, state.uuid(), bubbleRadius + 0.03F, 0.85F);
        renderSplatters(poseStack, buffer, state.uuid(), bubbleRadius - 0.17F, 0.85F);

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

            float x = radius * Mth.sin(phi) * Mth.cos(theta);
            float y = radius * Mth.cos(phi);
            float z = radius * Mth.sin(phi) * Mth.sin(theta);

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
        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(SPLATTER_TEXTURE));
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
        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(BUBBLE_TEXTURE));
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
package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;

import java.util.*;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class ArmorStandBillboardRenderer {

    private static final Map<String, BillboardType> BILLBOARDS = new HashMap<>();
    private static final Map<Integer, ActiveBillboard> ENTITY_BILLBOARDS = new HashMap<>();
    private static final List<ExpiringBillboard> EXPIRING_BILLBOARDS = new ArrayList<>();

    static {
        register("lapse_blue", new BillboardType(
                "textures/entity/projectile/blue.png",
                5,
                4,
                true,
                10,
                2.0F,
                true,
                6,
                3.5F,
                1.2F
        ));
    }

    public static void setBillboard(int entityId, String billboardId) {
        ENTITY_BILLBOARDS.put(
                entityId,
                new ActiveBillboard(billboardId, Vec3.ZERO)
        );
    }

    public static void removeBillboard(int entityId) {
        ActiveBillboard active = ENTITY_BILLBOARDS.remove(entityId);

        if (active == null) {
            return;
        }

        BillboardType type = BILLBOARDS.get(active.id);

        if (type != null && active.lastPos != null) {
            if (type.fadeOnExpire()) {
                EXPIRING_BILLBOARDS.add(new ExpiringBillboard(type, active.lastPos));
            }
        }
    }

    private static void register(String id, BillboardType type) {
        BILLBOARDS.put(id, type);
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            ENTITY_BILLBOARDS.clear();
            EXPIRING_BILLBOARDS.clear();
            return;
        }

        for (ActiveBillboard active : ENTITY_BILLBOARDS.values()) {
            active.age++;
        }

        Iterator<Map.Entry<Integer, ActiveBillboard>> activeIterator =
                ENTITY_BILLBOARDS.entrySet().iterator();

        while (activeIterator.hasNext()) {
            Map.Entry<Integer, ActiveBillboard> entry = activeIterator.next();

            if (minecraft.level.getEntity(entry.getKey()) != null) {
                continue;
            }

            ActiveBillboard active = entry.getValue();
            BillboardType type = BILLBOARDS.get(active.id);

            if (type != null && active.lastPos != null) {
                if (type.fadeOnExpire()) {
                    EXPIRING_BILLBOARDS.add(new ExpiringBillboard(type, active.lastPos));
                }
            }

            activeIterator.remove();
        }

        Iterator<ExpiringBillboard> expireIterator = EXPIRING_BILLBOARDS.iterator();

        while (expireIterator.hasNext()) {
            ExpiringBillboard billboard = expireIterator.next();

            billboard.age++;

            if (billboard.age >= billboard.type.expireTime()) {
                expireIterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        float partialTick = event.getPartialTick();

        AABB area = minecraft.player.getBoundingBox().inflate(128.0D);

        for (ArmorStand armorStand : minecraft.level.getEntitiesOfClass(ArmorStand.class, area)) {
            ActiveBillboard active = ENTITY_BILLBOARDS.get(armorStand.getId());

            if (active == null) {
                continue;
            }

            BillboardType type = BILLBOARDS.get(active.id);

            if (type == null) {
                continue;
            }

            int frame = (armorStand.tickCount / type.ticksPerFrame()) % type.frameCount();

            Vec3 pos = armorStand.position().add(0.0D, type.heightOffset(), 0.0D);
            active.lastPos = pos;

            float size = type.size();

            if (type.fadeIn()) {
                float renderAge = active.age + partialTick;

                float fadeProgress = Math.min(
                        1.0F,
                        renderAge / (float) Math.max(1, type.fadeInTime())
                );

                fadeProgress = fadeProgress * fadeProgress * (3.0F - 2.0F * fadeProgress);

                size = type.size() * fadeProgress;
            }

            renderBillboard(
                    poseStack,
                    buffer,
                    camera,
                    pos,
                    type.getTexture(),
                    frame,
                    type.frameCount(),
                    size,
                    255
            );
        }

        for (ExpiringBillboard billboard : EXPIRING_BILLBOARDS) {
            float progress = Math.min(
                    1.0F,
                    (billboard.age + partialTick) / (float) billboard.type.expireTime()
            );

            float size = billboard.type.size()
                    * (1.0F + progress * (billboard.type.expireScale() - 1.0F));

            int alpha = (int) ((1.0F - progress) * 255.0F);

            int frame = (billboard.age / billboard.type.ticksPerFrame())
                    % billboard.type.frameCount();

            renderBillboard(
                    poseStack,
                    buffer,
                    camera,
                    billboard.pos,
                    billboard.type.getTexture(),
                    frame,
                    billboard.type.frameCount(),
                    size,
                    alpha
            );
        }

        buffer.endBatch();
    }

    private static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vec3 camera,
            Vec3 pos,
            ResourceLocation texture,
            int frame,
            int frameCount,
            float size,
            int alpha
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        float frameHeight = 1.0F / frameCount;
        float vMin = frame * frameHeight;
        float vMax = vMin + frameHeight;

        poseStack.pushPose();

        poseStack.translate(
                pos.x - camera.x,
                pos.y - camera.y,
                pos.z - camera.z
        );

        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(size, size, size);

        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        var matrix = poseStack.last().pose();

        int fullBright = LightTexture.FULL_BRIGHT;

        vertexConsumer.vertex(matrix, -0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(0.0F, vMax)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, 0.5F, -0.5F, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(1.0F, vMax)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, 0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(1.0F, vMin)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, -0.5F, 0.5F, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(0.0F, vMin)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(fullBright)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        poseStack.popPose();
    }

    private record BillboardType(
            String texture,
            int frameCount,
            int ticksPerFrame,
            boolean fadeOnExpire,
            int expireTime,
            float expireScale,
            boolean fadeIn,
            int fadeInTime,
            float size,
            float heightOffset
    ) {
        ResourceLocation getTexture() {
            return ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, texture);
        }
    }

    private static class ActiveBillboard {
        final String id;
        Vec3 lastPos;
        int age = 0;

        ActiveBillboard(String id, Vec3 lastPos) {
            this.id = id;
            this.lastPos = lastPos;
        }
    }

    private static class ExpiringBillboard {
        final BillboardType type;
        final Vec3 pos;
        int age = 0;

        ExpiringBillboard(BillboardType type, Vec3 pos) {
            this.type = type;
            this.pos = pos;
        }
    }
}
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
                "textures/entity/projectile/red.png",
                5,
                5,
                2,
                1,
                3.5F,
                1.8F
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
            EXPIRING_BILLBOARDS.add(new ExpiringBillboard(type, active.lastPos));
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
                EXPIRING_BILLBOARDS.add(new ExpiringBillboard(type, active.lastPos));
            }

            activeIterator.remove();
        }

        Iterator<ExpiringBillboard> expireIterator = EXPIRING_BILLBOARDS.iterator();

        while (expireIterator.hasNext()) {
            ExpiringBillboard billboard = expireIterator.next();

            billboard.age++;

            int frame = billboard.age / billboard.type.expireTicksPerFrame();

            if (frame >= billboard.type.expireFrameCount()) {
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

            renderBillboard(
                    poseStack,
                    buffer,
                    camera,
                    pos,
                    type.getTexture(),
                    frame,
                    type.frameCount(),
                    type.size()
            );
        }

        for (ExpiringBillboard billboard : EXPIRING_BILLBOARDS) {
            int frame = billboard.age / billboard.type.expireTicksPerFrame();

            if (frame >= billboard.type.expireFrameCount()) {
                continue;
            }

            renderBillboard(
                    poseStack,
                    buffer,
                    camera,
                    billboard.pos,
                    billboard.type.getExpireTexture(),
                    frame,
                    billboard.type.expireFrameCount(),
                    billboard.type.size()
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
            float size
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
    }

    private record BillboardType(
            String texture,
            String expireTexture,
            int frameCount,
            int expireFrameCount,
            int ticksPerFrame,
            int expireTicksPerFrame,
            float size,
            float heightOffset
    ) {
        ResourceLocation getTexture() {
            return ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, texture);
        }

        ResourceLocation getExpireTexture() {
            return ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, expireTexture);
        }
    }

    private static class ActiveBillboard {
        final String id;
        Vec3 lastPos;

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
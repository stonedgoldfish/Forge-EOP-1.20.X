package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class ReversalRedBillboardRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/entity/projectile/red.png"
            );

    private static final int FRAME_COUNT = 5;
    private static final int TICKS_PER_FRAME = 2;

    private static final float SIZE = 1.8F;
    private static final double FORWARD_OFFSET = 1.4D;
    private static final double HEIGHT_OFFSET = 1.35D;

    private static float overlayFade = 0.0F;

    private static final float TARGET_OVERLAY_FADE = 1.0F;
    private static final float FADE_SMOOTHNESS = 0.35F;
    private static final float MAX_SCREEN_ALPHA = 84.0F;
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        float target = minecraft.player != null && hasReversalRedAbility(minecraft.player)
                ? TARGET_OVERLAY_FADE
                : 0.0F;

        overlayFade += (target - overlayFade) * FADE_SMOOTHNESS;

        if (Math.abs(overlayFade) < 0.001F) {
            overlayFade = 0.0F;
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

        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(
                LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(128.0D)
        )) {
            if (!entity.isAlive() || entity.isRemoved()) {
                continue;
            }

            if (!hasReversalRedAbility(entity)) {
                continue;
            }

            double x = entity.xo + (entity.getX() - entity.xo) * partialTick;
            double y = entity.yo + (entity.getY() - entity.yo) * partialTick;
            double z = entity.zo + (entity.getZ() - entity.zo) * partialTick;

            Vec3 look = entity.getViewVector(partialTick).normalize();

            Vec3 pos = new Vec3(x, y, z)
                    .add(0.0D, HEIGHT_OFFSET, 0.0D)
                    .add(look.scale(FORWARD_OFFSET));

            int frame = ((int) ((entity.tickCount + partialTick) / TICKS_PER_FRAME)) % FRAME_COUNT;

            renderBillboard(
                    poseStack,
                    buffer,
                    camera,
                    pos,
                    frame
            );
        }

        buffer.endBatch();
    }

    @SubscribeEvent
    public static void renderRedOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        if (overlayFade <= 0.001F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        int alpha = (int) (overlayFade * MAX_SCREEN_ALPHA);

        int color =
                (alpha << 24)
                        | (255 << 16)
                        | (40 << 8)
                        | 40;

        gui.fill(
                0,
                0,
                width,
                height,
                color
        );
    }

    private static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vec3 camera,
            Vec3 pos,
            int frame
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        float frameHeight = 1.0F / FRAME_COUNT;
        float vMin = frame * frameHeight;
        float vMax = vMin + frameHeight;

        poseStack.pushPose();

        poseStack.translate(
                pos.x - camera.x,
                pos.y - camera.y,
                pos.z - camera.z
        );

        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(SIZE, SIZE, SIZE);

        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
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

    private static boolean hasReversalRedAbility(LivingEntity entity) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath("eop", "honored_one"),
                "Red"
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }
}
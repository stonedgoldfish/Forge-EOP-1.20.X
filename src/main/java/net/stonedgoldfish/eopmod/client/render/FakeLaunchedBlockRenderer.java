package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class FakeLaunchedBlockRenderer {

    private static final List<FakeBlock> BLOCKS = new ArrayList<>();

    private static final int LIFETIME = 45;
    private static final int SHRINK_TIME = 10;
    private static final double GRAVITY = -0.04D;
    private static final double DRAG = 0.98D;
    private static final double BOUNCE_SIDEWAYS = 0.45D;
    private static final double BOUNCE_UPWARD = 0.35D;
    private static final double STOP_SPEED = 0.015D;
    private static final int COLLISION_GRACE_TICKS = 5;
    private static final int RISING_HOLD_TIME = 2;

    public static void add(BlockState state, Vec3 position, Vec3 velocity) {
        if (velocity.y == RISING_MARKER) {
            Vec3 target = position.add(0.0D, 1.0D, 0.0D);
            BLOCKS.add(new FakeBlock(state, position, Vec3.ZERO, target));
            return;
        }

        BLOCKS.add(new FakeBlock(state, position, velocity));
    }
    private static final double RISING_MARKER = 999.0D;
    private static final int RISING_TIME = 8;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.isPaused()) {
            return;
        }

        Iterator<FakeBlock> iterator = BLOCKS.iterator();

        while (iterator.hasNext()) {
            FakeBlock block = iterator.next();

            block.prevPosition = block.position;
            block.prevRotX = block.rotX;
            block.prevRotY = block.rotY;
            block.prevRotZ = block.rotZ;
            block.prevScale = block.scale;

            if (block.rising) {
                block.risingAge++;

                float progress = block.risingAge / (float) RISING_TIME;
                progress = Math.min(1.0F, progress);

                float eased = 1.0F - (1.0F - progress) * (1.0F - progress);

                Vec3 start = block.targetPosition.add(0.0D, -1.0D, 0.0D);
                block.position = start.lerp(block.targetPosition, eased);
                float scaleProgress = progress;
                scaleProgress = Math.min(1.0F, scaleProgress);

                float easedScale = 1.0F - (1.0F - scaleProgress) * (1.0F - scaleProgress);

                block.scale = 0.15F + (0.85F * easedScale);
                spawnRisingParticles(mc, block);

                if (progress >= 1.0F) {
                    block.position = block.targetPosition;
                    block.risingHoldAge++;

                    if (block.risingHoldAge >= RISING_HOLD_TIME) {
                        iterator.remove();
                    }
                }

                continue;
            }

            if (!block.stopped) {
                Vec3 nextPosition = block.position.add(block.velocity);

                if (block.age >= COLLISION_GRACE_TICKS && collidesWithWorld(mc, nextPosition)) {
                    block.position = block.prevPosition;
                    block.prevPosition = block.position;

                    block.disappearing = true;

                    block.velocity = new Vec3(
                            block.velocity.x * BOUNCE_SIDEWAYS,
                            Math.abs(block.velocity.y) * BOUNCE_UPWARD,
                            block.velocity.z * BOUNCE_SIDEWAYS
                    );

                    if (block.velocity.lengthSqr() < STOP_SPEED) {
                        block.velocity = Vec3.ZERO;
                        block.stopped = true;

                        block.prevRotX = block.rotX;
                        block.prevRotY = block.rotY;
                        block.prevRotZ = block.rotZ;
                    }
                } else {
                    block.position = nextPosition;
                    block.velocity = block.velocity.add(0.0D, GRAVITY, 0.0D).scale(DRAG);

                    block.rotX += block.spinX;
                    block.rotY += block.spinY;
                    block.rotZ += block.spinZ;
                }
            }

            block.age++;

            if (block.disappearing) {
                block.disappearAge++;

                float progress = block.disappearAge / (float) SHRINK_TIME;
                block.scale = Math.max(0.0F, 1.0F - progress);

                if (block.disappearAge >= SHRINK_TIME) {
                    iterator.remove();
                }

                continue;
            }

            int shrinkStart = LIFETIME - SHRINK_TIME;

            if (block.age >= shrinkStart) {
                float progress = (block.age - shrinkStart) / (float) SHRINK_TIME;
                block.scale = Math.max(0.0F, 1.0F - progress);
            }

            if (block.age > LIFETIME) {
                iterator.remove();
            }
        }
    }

    private static void spawnRisingParticles(Minecraft mc, FakeBlock block) {
        if (mc.level == null) {
            return;
        }

        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 4; i++) {
            double x = block.position.x + random.nextDouble();
            double y = block.position.y + random.nextDouble();
            double z = block.position.z + random.nextDouble();

            mc.level.addParticle(
                    new net.minecraft.core.particles.BlockParticleOption(
                            net.minecraft.core.particles.ParticleTypes.BLOCK,
                            block.state
                    ),
                    x,
                    y,
                    z,
                    0.0D,
                    0.04D,
                    0.0D
            );
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        float partialTick = event.getPartialTick();

        for (FakeBlock block : BLOCKS) {
            Vec3 pos = block.prevPosition.lerp(block.position, partialTick);

            float rotX = lerp(block.prevRotX, block.rotX, partialTick);
            float rotY = lerp(block.prevRotY, block.rotY, partialTick);
            float rotZ = lerp(block.prevRotZ, block.rotZ, partialTick);

            BlockPos lightPos = BlockPos.containing(pos.x, pos.y + 0.5D, pos.z);

            if (!mc.level.getBlockState(lightPos).isAir()) {
                lightPos = findAirLightPos(mc, lightPos);
            }

            int packedLight = LevelRenderer.getLightColor(
                    mc.level,
                    lightPos
            );


            poseStack.pushPose();

            poseStack.translate(
                    pos.x - camera.x,
                    pos.y - camera.y,
                    pos.z - camera.z
            );

            poseStack.translate(0.5D, 0.5D, 0.5D);

            poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

            float scale = lerp(block.prevScale, block.scale, partialTick);

            poseStack.scale(
                    scale,
                    scale,
                    scale
            );

            poseStack.translate(-0.5D, -0.5D, -0.5D);

            mc.getBlockRenderer().renderSingleBlock(
                    block.state,
                    poseStack,
                    buffer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );

            poseStack.popPose();
        }

        buffer.endBatch();
    }

    private static BlockPos findAirLightPos(Minecraft mc, BlockPos startPos) {
        if (mc.level == null) {
            return startPos;
        }

        for (int i = 1; i <= 6; i++) {
            BlockPos above = startPos.above(i);

            if (mc.level.getBlockState(above).isAir()) {
                return above;
            }
        }

        return startPos;
    }

    private static boolean collidesWithWorld(Minecraft mc, Vec3 pos) {
        if (mc.level == null) {
            return false;
        }

        BlockPos blockPos = BlockPos.containing(
                pos.x + 0.5D,
                pos.y,
                pos.z + 0.5D
        );

        var state = mc.level.getBlockState(blockPos);

        return !state.isAir()
                && state.isSolidRender(mc.level, blockPos);
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }

    private static class FakeBlock {
        final BlockState state;

        Vec3 position;
        Vec3 prevPosition;
        Vec3 velocity;
        Vec3 targetPosition;
        boolean rising = false;
        int risingAge = 0;

        int age = 0;
        float scale = 1.0F;
        float prevScale = 1.0F;

        boolean stopped = false;
        boolean disappearing = false;
        int disappearAge = 0;
        int risingHoldAge = 0;

        float rotX;
        float rotY;
        float rotZ;

        float prevRotX;
        float prevRotY;
        float prevRotZ;

        float spinX;
        float spinY;
        float spinZ;

        FakeBlock(BlockState state, Vec3 position, Vec3 velocity) {
            this.state = state;
            this.position = position;
            this.prevPosition = position;
            this.velocity = velocity;

            java.util.Random random = new java.util.Random();

            this.rotX = random.nextFloat() * 360.0F;
            this.rotY = random.nextFloat() * 360.0F;
            this.rotZ = random.nextFloat() * 360.0F;

            this.prevRotX = this.rotX;
            this.prevRotY = this.rotY;
            this.prevRotZ = this.rotZ;

            this.spinX = -8.0F + random.nextFloat() * 16.0F;
            this.spinY = -8.0F + random.nextFloat() * 16.0F;
            this.spinZ = -8.0F + random.nextFloat() * 16.0F;
        }

        FakeBlock(BlockState state, Vec3 position, Vec3 velocity, Vec3 targetPosition) {
            this(state, position, velocity);

            this.targetPosition = targetPosition;
            this.rising = true;

            this.scale = 0.15F;
            this.prevScale = 0.15F;

            this.rotX = 0.0F;
            this.rotY = 0.0F;
            this.rotZ = 0.0F;

            this.prevRotX = 0.0F;
            this.prevRotY = 0.0F;
            this.prevRotZ = 0.0F;

            this.spinX = 0.0F;
            this.spinY = 0.0F;
            this.spinZ = 0.0F;
        }
    }
}
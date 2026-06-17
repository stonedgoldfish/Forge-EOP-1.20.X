package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import org.joml.Matrix4f;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import java.util.*;

@Mod.EventBusSubscriber(
        modid = EOPMod.MOD_ID,
        value = Dist.CLIENT
)
public class DimensionalCrackRenderer {

    private static final Map<UUID, CrackCluster> ACTIVE_CRACKS = new HashMap<>();

    private static final int CRACKS_PER_ENTITY = 18;
    private static final int FRAME_COUNT = 4;
    private static final int TICKS_PER_FRAME = 5;
    private static final double MIN_RADIUS = 7.0D;
    private static final double MAX_RADIUS = 130.0D;
    private static final double MIN_HEIGHT_ABOVE_EYES = 44.5D;
    private static final double MAX_HEIGHT_ABOVE_EYES = 54.0D;
    private static final float MIN_SIZE = 6.4F;
    private static final float MAX_SIZE = 7.8F;

    private static ResourceLocation getTexture(int frame) {
        return ResourceLocation.fromNamespaceAndPath(
                EOPMod.MOD_ID,
                "textures/entity/render/dimensional_crack_" + frame + ".png"
        );
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            ACTIVE_CRACKS.clear();
            return;
        }

        Set<UUID> seenEntities = new HashSet<>();

        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(
                LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(128.0D)
        )) {
            UUID id = entity.getUUID();
            seenEntities.add(id);

            boolean active = entity.isAlive()
                    && !entity.isRemoved()
                    && hasCrackAbility(entity);

            CrackCluster cluster = ACTIVE_CRACKS.get(id);

            if (active) {
                if (cluster == null) {
                    ACTIVE_CRACKS.put(id, new CrackCluster(entity, minecraft.player.tickCount));
                } else {
                    cluster.active = true;
                }
            } else if (cluster != null && cluster.active) {
                cluster.active = false;
                cluster.endTick = minecraft.player.tickCount;

                for (Crack crack : cluster.cracks) {
                    crack.reverseStartFrame = crack.getFrame(cluster, minecraft.player.tickCount);
                }
            }
        }

        for (Map.Entry<UUID, CrackCluster> entry : ACTIVE_CRACKS.entrySet()) {
            UUID id = entry.getKey();
            CrackCluster cluster = entry.getValue();

            if (!seenEntities.contains(id) && cluster.active) {
                cluster.active = false;
                cluster.endTick = minecraft.player.tickCount;

                for (Crack crack : cluster.cracks) {
                    crack.reverseStartFrame = crack.getFrame(cluster, minecraft.player.tickCount);
                }
            }
        }

        Iterator<Map.Entry<UUID, CrackCluster>> iterator = ACTIVE_CRACKS.entrySet().iterator();

        while (iterator.hasNext()) {
            CrackCluster cluster = iterator.next().getValue();

            if (!cluster.active) {
                int reverseTime = FRAME_COUNT * TICKS_PER_FRAME + cluster.maxDelay();

                if (minecraft.player.tickCount - cluster.endTick > reverseTime) {
                    iterator.remove();
                }
            }
        }

        for (CrackCluster cluster : ACTIVE_CRACKS.values()) {

            if (!cluster.active) {
                continue;
            }

            for (Crack crack : cluster.cracks) {

                if (minecraft.level.random.nextInt(2) != 0) {
                    continue;
                }

                int particleCount = 4 + minecraft.level.random.nextInt(4);

                for (int i = 0; i < particleCount; i++) {

                    Vec3 pos = cluster.origin.add(crack.offset);

                    double spread = crack.size * 0.4D;

                    double px = pos.x + (minecraft.level.random.nextDouble() - 0.5D) * spread;
                    double py = pos.y + (minecraft.level.random.nextDouble() - 0.5D) * spread;
                    double pz = pos.z + (minecraft.level.random.nextDouble() - 0.5D) * spread;

                    double dx = minecraft.level.random.nextGaussian();
                    double dy = minecraft.level.random.nextGaussian();
                    double dz = minecraft.level.random.nextGaussian();

                    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (length < 0.0001D) {
                        length = 1.0D;
                    }

                    double speed = 0.15D + minecraft.level.random.nextDouble() * 0.20D;

                    minecraft.level.addParticle(
                            ParticleTypes.END_ROD,
                            px,
                            py,
                            pz,
                            dx / length * speed,
                            dy / length * speed,
                            dz / length * speed
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderCracks(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        for (CrackCluster cluster : ACTIVE_CRACKS.values()) {
            for (Crack crack : cluster.cracks) {
                int frame = crack.getFrame(cluster, minecraft.player.tickCount);

                if (frame < 0) {
                    continue;
                }

                ResourceLocation texture = getTexture(frame);
                RenderSystem.setShaderTexture(0, texture);

                Vec3 worldPos = cluster.origin.add(crack.offset);

                poseStack.pushPose();

                poseStack.translate(
                        worldPos.x - cameraPos.x,
                        worldPos.y - cameraPos.y,
                        worldPos.z - cameraPos.z
                );

                poseStack.mulPose(camera.rotation());
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(crack.rotation));

                Matrix4f matrix = poseStack.last().pose();

                float halfSize = crack.size * 0.5F;
                float alpha = crack.getAlpha(cluster, minecraft.player.tickCount);

                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder buffer = tessellator.getBuilder();

                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

                buffer.vertex(matrix, -halfSize, -halfSize, 0.0F).uv(0.0F, 1.0F).color(255, 255, 255, (int) (alpha * 255.0F)).endVertex();
                buffer.vertex(matrix, halfSize, -halfSize, 0.0F).uv(1.0F, 1.0F).color(255, 255, 255, (int) (alpha * 255.0F)).endVertex();
                buffer.vertex(matrix, halfSize, halfSize, 0.0F).uv(1.0F, 0.0F).color(255, 255, 255, (int) (alpha * 255.0F)).endVertex();
                buffer.vertex(matrix, -halfSize, halfSize, 0.0F).uv(0.0F, 0.0F).color(255, 255, 255, (int) (alpha * 255.0F)).endVertex();

                tessellator.end();

                poseStack.popPose();
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static boolean hasCrackAbility(LivingEntity entity) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath("eop", "models/dimensional_severance"),
                "Screen.Shake"
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }

    private static class CrackCluster {
        final Vec3 origin;
        final int startTick;
        final List<Crack> cracks = new ArrayList<>();

        boolean active = true;
        int endTick;

        CrackCluster(LivingEntity entity, int startTick) {
            this.origin = new Vec3(
                    entity.getX(),
                    entity.getEyeY(),
                    entity.getZ()
            );

            this.startTick = startTick;

            Random random = new Random(
                    entity.getUUID().getMostSignificantBits()
                            ^ entity.getUUID().getLeastSignificantBits()
            );

            int attempts = 0;
            int maxAttempts = 300;

            while (cracks.size() < CRACKS_PER_ENTITY && attempts < maxAttempts) {
                attempts++;

                Crack candidate = new Crack(random);

                boolean tooClose = false;

                for (Crack existing : cracks) {
                    if (candidate.offset.distanceTo(existing.offset) < 8.0D) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    cracks.add(candidate);
                }
            }
        }

        int maxDelay() {
            int max = 0;

            for (Crack crack : cracks) {
                max = Math.max(max, crack.delay);
            }

            return max;
        }
    }

    private static class Crack {
        final Vec3 offset;
        final float size;
        final float rotation;
        final int delay;

        int reverseStartFrame = FRAME_COUNT - 1;

        Crack(Random random) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), MIN_RADIUS, MAX_RADIUS);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Mth.lerp(random.nextDouble(), MIN_HEIGHT_ABOVE_EYES, MAX_HEIGHT_ABOVE_EYES);

            this.offset = new Vec3(x, y, z);
            this.size = Mth.lerp(random.nextFloat(), MIN_SIZE, MAX_SIZE);
            this.rotation = random.nextFloat() * 360.0F;
            this.delay = random.nextInt(18);
        }

        int getFrame(CrackCluster cluster, int currentTick) {
            if (cluster.active) {
                int age = currentTick - cluster.startTick - delay;

                if (age < 0) {
                    return -1;
                }

                return Math.min(age / TICKS_PER_FRAME, FRAME_COUNT - 1);
            }

            int age = currentTick - cluster.endTick - delay;

            if (age < 0) {
                return reverseStartFrame;
            }

            return Math.max(reverseStartFrame - age / TICKS_PER_FRAME, 0);
        }

        float getAlpha(CrackCluster cluster, int currentTick) {
            int frame = getFrame(cluster, currentTick);

            if (frame < 0) {
                return 0.0F;
            }

            if (cluster.active) {
                return Mth.clamp(frame / 2.0F, 0.0F, 1.0F);
            }

            return Mth.clamp(frame / 2.0F, 0.0F, 1.0F);
        }
    }
}
package net.stonedgoldfish.eopmod.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
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

import java.util.*;

@Mod.EventBusSubscriber(
        modid = EOPMod.MOD_ID,
        value = Dist.CLIENT
)
public class DimensionalRisingTearRenderer {

    private static final Map<UUID, TearCluster> ACTIVE_TEARS = new HashMap<>();

    private static final int FRAME_COUNT = 3;
    private static final int TICKS_PER_FRAME = 3;
    private static final double SPAWN_RADIUS = 20.0D;
    private static final float MIN_SIZE = 3.0F;
    private static final float MAX_SIZE = 6.0F;
    private static final double RISE_SPEED = 0.18D;
    private static final double MAX_RISE_HEIGHT = 80.0D;
    private static final int MIN_SPAWN_DELAY = 6;
    private static final int MAX_SPAWN_DELAY = 15;

    private static final int FADE_OUT_TIME = 40;

    private static ResourceLocation getTexture(int frame) {
        return ResourceLocation.fromNamespaceAndPath(
                EOPMod.MOD_ID,
                "textures/entity/render/rising_tear_" + frame + ".png"
        );
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            ACTIVE_TEARS.clear();
            return;
        }

        if (minecraft.isPaused()) {
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
                    && hasAbility(entity);

            TearCluster cluster = ACTIVE_TEARS.get(id);

            if (active) {
                if (cluster == null) {
                    ACTIVE_TEARS.put(id, new TearCluster(entity, minecraft.player.tickCount));
                } else {
                    cluster.entity = entity;
                    cluster.active = true;
                }
            } else if (cluster != null && cluster.active) {
                cluster.active = false;
                cluster.endTick = minecraft.player.tickCount;
            }
        }

        for (Map.Entry<UUID, TearCluster> entry : ACTIVE_TEARS.entrySet()) {
            TearCluster cluster = entry.getValue();

            if (!seenEntities.contains(entry.getKey()) && cluster.active) {
                cluster.active = false;
                cluster.endTick = minecraft.player.tickCount;
            }
        }

        for (TearCluster cluster : ACTIVE_TEARS.values()) {
            if (!cluster.active || cluster.entity == null) {
                continue;
            }

            if (minecraft.player.tickCount >= cluster.nextSpawnTick) {
                cluster.tears.add(new RisingTear(
                        cluster.entity,
                        minecraft.player.tickCount,
                        minecraft.level.random
                ));

                cluster.nextSpawnTick = minecraft.player.tickCount
                        + MIN_SPAWN_DELAY
                        + minecraft.level.random.nextInt(MAX_SPAWN_DELAY - MIN_SPAWN_DELAY + 1);
            }
        }

        Iterator<Map.Entry<UUID, TearCluster>> clusterIterator = ACTIVE_TEARS.entrySet().iterator();

        while (clusterIterator.hasNext()) {
            TearCluster cluster = clusterIterator.next().getValue();

            Iterator<RisingTear> tearIterator = cluster.tears.iterator();

            while (tearIterator.hasNext()) {
                RisingTear tear = tearIterator.next();

                int age = minecraft.player.tickCount - tear.spawnTick;

                if (age > tear.lifetime) {
                    tearIterator.remove();
                }
            }

            if (!cluster.active
                    && cluster.tears.isEmpty()
                    && minecraft.player.tickCount - cluster.endTick > FADE_OUT_TIME) {
                clusterIterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void renderTears(RenderLevelStageEvent event) {
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
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        for (TearCluster cluster : ACTIVE_TEARS.values()) {
            float clusterFade = getClusterFade(cluster, minecraft.player.tickCount);

            if (clusterFade <= 0.0F) {
                continue;
            }

            for (RisingTear tear : cluster.tears) {
                int age = minecraft.player.tickCount - tear.spawnTick;

                if (age < 0) {
                    continue;
                }

                double rise = age * RISE_SPEED;

                if (rise > MAX_RISE_HEIGHT) {
                    continue;
                }

                int frame = (age / TICKS_PER_FRAME) % FRAME_COUNT;

                float tearFade = getTearFade(age, tear.lifetime);
                float alpha = clusterFade * tearFade;

                if (alpha <= 0.0F) {
                    continue;
                }

                Vec3 worldPos = tear.groundPos.add(0.0D, rise, 0.0D);

                RenderSystem.setShaderTexture(0, getTexture(frame));

                poseStack.pushPose();

                poseStack.translate(
                        worldPos.x - cameraPos.x,
                        worldPos.y - cameraPos.y,
                        worldPos.z - cameraPos.z
                );

                poseStack.mulPose(camera.rotation());
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(tear.rotation));

                Matrix4f matrix = poseStack.last().pose();

                float halfWidth = tear.width * 0.5F;
                float height = tear.height;
                int alphaInt = (int) (alpha * 255.0F);

                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder buffer = tessellator.getBuilder();

                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

                buffer.vertex(matrix, -halfWidth, 0.0F, 0.0F).uv(0.0F, 1.0F).color(255, 255, 255, alphaInt).endVertex();
                buffer.vertex(matrix, halfWidth, 0.0F, 0.0F).uv(1.0F, 1.0F).color(255, 255, 255, alphaInt).endVertex();
                buffer.vertex(matrix, halfWidth, height, 0.0F).uv(1.0F, 0.0F).color(255, 255, 255, alphaInt).endVertex();
                buffer.vertex(matrix, -halfWidth, height, 0.0F).uv(0.0F, 0.0F).color(255, 255, 255, alphaInt).endVertex();

                tessellator.end();

                poseStack.popPose();
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static float getClusterFade(TearCluster cluster, int tick) {
        if (cluster.active) {
            return 1.0F;
        }

        return Mth.clamp(
                1.0F - (tick - cluster.endTick) / (float) FADE_OUT_TIME,
                0.0F,
                1.0F
        );
    }

    private static float getTearFade(int age, int lifetime) {
        float fadeIn = Mth.clamp(age / 10.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((lifetime - age) / 20.0F, 0.0F, 1.0F);

        return fadeIn * fadeOut;
    }

    private static boolean hasAbility(LivingEntity entity) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath("eop", "models/dimensional_severance"),
                "Screen.Shake"
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }

    private static class TearCluster {
        LivingEntity entity;
        final List<RisingTear> tears = new ArrayList<>();

        boolean active = true;
        int endTick;
        int nextSpawnTick;

        TearCluster(LivingEntity entity, int startTick) {
            this.entity = entity;
            this.nextSpawnTick = startTick;
        }
    }

    private static class RisingTear {
        final Vec3 groundPos;
        final float width;
        final float height;
        final float rotation;
        final int spawnTick;
        final int lifetime;

        RisingTear(LivingEntity entity, int spawnTick, RandomSource random) {
            Minecraft minecraft = Minecraft.getInstance();

            this.spawnTick = spawnTick;

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Math.sqrt(random.nextDouble()) * SPAWN_RADIUS;

            double x = entity.getX() + Math.cos(angle) * radius;
            double z = entity.getZ() + Math.sin(angle) * radius;

            BlockPos pos = BlockPos.containing(x, entity.getY(), z);

            double groundY = entity.getY();

            if (minecraft.level != null) {
                groundY = minecraft.level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        pos.getX(),
                        pos.getZ()
                );
            }

            this.groundPos = new Vec3(x, groundY + 0.05D, z);

            this.width = Mth.lerp(random.nextFloat(), MIN_SIZE, MAX_SIZE);
            this.height = this.width * Mth.lerp(random.nextFloat(), 1.8F, 3.0F);
            this.rotation = random.nextFloat() * 360.0F;

            this.lifetime = (int) (MAX_RISE_HEIGHT / RISE_SPEED) + 40;
        }
    }
}
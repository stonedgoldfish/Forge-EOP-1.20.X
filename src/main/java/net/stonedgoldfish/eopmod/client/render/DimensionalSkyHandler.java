package net.stonedgoldfish.eopmod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;

import java.util.*;

@Mod.EventBusSubscriber(
        modid = EOPMod.MOD_ID,
        value = Dist.CLIENT
)
public class DimensionalSkyHandler {

    private static final Map<UUID, SkyWave> ACTIVE_WAVES = new HashMap<>();

    private static final float MAX_RADIUS = 512.0F;
    private static final float WAVE_SPEED = 8.0F;
    private static final float WAVE_EDGE_SIZE = 96.0F;
    private static final float FADE_IN_TIME = 40.0F;
    private static final float FADE_OUT_TIME = 40.0F;
    public static final Vec3 TARGET_SKY = new Vec3(0.005D, 0.06D, 0.025D);
    public static final Vec3 TARGET_CLOUDS = new Vec3(0.02D, 0.16D, 0.06D);
    private static final float TARGET_FOG_R = 0.005F;
    private static final float TARGET_FOG_G = 0.06F;
    private static final float TARGET_FOG_B = 0.025F;
    private static final float TARGET_FOG_DISTANCE = 100.0F;
    private static int nextLightningTick = 0;
    private static int lightningFlashTicks = 0;
    private static final int MIN_LIGHTNING_DELAY = 20;
    private static final int MAX_LIGHTNING_DELAY = 60;
    private static final int FLASH_DURATION = 8;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.isPaused()) {
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            ACTIVE_WAVES.clear();
            return;
        }

        updateWaves(minecraft);
        float influence = getSkyInfluence(minecraft, minecraft.getFrameTime());

        if (influence > 0.0F) {
            if (nextLightningTick <= 0) {
                nextLightningTick = MIN_LIGHTNING_DELAY + minecraft.level.random.nextInt(
                        MAX_LIGHTNING_DELAY - MIN_LIGHTNING_DELAY + 1
                );
            }

            nextLightningTick--;

            if (nextLightningTick <= 0) {
                lightningFlashTicks = FLASH_DURATION;

                minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        0.7F,
                        0.75F + minecraft.level.random.nextFloat() * 0.25F
                ));

                nextLightningTick = MIN_LIGHTNING_DELAY + minecraft.level.random.nextInt(
                        MAX_LIGHTNING_DELAY - MIN_LIGHTNING_DELAY + 1
                );
            }
        } else {
            nextLightningTick = 0;
            lightningFlashTicks = 0;
        }

        if (lightningFlashTicks > 0) {
            lightningFlashTicks--;
        }
    }

    public static float getLightningFlash(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.isPaused()) {
            partialTick = 0.0F;
        }

        if (lightningFlashTicks <= 0) {
            return 0.0F;
        }

        float flash = (lightningFlashTicks - partialTick) / FLASH_DURATION;
        flash = clamp(flash, 0.0F, 1.0F);

        return flash * flash;
    }

    @SubscribeEvent
    public static void computeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        float influence = getSkyInfluence(minecraft, (float) event.getPartialTick());

        if (influence <= 0.0F) {
            return;
        }

        event.setRed(lerp(event.getRed(), TARGET_FOG_R, influence));
        event.setGreen(lerp(event.getGreen(), TARGET_FOG_G, influence));
        event.setBlue(lerp(event.getBlue(), TARGET_FOG_B, influence));
    }

    @SubscribeEvent
    public static void renderFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        float influence = getSkyInfluence(minecraft, minecraft.getFrameTime());

        if (influence <= 0.0F) {
            return;
        }

        float originalNear = event.getNearPlaneDistance();
        float originalFar = event.getFarPlaneDistance();

        float targetNear = 48.0F;
        float targetFar = TARGET_FOG_DISTANCE;

        event.setNearPlaneDistance(lerp(originalNear, targetNear, influence));
        event.setFarPlaneDistance(lerp(originalFar, targetFar, influence));
        event.setCanceled(true);
    }

    private static void updateWaves(Minecraft minecraft) {
        Set<UUID> seenEntities = new HashSet<>();

        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(
                LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(256.0D)
        )) {
            UUID id = entity.getUUID();
            seenEntities.add(id);

            boolean active = entity.isAlive()
                    && !entity.isRemoved()
                    && hasSkyAbility(entity);

            SkyWave wave = ACTIVE_WAVES.get(id);

            if (active) {
                if (wave == null) {
                    ACTIVE_WAVES.put(id, new SkyWave(entity, minecraft.player.tickCount));
                } else {
                    wave.active = true;
                }
            } else if (wave != null && wave.active) {
                wave.active = false;
                wave.endTick = minecraft.player.tickCount;
            }
        }

        for (Map.Entry<UUID, SkyWave> entry : ACTIVE_WAVES.entrySet()) {
            UUID id = entry.getKey();
            SkyWave wave = entry.getValue();

            if (!seenEntities.contains(id) && wave.active) {
                wave.active = false;
                wave.endTick = minecraft.player.tickCount;
            }
        }
    }

    public static float getSkyInfluence(Minecraft minecraft, float partialTick) {
        if (minecraft.level == null || minecraft.player == null) {
            return 0.0F;
        }

        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();

        float strongestInfluence = 0.0F;

        Iterator<Map.Entry<UUID, SkyWave>> iterator = ACTIVE_WAVES.entrySet().iterator();

        while (iterator.hasNext()) {
            SkyWave wave = iterator.next().getValue();

            float age = minecraft.player.tickCount - wave.startTick + partialTick;
            float radius = Math.min(age * WAVE_SPEED, MAX_RADIUS);

            double distance = cameraPos.distanceTo(wave.origin);

            float reach = 1.0F - (float) Math.max(0.0D, distance - radius) / WAVE_EDGE_SIZE;
            reach = clamp(reach, 0.0F, 1.0F);

            float fade;

            if (wave.active) {
                fade = clamp(age / FADE_IN_TIME, 0.0F, 1.0F);
            } else {
                float collapseTicks = minecraft.player.tickCount - wave.endTick + partialTick;
                fade = 1.0F - clamp(collapseTicks / FADE_OUT_TIME, 0.0F, 1.0F);

                if (fade <= 0.0F) {
                    iterator.remove();
                    continue;
                }
            }

            float influence = easeOutCubic(reach) * easeOutCubic(fade);

            if (influence > strongestInfluence) {
                strongestInfluence = influence;
            }
        }

        return strongestInfluence;
    }

    public static Vec3 lerpColor(Vec3 original, Vec3 target, float amount) {
        return new Vec3(
                lerp((float) original.x, (float) target.x, amount),
                lerp((float) original.y, (float) target.y, amount),
                lerp((float) original.z, (float) target.z, amount)
        );
    }

    private static boolean hasSkyAbility(LivingEntity entity) {
        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath("eop", "models/dimensional_severance"),
                "Screen.Shake"
        );

        AbilityInstance ability = reference.getEntry(entity, null);

        return ability != null && ability.isEnabled();
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float easeOutCubic(float x) {
        return 1.0F - (float) Math.pow(1.0F - x, 3.0D);
    }

    private static class SkyWave {
        final Vec3 origin;
        final int startTick;

        boolean active = true;
        int endTick;

        SkyWave(LivingEntity entity, int startTick) {
            this.origin = new Vec3(
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5D,
                    entity.getZ()
            );

            this.startTick = startTick;
        }
    }
}
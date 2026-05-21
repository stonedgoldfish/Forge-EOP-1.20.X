package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.ability.ScreenShakeAbility;
import net.threetag.palladiumcore.event.ViewportEvents;

import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class EOPScreenShakeEvents implements ViewportEvents.ComputeCameraAngles {

    public static void init() {
        ViewportEvents.COMPUTE_CAMERA_ANGLES.register(new EOPScreenShakeEvents());
    }

    @Override
    public void computeCameraAngles(
            GameRenderer gameRenderer,
            Camera camera,
            double partialTick,
            AtomicReference<Float> yaw,
            AtomicReference<Float> pitch,
            AtomicReference<Float> roll
    ) {
        var player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        ScreenShakeAbility.ShakeSettings shake = ScreenShakeAbility.getShake(player);

        if (shake == null) {
            return;
        }

        double time = (player.tickCount + partialTick) * shake.speed();

        float pitchShake = (float) Math.sin(time * 1.7D) * shake.intensity();
        float rollShake = (float) Math.cos(time * 2.1D) * shake.intensity();

        pitch.set(pitch.get() + pitchShake);
        roll.set(roll.get() + rollShake);
    }
}
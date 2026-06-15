package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.effect.EOPEffects;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class DistortedShaderHandler {

    private static final ResourceLocation DECONVERGE_SHADER =
            ResourceLocation.fromNamespaceAndPath("eop", "shaders/post/distorted.json");

    private static boolean shaderActive = false;
    private static int switchTimer = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            clearShader(minecraft);
            return;
        }

        boolean distorted = minecraft.player.hasEffect(EOPEffects.DISTORTED.get()) || minecraft.player.hasEffect(EOPEffects.FRACTURED.get());

        if (!distorted) {
            clearShader(minecraft);
            return;
        }

        if (switchTimer > 0) {
            switchTimer--;
            return;
        }

        shaderActive = !shaderActive;

        if (shaderActive) {
            minecraft.gameRenderer.loadEffect(DECONVERGE_SHADER);

            switchTimer = 20 + minecraft.player.getRandom().nextInt(16);
        } else {
            minecraft.gameRenderer.shutdownEffect();

            switchTimer = 10 + minecraft.player.getRandom().nextInt(16);
        }
    }

    private static void clearShader(Minecraft minecraft) {
        switchTimer = 0;

        if (shaderActive) {
            minecraft.gameRenderer.shutdownEffect();
            shaderActive = false;
        }
    }
}
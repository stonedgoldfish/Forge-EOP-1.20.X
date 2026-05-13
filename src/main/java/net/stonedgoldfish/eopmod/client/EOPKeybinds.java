package net.stonedgoldfish.eopmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import net.stonedgoldfish.eopmod.EOPMod;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EOPKeybinds {

    public static final String CATEGORY = "key.categories.eop";

    public static final KeyMapping EXTRAS_PANEL = new KeyMapping(
            "key.eop.extras_panel",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
    );

    public static final KeyMapping BATTLE_MODE = new KeyMapping(
            "key.eop.battle_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(BATTLE_MODE);
        event.register(EXTRAS_PANEL);
    }
}
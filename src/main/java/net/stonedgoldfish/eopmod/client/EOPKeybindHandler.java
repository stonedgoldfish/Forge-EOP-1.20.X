package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.sounds.SoundEvents;
import net.stonedgoldfish.eopmod.client.screen.EOPExtrasScreen;

import net.stonedgoldfish.eopmod.EOPMod;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EOPKeybindHandler {

    private static boolean wasPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        boolean pressed = EOPKeybinds.BATTLE_MODE.isDown();

        if (pressed && !wasPressed) {
            toggleBattleMode(minecraft.player);
        }

        wasPressed = pressed;

        while (EOPKeybinds.EXTRAS_PANEL.consumeClick()) {
            Minecraft.getInstance().setScreen(new EOPExtrasScreen());
        }
    }

    private static void toggleBattleMode(LocalPlayer player) {
        Scoreboard scoreboard = player.getScoreboard();

        Objective objective = scoreboard.getObjective("EOP.Battle.Mode");

        if (objective == null) {
            objective = scoreboard.addObjective(
                    "EOP.Battle.Mode",
                    net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY,
                    Component.literal("Battle Mode"),
                    net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER
            );
        }

        var score = scoreboard.getOrCreatePlayerScore(
                player.getScoreboardName(),
                objective
        );

        int current = score.getScore();

        int next = current == 0 ? 1 : 0;

        score.setScore(next);

        player.playSound(
                SoundEvents.UI_BUTTON_CLICK.get(),
                1.0F,
                1.0F
        );

        player.displayClientMessage(
                Component.literal("§dBattlemode: " + (next == 1 ? "§aON" : "§cOFF")),
                true
        );
    }
}
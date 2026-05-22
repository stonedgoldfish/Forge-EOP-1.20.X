package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleBattleModePacket {

    public static void encode(ToggleBattleModePacket packet, FriendlyByteBuf buf) {
    }

    public static ToggleBattleModePacket decode(FriendlyByteBuf buf) {
        return new ToggleBattleModePacket();
    }

    public static void handle(ToggleBattleModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            if (!player.getTags().contains("EOP.Has.BM")) {
                player.displayClientMessage(
                        Component.literal("§cBattlemode unavailable"),
                        true
                );
                return;
            }

            boolean currentlyOn = player.getTags().contains("EOP.Battle.Mode");

            if (currentlyOn) {
                player.removeTag("EOP.Battle.Mode");
            } else {
                player.addTag("EOP.Battle.Mode");
            }

            boolean nowOn = !currentlyOn;

            var scoreboard = player.getScoreboard();

            var objective = scoreboard.getObjective("EOP.Battle.Mode");

            if (objective == null) {
                objective = scoreboard.addObjective(
                        "EOP.Battle.Mode",
                        net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY,
                        net.minecraft.network.chat.Component.literal("Battle Mode"),
                        net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER
                );
            }

            var score = scoreboard.getOrCreatePlayerScore(
                    player.getScoreboardName(),
                    objective
            );

            score.setScore(nowOn ? 1 : 0);

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            player.displayClientMessage(
                    Component.literal("§dBattlemode: " + (nowOn ? "§aON" : "§cOFF")),
                    true
            );
        });

        context.setPacketHandled(true);
    }
}
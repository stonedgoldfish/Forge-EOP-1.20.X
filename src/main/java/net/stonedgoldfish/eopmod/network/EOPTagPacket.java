package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EOPTagPacket {

    private final String tag;
    private final boolean enabled;

    public EOPTagPacket(String tag, boolean enabled) {
        this.tag = tag;
        this.enabled = enabled;
    }

    public static void encode(EOPTagPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.tag);
        buf.writeBoolean(packet.enabled);
    }

    public static EOPTagPacket decode(FriendlyByteBuf buf) {
        return new EOPTagPacket(
                buf.readUtf(),
                buf.readBoolean()
        );
    }

    public static void handle(EOPTagPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            if (packet.enabled) {
                player.addTag(packet.tag);
                player.getPersistentData().putBoolean(packet.tag, true);
            } else {
                player.removeTag(packet.tag);
                player.getPersistentData().remove(packet.tag);
            }
        });

        context.setPacketHandled(true);
    }
}
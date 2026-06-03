package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSelectedPowerPacket {

    private final String selectedPower;

    public SyncSelectedPowerPacket(String selectedPower) {
        this.selectedPower = selectedPower;
    }

    public static void encode(SyncSelectedPowerPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.selectedPower);
    }

    public static SyncSelectedPowerPacket decode(FriendlyByteBuf buf) {
        return new SyncSelectedPowerPacket(buf.readUtf());
    }

    public static void handle(SyncSelectedPowerPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            var player = context.getSender();

            if (player == null) {
                return;
            }

            player.getPersistentData().putString("selectedPower", packet.selectedPower);
        });

        context.setPacketHandled(true);
    }
}
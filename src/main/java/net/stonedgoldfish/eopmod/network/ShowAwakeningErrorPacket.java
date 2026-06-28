package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.client.screen.AwakeningSequencerPanel;

import java.util.function.Supplier;

public class ShowAwakeningErrorPacket {

    private final String message;

    public ShowAwakeningErrorPacket(String message) {
        this.message = message;
    }

    public ShowAwakeningErrorPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.message);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> AwakeningSequencerPanel.showError(this.message));

        context.setPacketHandled(true);
    }
}
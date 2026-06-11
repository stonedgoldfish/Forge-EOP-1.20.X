package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationType;

import java.util.function.Supplier;

public class DodgePacket {

    private final EOPAnimationType animation;

    public DodgePacket(EOPAnimationType animation) {
        this.animation = animation;
    }

    public static void encode(DodgePacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.animation);
    }

    public static DodgePacket decode(FriendlyByteBuf buf) {
        return new DodgePacket(buf.readEnum(EOPAnimationType.class));
    }

    public static void handle(DodgePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            EOPAnimationHandler.play(packet.animation);
        });

        context.setPacketHandled(true);
    }
}
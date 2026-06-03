package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.client.EOPClientStats;

import java.util.function.Supplier;

public class SyncAttackDamagePacket {

    private final double attackDamage;

    public SyncAttackDamagePacket(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public static void encode(SyncAttackDamagePacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.attackDamage);
    }

    public static SyncAttackDamagePacket decode(FriendlyByteBuf buf) {
        return new SyncAttackDamagePacket(buf.readDouble());
    }

    public static void handle(SyncAttackDamagePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            EOPClientStats.ATTACK_DAMAGE = packet.attackDamage;
        });

        context.setPacketHandled(true);
    }
}
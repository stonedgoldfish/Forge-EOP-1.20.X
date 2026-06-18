package net.stonedgoldfish.eopmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.client.render.ArmorStandBillboardRenderer;

import java.util.function.Supplier;

public class SyncArmorStandBillboardPacket {

    private final int entityId;
    private final String billboardId;

    public SyncArmorStandBillboardPacket(int entityId, String billboardId) {
        this.entityId = entityId;
        this.billboardId = billboardId;
    }

    public static void encode(
            SyncArmorStandBillboardPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeInt(packet.entityId);
        buffer.writeUtf(packet.billboardId);
    }

    public static SyncArmorStandBillboardPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncArmorStandBillboardPacket(
                buffer.readInt(),
                buffer.readUtf()
        );
    }

    public static void handle(
            SyncArmorStandBillboardPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            ArmorStandBillboardRenderer.setBillboard(
                    packet.entityId,
                    packet.billboardId
            );
        });

        context.setPacketHandled(true);
    }
}
package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;

import java.util.function.Supplier;

public class ToggleCustomFlightPacket {

    public ToggleCustomFlightPacket() {

    }

    public static void encode(ToggleCustomFlightPacket packet, FriendlyByteBuf buf) {

    }

    public static ToggleCustomFlightPacket decode(FriendlyByteBuf buf) {
        return new ToggleCustomFlightPacket();
    }

    public static void handle(ToggleCustomFlightPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            if (!CustomFlightAbility.hasCustomFlight(player)) {
                return;
            }

            CustomFlightAbility.toggleFlying(player);
        });

        context.setPacketHandled(true);
    }
}
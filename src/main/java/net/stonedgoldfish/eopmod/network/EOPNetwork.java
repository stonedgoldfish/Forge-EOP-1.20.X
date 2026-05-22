package net.stonedgoldfish.eopmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                EOPTagPacket.class,
                EOPTagPacket::encode,
                EOPTagPacket::decode,
                EOPTagPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                DashPacket.class,
                DashPacket::encode,
                DashPacket::decode,
                DashPacket::handle
        );
        CHANNEL.registerMessage(
                packetId++,
                ToggleBattleModePacket.class,
                ToggleBattleModePacket::encode,
                ToggleBattleModePacket::decode,
                ToggleBattleModePacket::handle
        );
    }
}
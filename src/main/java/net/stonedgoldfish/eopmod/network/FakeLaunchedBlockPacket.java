package net.stonedgoldfish.eopmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.client.render.FakeLaunchedBlockRenderer;

import java.util.function.Supplier;

public class FakeLaunchedBlockPacket {

    private final BlockState state;
    private final Vec3 position;
    private final Vec3 velocity;

    public FakeLaunchedBlockPacket(BlockState state, Vec3 position, Vec3 velocity) {
        this.state = state;
        this.position = position;
        this.velocity = velocity;
    }

    public static void encode(FakeLaunchedBlockPacket packet, FriendlyByteBuf buffer) {
        buffer.writeId(
                net.minecraft.core.registries.BuiltInRegistries.BLOCK,
                packet.state.getBlock()
        );

        buffer.writeDouble(packet.position.x);
        buffer.writeDouble(packet.position.y);
        buffer.writeDouble(packet.position.z);

        buffer.writeDouble(packet.velocity.x);
        buffer.writeDouble(packet.velocity.y);
        buffer.writeDouble(packet.velocity.z);
    }

    public static FakeLaunchedBlockPacket decode(FriendlyByteBuf buffer) {
        var block = buffer.readById(net.minecraft.core.registries.BuiltInRegistries.BLOCK);

        BlockState state = block == null
                ? net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()
                : block.defaultBlockState();

        Vec3 position = new Vec3(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );

        Vec3 velocity = new Vec3(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );

        return new FakeLaunchedBlockPacket(state, position, velocity);
    }

    public static void handle(FakeLaunchedBlockPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            FakeLaunchedBlockRenderer.add(
                    packet.state,
                    packet.position,
                    packet.velocity
            );
        });

        context.setPacketHandled(true);
    }
}
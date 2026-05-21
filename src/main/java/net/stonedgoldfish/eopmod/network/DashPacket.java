package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

public class DashPacket {

    private final double x;
    private final double z;
    private final float strength;

    public DashPacket(double x, double z, float strength) {
        this.x = x;
        this.z = z;
        this.strength = strength;
    }

    private static final UUID DASH_KNOCKBACK_UUID =
            UUID.fromString("7c4bbf2d-4c4f-4f63-8f83-df6e6d2e8a11");

    public static void encode(DashPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.strength);
    }

    public static DashPacket decode(FriendlyByteBuf buf) {
        return new DashPacket(
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat()
        );
    }

    public static void handle(DashPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            Vec3 direction = new Vec3(packet.x, 0.0D, packet.z).normalize();

            player.setDeltaMovement(
                    player.getDeltaMovement().add(direction.scale(packet.strength))
            );

            var attribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

            if (attribute != null) {

                attribute.removeModifier(DASH_KNOCKBACK_UUID);

                attribute.addTransientModifier(
                        new AttributeModifier(
                                DASH_KNOCKBACK_UUID,
                                "eop_dash_knockback_resistance",
                                1.0D,
                                AttributeModifier.Operation.ADDITION
                        )
                );
                DashKnockbackHandler.startDash(player);
            }
            player.hurtMarked = true;
        });

        context.setPacketHandled(true);
    }

    @Mod.EventBusSubscriber
    public static class DashKnockbackHandler {

        private static final java.util.Map<UUID, Integer> DASH_TICKS = new java.util.HashMap<>();

        public static void startDash(ServerPlayer player) {
            DASH_TICKS.put(player.getUUID(), 6);
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (!(event.player instanceof ServerPlayer player)) {
                return;
            }

            UUID uuid = player.getUUID();

            if (!DASH_TICKS.containsKey(uuid)) {
                return;
            }

            int ticks = DASH_TICKS.get(uuid) - 1;

            if (ticks <= 0) {

                var attribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

                if (attribute != null) {
                    attribute.removeModifier(DASH_KNOCKBACK_UUID);
                }

                DASH_TICKS.remove(uuid);

            } else {
                DASH_TICKS.put(uuid, ticks);
            }
        }
    }
}
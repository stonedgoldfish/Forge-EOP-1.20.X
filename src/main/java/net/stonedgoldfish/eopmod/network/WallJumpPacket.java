package net.stonedgoldfish.eopmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.power.ability.WallClimbAbility;

import java.util.function.Supplier;

public class WallJumpPacket {

    public WallJumpPacket() {}

    public static void encode(WallJumpPacket packet, FriendlyByteBuf buf) {}

    public static WallJumpPacket decode(FriendlyByteBuf buf) {
        return new WallJumpPacket();
    }

    public static void handle(WallJumpPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            if (!WallClimbAbility.isWallClimbing(player)) {
                return;
            }

            if (!player.isShiftKeyDown()) {
                return;
            }

            Vec3 look = player.getLookAngle().normalize();

            float jumpPower = WallClimbAbility.getWallJumpPower(player);

            Vec3 jumpMotion = look.scale(jumpPower)
                    .add(0.0D, jumpPower * 0.16D, 0.0D);

            player.setDeltaMovement(jumpMotion);
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.2F
            );

            WallClimbAbility.startWallJumpCooldown(player);

            player.fallDistance = 0.0F;
            player.hurtMarked = true;
        });

        context.setPacketHandled(true);
    }
}
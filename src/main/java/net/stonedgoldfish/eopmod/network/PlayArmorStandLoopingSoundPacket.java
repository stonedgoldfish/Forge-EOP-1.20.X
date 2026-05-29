package net.stonedgoldfish.eopmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.network.NetworkEvent;
import net.stonedgoldfish.eopmod.client.sound.EOPArmorStandLoopingSound;

import java.util.function.Supplier;

public class PlayArmorStandLoopingSoundPacket {

    private final int entityId;
    private final ResourceLocation sound;
    private final float volume;
    private final float pitch;

    public PlayArmorStandLoopingSoundPacket(int entityId, ResourceLocation sound, float volume, float pitch) {
        this.entityId = entityId;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static void encode(PlayArmorStandLoopingSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeResourceLocation(msg.sound);
        buf.writeFloat(msg.volume);
        buf.writeFloat(msg.pitch);
    }

    public static PlayArmorStandLoopingSoundPacket decode(FriendlyByteBuf buf) {
        return new PlayArmorStandLoopingSoundPacket(
                buf.readInt(),
                buf.readResourceLocation(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(PlayArmorStandLoopingSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.level == null) return;

            Entity entity = minecraft.level.getEntity(msg.entityId);

            if (entity instanceof ArmorStand armorStand) {
                minecraft.getSoundManager().play(
                        new EOPArmorStandLoopingSound(
                                armorStand,
                                msg.sound,
                                msg.volume,
                                msg.pitch
                        )
                );
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
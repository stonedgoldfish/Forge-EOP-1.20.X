package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.WallJumpPacket;
import net.stonedgoldfish.eopmod.power.ability.WallClimbAbility;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, value = Dist.CLIENT)
public class WallJumpClientHandler {

    private static boolean wasJumping = false;

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        boolean jumping = event.getInput().jumping;

        if (jumping
                && !wasJumping
                && minecraft.player.isShiftKeyDown()
                && WallClimbAbility.isWallClimbing(minecraft.player)) {
            EOPNetwork.CHANNEL.sendToServer(new WallJumpPacket());
        }

        wasJumping = jumping;
    }
}
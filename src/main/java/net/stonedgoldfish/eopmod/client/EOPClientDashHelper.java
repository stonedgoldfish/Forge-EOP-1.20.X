package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationType;
import net.stonedgoldfish.eopmod.network.DashPacket;
import net.stonedgoldfish.eopmod.network.EOPNetwork;

public class EOPClientDashHelper {

    public static void dash(float strength) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        float yawRad = (float) Math.toRadians(player.getYRot());

        Vec3 forward = new Vec3(
                -Math.sin(yawRad),
                0.0D,
                Math.cos(yawRad)
        );

        Vec3 right = new Vec3(
                Math.cos(yawRad),
                0.0D,
                Math.sin(yawRad)
        );

        Vec3 direction = Vec3.ZERO;

        boolean pressingForward = minecraft.options.keyUp.isDown();
        boolean pressingBack = minecraft.options.keyDown.isDown();
        boolean pressingLeft = minecraft.options.keyLeft.isDown();
        boolean pressingRight = minecraft.options.keyRight.isDown();

        boolean pressingAnything =
                pressingForward || pressingBack || pressingLeft || pressingRight;

        if (pressingForward) {
            direction = direction.add(forward);
        }

        if (pressingBack) {
            direction = direction.add(forward.scale(-1.0D));
        }

        if (pressingLeft) {
            direction = direction.add(right);
        }

        if (pressingRight) {
            direction = direction.add(right.scale(-1.0D));
        }

        if (direction.lengthSqr() < 0.001D) {
            direction = forward;
        }

        direction = direction.normalize();

        if (pressingForward || !pressingAnything) {
            EOPAnimationHandler.play(EOPAnimationType.DASH_FRONT);
        } else if (pressingBack) {
            EOPAnimationHandler.play(EOPAnimationType.DASH_BACK);
        } else if (pressingLeft) {
            EOPAnimationHandler.play(EOPAnimationType.DASH_LEFT);
        } else if (pressingRight) {
            EOPAnimationHandler.play(EOPAnimationType.DASH_RIGHT);
        }

        EOPNetwork.CHANNEL.sendToServer(
                new DashPacket(direction.x, direction.z, strength)
        );
    }
}
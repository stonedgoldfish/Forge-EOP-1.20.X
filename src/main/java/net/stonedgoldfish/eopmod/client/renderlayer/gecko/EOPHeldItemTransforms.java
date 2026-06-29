package net.stonedgoldfish.eopmod.client.renderlayer.gecko;

import net.minecraft.client.Minecraft;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EOPHeldItemTransforms {

    public record Transform(Vector3f position, Vector3f rotation, Vector3f scale, long gameTime) {}

    private static final Map<UUID, Transform> RIGHT_HAND = new HashMap<>();
    private static final Map<UUID, Transform> LEFT_HAND = new HashMap<>();

    public static void set(UUID uuid, boolean right, Vector3f position, Vector3f rotation, Vector3f scale) {
        long gameTime = getGameTime();

        Transform transform = new Transform(position, rotation, scale, gameTime);

        if (right) {
            RIGHT_HAND.put(uuid, transform);
        } else {
            LEFT_HAND.put(uuid, transform);
        }
    }

    public static Transform get(UUID uuid, boolean right) {
        Transform transform = right ? RIGHT_HAND.get(uuid) : LEFT_HAND.get(uuid);

        if (transform == null) {
            return null;
        }

        long gameTime = getGameTime();

        if (gameTime - transform.gameTime() > 1) {
            clear(uuid);
            return null;
        }

        return transform;
    }

    public static void clear(UUID uuid) {
        RIGHT_HAND.remove(uuid);
        LEFT_HAND.remove(uuid);
    }

    private static long getGameTime() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return 0L;
        }

        return minecraft.level.getGameTime();
    }
}
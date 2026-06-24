package net.stonedgoldfish.eopmod.power.condition;

import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EOPConditionLocks {

    private static final Map<UUID, Integer> LOCKS = new HashMap<>();

    public static boolean canStart(LivingEntity entity, AbilityInstance entry, boolean allowConcurrent) {
        if (allowConcurrent) {
            return true;
        }

        Integer lock = LOCKS.get(entity.getUUID());

        return lock == null || lock == System.identityHashCode(entry);
    }

    public static void lock(LivingEntity entity, AbilityInstance entry, boolean allowConcurrent) {
        if (allowConcurrent) {
            return;
        }

        LOCKS.put(entity.getUUID(), System.identityHashCode(entry));
    }

    public static void unlock(LivingEntity entity, AbilityInstance entry) {
        Integer lock = LOCKS.get(entity.getUUID());

        if (lock != null && lock == System.identityHashCode(entry)) {
            LOCKS.remove(entity.getUUID());
        }
    }
}
package net.stonedgoldfish.eopmod.power.condition;

import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;

public class EOPActivationHandler {

    public static void forceEndAndStartCooldown(LivingEntity entity, AbilityInstance entry) {
        if (entity == null || entry == null) {
            return;
        }

        if (entry.activationTimer <= 0) {
            return;
        }

        entry.activationTimer = 1;

        EOPConditionLocks.unlock(entity, entry);
    }
}
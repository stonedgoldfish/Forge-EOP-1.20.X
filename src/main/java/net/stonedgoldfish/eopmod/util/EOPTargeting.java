package net.stonedgoldfish.eopmod.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class EOPTargeting {

    public static boolean isFriendlyTarget(LivingEntity caster, LivingEntity target) {
        if (caster == null || target == null) {
            return true;
        }

        if (target == caster) {
            return true;
        }

        if (caster.isAlliedTo(target)) {
            return true;
        }

        if (target instanceof OwnableEntity ownableTarget) {
            var owner = ownableTarget.getOwner();

            if (owner == null) {
                return false;
            }

            if (owner.getUUID().equals(caster.getUUID())) {
                return true;
            }

            if (caster.isAlliedTo(owner)) {
                return true;
            }
        }

        if (caster instanceof OwnableEntity ownableCaster) {
            var owner = ownableCaster.getOwner();

            if (owner != null && owner.getUUID().equals(target.getUUID())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return target != null
                && target.isAlive()
                && !(target instanceof ArmorStand)
                && !isFriendlyTarget(caster, target);
    }
}
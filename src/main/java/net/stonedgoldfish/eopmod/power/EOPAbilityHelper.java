package net.stonedgoldfish.eopmod.power;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;

public class EOPAbilityHelper {

    public static boolean isAbilityActive(
            LivingEntity entity,
            String namespace,
            String powerPath,
            String abilityId
    ) {
        if (entity == null || namespace == null || powerPath == null || abilityId == null) {
            return false;
        }

        AbilityReference reference = new AbilityReference(
                ResourceLocation.fromNamespaceAndPath(namespace, powerPath),
                abilityId
        );

        AbilityInstance entry = reference.getEntry(entity);

        return entry != null && entry.isEnabled();
    }

    public static boolean isAbilityActive(
            LivingEntity entity,
            ResourceLocation powerId,
            String abilityId
    ) {
        if (entity == null || powerId == null || abilityId == null) {
            return false;
        }

        AbilityReference reference = new AbilityReference(powerId, abilityId);
        AbilityInstance entry = reference.getEntry(entity);

        return entry != null && entry.isEnabled();
    }
}
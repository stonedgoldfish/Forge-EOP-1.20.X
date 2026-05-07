package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringArrayProperty;

import java.util.*;

public class ImmuneToEffectAbility extends Ability {

    public static final PalladiumProperty<String[]> EFFECTS =
            new StringArrayProperty("effects")
                    .configurable("The potion effects this ability makes the entity immune to. Example: [\"minecraft:poison\", \"minecraft:wither\"]");

    private static final Map<UUID, Set<ResourceLocation>> IMMUNITIES = new HashMap<>();

    public ImmuneToEffectAbility() {
        this.withProperty(ICON, new ItemIcon(Items.MILK_BUCKET));
        this.withProperty(EFFECTS, new String[]{"minecraft:poison"});
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled) {
            addImmunities(entity, entry.getProperty(EFFECTS));
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide) {
            removeImmunities(entity, entry.getProperty(EFFECTS));
        }
    }

    private static void addImmunities(LivingEntity entity, String[] effectIds) {
        if (effectIds == null) {
            return;
        }

        Set<ResourceLocation> effects = IMMUNITIES.computeIfAbsent(entity.getUUID(), uuid -> new HashSet<>());

        for (String effectId : effectIds) {
            ResourceLocation effectLocation = ResourceLocation.tryParse(effectId);

            if (effectLocation != null && BuiltInRegistries.MOB_EFFECT.getOptional(effectLocation).isPresent()) {
                effects.add(effectLocation);
            }
        }

        if (effects.isEmpty()) {
            IMMUNITIES.remove(entity.getUUID());
        }
    }

    private static void removeImmunities(LivingEntity entity, String[] effectIds) {
        if (effectIds == null) {
            return;
        }

        Set<ResourceLocation> effects = IMMUNITIES.get(entity.getUUID());

        if (effects == null) {
            return;
        }

        for (String effectId : effectIds) {
            ResourceLocation effectLocation = ResourceLocation.tryParse(effectId);

            if (effectLocation != null) {
                effects.remove(effectLocation);
            }
        }

        if (effects.isEmpty()) {
            IMMUNITIES.remove(entity.getUUID());
        }
    }

    public static boolean isImmuneTo(LivingEntity entity, MobEffect effect) {
        ResourceLocation effectLocation = BuiltInRegistries.MOB_EFFECT.getKey(effect);

        if (effectLocation == null) {
            return false;
        }

        Set<ResourceLocation> effects = IMMUNITIES.get(entity.getUUID());

        return effects != null && effects.contains(effectLocation);
    }

    @Override
    public String getDocumentationDescription() {
        return "Makes the entity completely immune to multiple chosen potion effects.";
    }
}
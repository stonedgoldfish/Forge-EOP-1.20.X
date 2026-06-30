package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.UUID;

public class TransformedHealthAbility extends Ability {

    public static final PalladiumProperty<Float> HEALTH_BONUS = new FloatProperty("health_bonus").configurable("Additional max health while transformed");
    public static final PalladiumProperty<Integer> FULL_HEAL_COOLDOWN = new IntegerProperty("full_heal_cooldown").configurable("Ticks before transformed health is set to max again");

    private static final UUID TRANSFORMED_HEALTH_MODIFIER_UUID =
            UUID.fromString("7e8e4e6a-3c7e-4f8d-9c6f-8f5c4b6e2a11");

    private static final String NORMAL_HEALTH_KEY = "eop.transformed_health.normal_health";
    private static final String TRANSFORMED_HEALTH_KEY = "eop.transformed_health.transformed_health";
    private static final String HAS_TRANSFORMED_KEY = "eop.transformed_health.has_transformed";
    private static final String LAST_FULL_HEAL_TICK_KEY = "eop.transformed_health.last_full_heal_tick";

    public TransformedHealthAbility() {
        this.withProperty(ICON, new ItemIcon(Items.GOLDEN_APPLE));
        this.withProperty(HEALTH_BONUS, 20.0F);
        this.withProperty(FULL_HEAL_COOLDOWN, 1200);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        CompoundTag data = entity.getPersistentData();

        float targetMaxHealth = Math.max(1.0F, entry.getProperty(HEALTH_BONUS));

        data.putFloat(NORMAL_HEALTH_KEY, entity.getHealth());

        applyMaxHealth(entity, targetMaxHealth);

        long gameTime = entity.level().getGameTime();
        int cooldown = Math.max(0, entry.getProperty(FULL_HEAL_COOLDOWN));

        boolean hasTransformedBefore = data.getBoolean(HAS_TRANSFORMED_KEY);
        long lastFullHealTick = data.getLong(LAST_FULL_HEAL_TICK_KEY);

        boolean shouldFullHeal =
                !hasTransformedBefore ||
                        cooldown <= 0 ||
                        gameTime - lastFullHealTick >= cooldown;

        if (shouldFullHeal) {
            entity.setHealth(entity.getMaxHealth());
            data.putLong(LAST_FULL_HEAL_TICK_KEY, gameTime);
        } else {
            float savedTransformedHealth = data.getFloat(TRANSFORMED_HEALTH_KEY);
            entity.setHealth(clamp(savedTransformedHealth, 1.0F, entity.getMaxHealth()));
        }

        data.putBoolean(HAS_TRANSFORMED_KEY, true);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        CompoundTag data = entity.getPersistentData();

        data.putFloat(TRANSFORMED_HEALTH_KEY, entity.getHealth());

        removeMaxHealthModifier(entity);

        float savedNormalHealth = data.getFloat(NORMAL_HEALTH_KEY);
        entity.setHealth(clamp(savedNormalHealth, 1.0F, entity.getMaxHealth()));
    }

    private static void applyMaxHealth(LivingEntity entity, float healthBonus) {
        AttributeInstance maxHealthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealthAttribute == null) {
            return;
        }

        removeMaxHealthModifier(entity);

        AttributeModifier modifier = new AttributeModifier(
                TRANSFORMED_HEALTH_MODIFIER_UUID,
                "Transformed Health",
                healthBonus,
                AttributeModifier.Operation.ADDITION
        );

        maxHealthAttribute.addTransientModifier(modifier);
    }

    private static void removeMaxHealthModifier(LivingEntity entity) {
        AttributeInstance maxHealthAttribute = entity.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealthAttribute == null) {
            return;
        }

        if (maxHealthAttribute.getModifier(TRANSFORMED_HEALTH_MODIFIER_UUID) != null) {
            maxHealthAttribute.removeModifier(TRANSFORMED_HEALTH_MODIFIER_UUID);
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    @Override
    public String getDocumentationDescription() {
        return "Changes max health while transformed, saves normal health, and restores it when the ability ends.";
    }
}
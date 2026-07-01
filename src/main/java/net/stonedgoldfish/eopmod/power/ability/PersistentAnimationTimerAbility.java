package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AnimationTimer;
import net.threetag.palladium.util.property.*;

public class PersistentAnimationTimerAbility extends Ability implements AnimationTimer {

    public static final PalladiumProperty<Integer> START_VALUE = new IntegerProperty("start_value").configurable("The value for the integer when the ability is disabled");
    public static final PalladiumProperty<Integer> MAX_VALUE = new IntegerProperty("max_value").configurable("The value for the integer when the ability is enabled");
    public static final PalladiumProperty<Boolean> REMOVE_POWER_ON_DEATH = new BooleanProperty("remove_power_on_death").configurable("Remove this power when the player dies");
    public static final PalladiumProperty<Integer> VALUE = new IntegerProperty("value").sync(SyncType.NONE);
    public static final PalladiumProperty<Integer> PREV_VALUE = new IntegerProperty("prev_value").sync(SyncType.NONE).disablePersistence();

    public PersistentAnimationTimerAbility() {
        this.withProperty(START_VALUE, 0);
        this.withProperty(MAX_VALUE, 20);
        this.withProperty(REMOVE_POWER_ON_DEATH, false);
    }

    @Override
    public void registerUniqueProperties(PropertyManager manager) {
        manager.register(VALUE, 0);
        manager.register(PREV_VALUE, 0);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        int value = entry.getProperty(VALUE);
        entry.setUniqueProperty(PREV_VALUE, value);

        if (!entity.level().isClientSide && entry.getProperty(REMOVE_POWER_ON_DEATH)) {
            entity.getPersistentData().putString(
                    "EOP.RemovePowerOnDeath",
                    holder.getPower().getId().toString()
            );
        }
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        int value = entry.getProperty(VALUE);

        entry.setUniqueProperty(PREV_VALUE, value);

        if (enabled && value < entry.getProperty(MAX_VALUE)) {
            entry.setUniqueProperty(VALUE, value + 1);
        } else if (!enabled && value > entry.getProperty(START_VALUE)) {
            entry.setUniqueProperty(VALUE, value - 1);
        }
    }

    @Override
    public boolean isEffect() {
        return true;
    }

    @Override
    public float getAnimationValue(AbilityInstance entry, float partialTick) {
        return Mth.lerp(
                partialTick,
                entry.getProperty(PREV_VALUE),
                entry.getProperty(VALUE)
        ) / entry.getProperty(MAX_VALUE);
    }

    @Override
    public float getAnimationTimer(AbilityInstance entry, float partialTick, boolean maxedOut) {
        if (maxedOut) {
            return entry.getProperty(MAX_VALUE);
        }

        return Mth.lerp(
                partialTick,
                entry.getProperty(PREV_VALUE),
                entry.getProperty(VALUE)
        );
    }

    @Override
    public String getDocumentationDescription() {
        return "Persistent animation timer that saves its value";
    }
}
package net.stonedgoldfish.eopmod.power.condition;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.condition.Condition;
import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

public class HealthBelowPercentCondition extends Condition {

    private final float percent;

    public HealthBelowPercentCondition(float percent) {
        this.percent = percent;
    }

    @Override
    public boolean active(DataContext context) {
        LivingEntity entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        if (entity.getMaxHealth() <= 0.0F) {
            return false;
        }

        float healthPercent = entity.getHealth() / entity.getMaxHealth();

        return healthPercent <= this.percent;
    }

    @Override
    public ConditionSerializer getSerializer() {
        return EOPConditions.HEALTH_BELOW_PERCENT.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Float> PERCENT = new FloatProperty("percent").configurable("Health percentage threshold. Example: 0.5 = 50%");

        public Serializer() {
            this.withProperty(PERCENT, 0.5F);
        }

        @Override
        public Condition make(JsonObject json) {
            return new HealthBelowPercentCondition(
                    this.getProperty(json, PERCENT)
            );
        }

        @Override
        public String getDocumentationDescription() {
            return "Returns true when the entity's health percentage is at or below the configured threshold.";
        }
    }
}
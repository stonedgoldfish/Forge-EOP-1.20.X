package net.stonedgoldfish.eopmod.power.condition;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.condition.Condition;
import net.threetag.palladium.condition.ConditionEnvironment;
import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringArrayProperty;

public class DamageTakenCondition extends Condition {

    private final boolean specificDamage;
    private final String[] damageTypes;
    private final float minimumDamage;

    public DamageTakenCondition(
            boolean specificDamage,
            String[] damageTypes,
            float minimumDamage
    ) {
        this.specificDamage = specificDamage;
        this.damageTypes = damageTypes;
        this.minimumDamage = minimumDamage;
    }

    @Override
    public boolean active(DataContext context) {
        LivingEntity entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        EOPDamageTracker.DamageInfo info = EOPDamageTracker.getDamage(entity);

        if (info == null) {
            return false;
        }

        if (info.amount() < this.minimumDamage) {
            return false;
        }

        if (this.specificDamage) {
            return matchesDamageType(info.damageType());
        }

        return true;
    }

    private boolean matchesDamageType(ResourceLocation damageType) {
        if (damageType == null || this.damageTypes == null) {
            return false;
        }

        String id = damageType.toString();

        for (String allowed : this.damageTypes) {
            if (allowed == null || allowed.isBlank()) {
                continue;
            }

            if (allowed.equals(id)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer getSerializer() {
        return EOPConditions.DAMAGE_TAKEN.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Boolean> SPECIFIC_DAMAGE = new BooleanProperty("specific_damage").configurable("Only trigger for selected damage types");
        public static final PalladiumProperty<String[]> DAMAGE_TYPES = new StringArrayProperty("damage_types").configurable("Accepted damage type IDs");
        public static final PalladiumProperty<Float> MINIMUM_DAMAGE = new FloatProperty("minimum_damage").configurable("Minimum damage required");

        public Serializer() {
            this.withProperty(SPECIFIC_DAMAGE, false);
            this.withProperty(DAMAGE_TYPES, new String[0]);
            this.withProperty(MINIMUM_DAMAGE, 0.0F);
        }

        @Override
        public Condition make(JsonObject json) {
            return new DamageTakenCondition(
                    this.getProperty(json, SPECIFIC_DAMAGE),
                    this.getProperty(json, DAMAGE_TYPES),
                    this.getProperty(json, MINIMUM_DAMAGE)
            );
        }

        @Override
        public ConditionEnvironment getContextEnvironment() {
            return ConditionEnvironment.DATA;
        }

        @Override
        public String getDocumentationDescription() {
            return "Becomes true once when the entity takes damage. Can optionally require specific damage types and a minimum damage amount.";
        }
    }
}
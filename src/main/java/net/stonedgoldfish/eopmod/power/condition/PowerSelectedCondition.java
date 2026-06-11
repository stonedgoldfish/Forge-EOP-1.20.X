package net.stonedgoldfish.eopmod.power.condition;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.condition.Condition;
import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.ResourceLocationProperty;

public class PowerSelectedCondition extends Condition {

    private final ResourceLocation powerId;

    public PowerSelectedCondition(ResourceLocation powerId) {
        this.powerId = powerId;
    }

    @Override
    public boolean active(DataContext context) {
        LivingEntity entity = context.getLivingEntity();

        if (entity == null || this.powerId == null) {
            return false;
        }

        String selectedPower = entity.getPersistentData().getString("selectedPower");

        return selectedPower.equals(this.powerId.toString());
    }

    @Override
    public ConditionSerializer getSerializer() {
        return EOPConditions.POWER_SELECTED.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<ResourceLocation> POWER =
                new ResourceLocationProperty("power").configurable("ID of the selected power to test");

        public Serializer() {
            this.withProperty(POWER, ResourceLocation.parse("example:power_id"));
        }

        @Override
        public Condition make(JsonObject json) {
            return new PowerSelectedCondition(this.getProperty(json, POWER));
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the entity's selectedPower persistent data matches the given power ID.";
        }
    }
}
package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.ResourceLocationProperty;
import net.threetag.palladium.util.property.StringProperty;

public class ResetCooldownAbility extends Ability {

    public static final PalladiumProperty<ResourceLocation> POWER = new ResourceLocationProperty("power").configurable("ID of the power where is the desired ability is located. Can be null IF used for abilities, then it will look into the current power");
    public static final PalladiumProperty<String> ABILITY = new StringProperty("ability").configurable("ID of the desired ability");

    public ResetCooldownAbility() {
        this.withProperty(ICON, new ItemIcon(Items.CLOCK));
        this.withProperty(POWER, null);
        this.withProperty(ABILITY, "ability_id");
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        ResourceLocation powerId = entry.getProperty(POWER);
        String abilityId = entry.getProperty(ABILITY);

        if (abilityId == null || abilityId.isBlank()) {
            return;
        }

        AbilityReference reference = AbilityReference.fromString(abilityId);

        if (powerId != null) {
            reference = new AbilityReference(powerId, abilityId);
        }

        AbilityInstance target = reference.getEntry(entity, holder);

        if (target == null) {
            return;
        }

        target.startCooldown(entity, 0);
        target.keyPressed = false;
    }

    @Override
    public String getDocumentationDescription() {
        return "Resets the cooldown of another ability.";
    }
}
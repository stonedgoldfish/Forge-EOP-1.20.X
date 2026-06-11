package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import java.util.*;

public class InvisibilityAbility extends Ability {

    public static final PalladiumProperty<Boolean> CLEAR_MOB_TARGETS = new BooleanProperty("clear_mob_targets").configurable("If true, mobs will stop targeting the entity while invisible");
    private static final Map<UUID, Boolean> INVISIBLE_ENTITIES = new HashMap<>();

    public InvisibilityAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENDER_EYE));
        this.withProperty(CLEAR_MOB_TARGETS, true);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (enabled) {
            INVISIBLE_ENTITIES.put(
                    entity.getUUID(),
                    entry.getProperty(CLEAR_MOB_TARGETS)
            );
        } else {
            INVISIBLE_ENTITIES.remove(entity.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        INVISIBLE_ENTITIES.remove(entity.getUUID());
    }

    public static boolean isInvisible(LivingEntity entity) {
        return entity != null
                && INVISIBLE_ENTITIES.containsKey(entity.getUUID());
    }

    public static boolean shouldClearMobTargets(LivingEntity entity) {
        return entity != null
                && Boolean.TRUE.equals(INVISIBLE_ENTITIES.get(entity.getUUID()));
    }

    @Override
    public String getDocumentationDescription() {
        return "Makes the entity invisible while active.";
    }
}
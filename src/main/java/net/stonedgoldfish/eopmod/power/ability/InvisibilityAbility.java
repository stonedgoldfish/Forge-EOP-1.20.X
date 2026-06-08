package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InvisibilityAbility extends Ability {

    private static final Set<UUID> INVISIBLE_ENTITIES = new HashSet<>();

    public InvisibilityAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENDER_EYE));
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (enabled) {
            INVISIBLE_ENTITIES.add(entity.getUUID());
        } else {
            INVISIBLE_ENTITIES.remove(entity.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        INVISIBLE_ENTITIES.remove(entity.getUUID());
    }

    public static boolean isInvisible(LivingEntity entity) {
        return entity != null && INVISIBLE_ENTITIES.contains(entity.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Makes the entity invisible while active.";
    }
}
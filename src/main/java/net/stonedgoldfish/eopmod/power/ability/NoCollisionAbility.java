package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoCollisionAbility extends Ability {

    public static final PalladiumProperty<Boolean> PROJECTILE_PHASING =
            new BooleanProperty("projectile_phasing")
                    .configurable("If true, projectiles will pass through the player.");

    private static final Map<UUID, Boolean> PHASING_ENTITIES = new HashMap<>();

    public NoCollisionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENDER_PEARL));
        this.withProperty(PROJECTILE_PHASING, true);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (enabled) {
            PHASING_ENTITIES.put(player.getUUID(), entry.getProperty(PROJECTILE_PHASING));
        } else {
            PHASING_ENTITIES.remove(player.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        PHASING_ENTITIES.remove(entity.getUUID());
    }

    public static boolean isEntityPhasing(Entity entity) {
        return entity != null && PHASING_ENTITIES.containsKey(entity.getUUID());
    }

    public static boolean isProjectilePhasing(Entity entity) {
        return entity != null && PHASING_ENTITIES.getOrDefault(entity.getUUID(), false);
    }

    @Override
    public String getDocumentationDescription() {
        return "Allows the player to phase through entities, with optional projectile phasing.";
    }
}
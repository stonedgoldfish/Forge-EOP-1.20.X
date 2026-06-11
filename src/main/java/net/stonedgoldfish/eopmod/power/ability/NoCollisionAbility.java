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
import net.threetag.palladium.util.property.StringArrayProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoCollisionAbility extends Ability {

    public static final PalladiumProperty<Boolean> PROJECTILE_PHASING =
            new BooleanProperty("projectile_phasing")
                    .configurable("If true, projectiles will pass through the player.");

    public static final PalladiumProperty<String[]> PROJECTILE_BLACKLIST =
            new StringArrayProperty("projectile_blacklist")
                    .configurable("Projectile entity IDs that are NOT ignored by projectile phasing.");

    private static final Map<UUID, PhasingData> PHASING_ENTITIES = new HashMap<>();

    private record PhasingData(
            boolean projectilePhasing,
            String[] projectileBlacklist
    ) {}

    public NoCollisionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENDER_PEARL));
        this.withProperty(PROJECTILE_PHASING, true);
        this.withProperty(PROJECTILE_BLACKLIST, new String[]{});
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (enabled) {
            PHASING_ENTITIES.put(
                    player.getUUID(),
                    new PhasingData(
                            entry.getProperty(PROJECTILE_PHASING),
                            entry.getProperty(PROJECTILE_BLACKLIST)
                    )
            );
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
        if (entity == null) {
            return false;
        }

        PhasingData data = PHASING_ENTITIES.get(entity.getUUID());

        return data != null && data.projectilePhasing();
    }

    public static boolean isProjectileBlacklisted(Entity phasedEntity, Entity projectile) {
        if (phasedEntity == null || projectile == null) {
            return false;
        }

        PhasingData data = PHASING_ENTITIES.get(phasedEntity.getUUID());

        if (data == null) {
            return false;
        }

        String projectileId =
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                        .getKey(projectile.getType())
                        .toString();

        for (String blacklisted : data.projectileBlacklist()) {
            if (projectileId.equals(blacklisted)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDocumentationDescription() {
        return "Allows the player to phase through entities, with optional projectile phasing.";
    }
}
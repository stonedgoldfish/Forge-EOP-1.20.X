package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringArrayProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AutoDodgeAbility extends Ability {

    public static final PalladiumProperty<String[]> COMMANDS =
            new StringArrayProperty("commands")
                    .configurable("Commands executed as the dodging entity when damage is dodged.");

    public static final PalladiumProperty<String[]> PROJECTILE_BLACKLIST =
            new StringArrayProperty("projectile_blacklist")
                    .configurable("Projectile entity IDs that are NOT dodged.");

    private static final Map<UUID, DodgeData> DODGING_ENTITIES = new HashMap<>();

    private record DodgeData(
            String[] commands,
            String[] projectileBlacklist
    ) {}

    public AutoDodgeAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FEATHER));
        this.withProperty(COMMANDS, new String[0]);
        this.withProperty(PROJECTILE_BLACKLIST, new String[0]);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        if (enabled) {
            DODGING_ENTITIES.put(
                    entity.getUUID(),
                    new DodgeData(
                            entry.getProperty(COMMANDS),
                            entry.getProperty(PROJECTILE_BLACKLIST)
                    )
            );
        } else {
            DODGING_ENTITIES.remove(entity.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        DODGING_ENTITIES.remove(entity.getUUID());
    }

    public static boolean canDodge(LivingEntity entity) {
        return entity != null && DODGING_ENTITIES.containsKey(entity.getUUID());
    }

    public static String[] getCommands(LivingEntity entity) {
        if (entity == null) {
            return new String[0];
        }

        DodgeData data = DODGING_ENTITIES.get(entity.getUUID());

        return data != null ? data.commands() : new String[0];
    }

    public static boolean isProjectileBlacklisted(Entity dodgingEntity, Entity projectile) {
        if (dodgingEntity == null || projectile == null) {
            return false;
        }

        DodgeData data = DODGING_ENTITIES.get(dodgingEntity.getUUID());

        if (data == null) {
            return false;
        }

        String projectileId =
                BuiltInRegistries.ENTITY_TYPE
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
        return "Automatically dodges incoming attacks while active, optionally executing multiple commands per dodge.";
    }
}
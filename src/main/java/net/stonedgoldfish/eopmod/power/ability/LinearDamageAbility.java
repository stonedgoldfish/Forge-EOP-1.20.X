package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;

import java.util.*;

public class LinearDamageAbility extends Ability {

    public static final PalladiumProperty<Float> DAMAGE =
            new FloatProperty("damage").configurable("Damage dealt to each target.");

    public static final PalladiumProperty<Float> RANGE =
            new FloatProperty("range").configurable("Maximum distance the line travels.");

    public static final PalladiumProperty<Float> WIDTH =
            new FloatProperty("width").configurable("Width of the damaging line.");

    public static final PalladiumProperty<Integer> TRAVEL_TIME =
            new IntegerProperty("travel_time").configurable("Ticks it takes for the line to reach max range.");

    public static final PalladiumProperty<String> DAMAGE_TYPE =
            new StringProperty("damage_type").configurable("Damage type used by this ability.");

    public static final PalladiumProperty<String> PARTICLE =
            new StringProperty("particle")
                    .configurable("Particle spawned as the linear damage travels. Empty disables particles.");

    public static final PalladiumProperty<Integer> MAX_WALL_THICKNESS =
            new IntegerProperty("max_wall_thickness")
                    .configurable("Maximum wall thickness this line can penetrate. -1 disables wall collision.");

    public static final PalladiumProperty<String[]> COMMANDS_ON_TARGETS =
            new StringArrayProperty("commands_on_targets")
                    .configurable("Commands to run as targets hit by the line.");

    public static final PalladiumProperty<String[]> COMMANDS_ON_ALLIES =
            new StringArrayProperty("commands_on_allies")
                    .configurable("Commands to run as allies hit by the line.");

    private static final Map<UUID, Set<UUID>> HIT_ENTITIES = new HashMap<>();

    private static final Map<UUID, Integer> ACTIVE_TICKS = new HashMap<>();

    public LinearDamageAbility() {
        this.withProperty(ICON, new ItemIcon(Items.BLAZE_ROD));
        this.withProperty(DAMAGE, 6.0F);
        this.withProperty(RANGE, 12.0F);
        this.withProperty(WIDTH, 1.5F);
        this.withProperty(TRAVEL_TIME, 10);
        this.withProperty(DAMAGE_TYPE, "minecraft:magic");
        this.withProperty(PARTICLE, "minecraft:poof");
        this.withProperty(MAX_WALL_THICKNESS, -1);
        this.withProperty(COMMANDS_ON_TARGETS, new String[0]);
        this.withProperty(COMMANDS_ON_ALLIES, new String[0]);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        EOPLinearDamageHandler.spawn(
                entity,
                entry.getProperty(DAMAGE),
                entry.getProperty(RANGE),
                entry.getProperty(WIDTH),
                entry.getProperty(TRAVEL_TIME),
                entry.getProperty(DAMAGE_TYPE),
                entry.getProperty(PARTICLE),
                entry.getProperty(COMMANDS_ON_TARGETS),
                entry.getProperty(COMMANDS_ON_ALLIES),
                entry.getProperty(MAX_WALL_THICKNESS)
        );
    }

    private static boolean isInsideLine(Vec3 origin, Vec3 direction, Vec3 targetPos, double maxDistance, double width) {
        Vec3 toTarget = targetPos.subtract(origin);

        double forwardDistance = toTarget.dot(direction);

        if (forwardDistance < 0.0D || forwardDistance > maxDistance) {
            return false;
        }

        Vec3 closestPoint = origin.add(direction.scale(forwardDistance));

        double distanceFromLine = targetPos.distanceTo(closestPoint);

        return distanceFromLine <= width;
    }

    private static DamageSource createDamageSource(LivingEntity entity, String damageTypeId) {
        ResourceLocation damageLocation = ResourceLocation.tryParse(damageTypeId);

        if (damageLocation == null) {
            return entity.damageSources().magic();
        }

        ResourceKey<DamageType> damageTypeKey = ResourceKey.create(
                Registries.DAMAGE_TYPE,
                damageLocation
        );

        return new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(damageTypeKey),
                entity
        );
    }

    @Override
    public String getDocumentationDescription() {
        return "Deals damage in a forward-traveling line with configurable range, width, travel time, and damage type.";
    }
}
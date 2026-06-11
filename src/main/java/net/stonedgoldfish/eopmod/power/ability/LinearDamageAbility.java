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

    public static final PalladiumProperty<Float> DAMAGE = new FloatProperty("damage").configurable("Damage dealt to each target");
    public static final PalladiumProperty<Float> RANGE = new FloatProperty("range").configurable("Maximum distance the line travels");
    public static final PalladiumProperty<Float> WIDTH = new FloatProperty("width").configurable("Width of the damaging line");
    public static final PalladiumProperty<Integer> TRAVEL_TIME = new IntegerProperty("travel_time").configurable("Ticks it takes for the line to reach max range");
    public static final PalladiumProperty<String> DAMAGE_TYPE = new StringProperty("damage_type").configurable("Damage type used");
    public static final PalladiumProperty<String> PARTICLE = new StringProperty("particle").configurable("Particle spawned as the linear damage travels");
    public static final PalladiumProperty<Integer> MAX_WALL_THICKNESS = new IntegerProperty("max_wall_thickness").configurable("Maximum wall thickness this line can penetrate");
    public static final PalladiumProperty<String[]> COMMANDS_ON_TARGETS = new StringArrayProperty("commands_on_targets").configurable("Commands to run as targets hit by the line");
    public static final PalladiumProperty<String[]> COMMANDS_ON_ALLIES = new StringArrayProperty("commands_on_allies").configurable("Commands to run as allies hit by the line");
    public static final PalladiumProperty<Boolean> IGNORE_PITCH = new BooleanProperty("ignore_pitch").configurable("If true, the line travels horizontally forward, ignoring look pitch");
    public static final PalladiumProperty<Boolean> SPAWN_ARMOR_STAND = new BooleanProperty("spawn_armor_stand").configurable("If true, spawns power stands along the linear path");
    public static final PalladiumProperty<Integer> ARMOR_STAND_INTERVAL = new IntegerProperty("armor_stand_interval").configurable("Ticks between power stand spawns along the line");
    public static final PalladiumProperty<Integer> ARMOR_STAND_LIFETIME = new IntegerProperty("armor_stand_lifetime").configurable("Power stand lifetime");
    public static final PalladiumProperty<Float> ARMOR_STAND_AOE_DAMAGE = new FloatProperty("armor_stand_aoe_damage").configurable("Power stand AOE damage");
    public static final PalladiumProperty<Float> ARMOR_STAND_AOE_RADIUS = new FloatProperty("armor_stand_aoe_radius").configurable("Power stand AOE radius");
    public static final PalladiumProperty<String> ARMOR_STAND_AOE_DAMAGE_TYPE = new StringProperty("armor_stand_aoe_damage_type").configurable("Power stand damage type");
    public static final PalladiumProperty<Boolean> ARMOR_STAND_ENABLE_DAMAGE = new BooleanProperty("armor_stand_enable_damage").configurable("Enable power stand damage");
    public static final PalladiumProperty<Boolean> ARMOR_STAND_DAMAGE_ON_LAST_TICK = new BooleanProperty("armor_stand_damage_on_last_tick").configurable("Damage on last tick");
    public static final PalladiumProperty<Float> ARMOR_STAND_KNOCKBACK_ON_LAST_TICK = new FloatProperty("armor_stand_knockback_on_last_tick").configurable("Last tick knockback");
    public static final PalladiumProperty<Float> ARMOR_STAND_TARGET_COMMAND_RADIUS = new FloatProperty("armor_stand_target_command_radius").configurable("Target command radius");
    public static final PalladiumProperty<Float> ARMOR_STAND_PULL_STRENGTH = new FloatProperty("armor_stand_pull_strength").configurable("Pull strength");
    public static final PalladiumProperty<Boolean> ARMOR_STAND_INVERT_PULL = new BooleanProperty("armor_stand_invert_pull").configurable("Invert pull");
    public static final PalladiumProperty<String> ARMOR_STAND_POWER = new StringProperty("armor_stand_power").configurable("Power given to stand");
    public static final PalladiumProperty<String[]> ARMOR_STAND_FIRST_TICK_COMMANDS = new StringArrayProperty("armor_stand_first_tick_commands").configurable("First tick commands");
    public static final PalladiumProperty<String[]> ARMOR_STAND_COMMANDS = new StringArrayProperty("armor_stand_commands").configurable("Tick commands");
    public static final PalladiumProperty<String[]> ARMOR_STAND_LAST_TICK_COMMANDS = new StringArrayProperty("armor_stand_last_tick_commands").configurable("Last tick commands");
    public static final PalladiumProperty<String[]> ARMOR_STAND_TARGET_FIRST_TICK_COMMANDS = new StringArrayProperty("armor_stand_target_first_tick_commands").configurable("Target first tick commands");
    public static final PalladiumProperty<String[]> ARMOR_STAND_TARGET_COMMANDS = new StringArrayProperty("armor_stand_target_commands").configurable("Target tick commands");
    public static final PalladiumProperty<String[]> ARMOR_STAND_TARGET_LAST_TICK_COMMANDS = new StringArrayProperty("armor_stand_target_last_tick_commands").configurable("Target last tick commands");
    public static final PalladiumProperty<String> ARMOR_STAND_LOOPING_SOUND = new StringProperty("armor_stand_looping_sound").configurable("Looping sound");
    public static final PalladiumProperty<Float> ARMOR_STAND_LOOPING_SOUND_VOLUME = new FloatProperty("armor_stand_looping_sound_volume").configurable("Looping sound volume");
    public static final PalladiumProperty<Float> ARMOR_STAND_LOOPING_SOUND_PITCH = new FloatProperty("armor_stand_looping_sound_pitch").configurable("Looping sound pitch");
    public static final PalladiumProperty<Boolean> ARMOR_STAND_DESTROY_BLOCKS = new BooleanProperty("armor_stand_destroy_blocks").configurable("Destroy blocks");
    public static final PalladiumProperty<Float> ARMOR_STAND_DESTROY_BLOCK_RADIUS = new FloatProperty("armor_stand_destroy_block_radius").configurable("Destroy block radius");
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
        this.withProperty(IGNORE_PITCH, false);
        this.withProperty(SPAWN_ARMOR_STAND, false);
        this.withProperty(ARMOR_STAND_INTERVAL, 1);
        this.withProperty(ARMOR_STAND_LIFETIME, 20);
        this.withProperty(ARMOR_STAND_AOE_DAMAGE, 0.0F);
        this.withProperty(ARMOR_STAND_AOE_RADIUS, 3.0F);
        this.withProperty(ARMOR_STAND_AOE_DAMAGE_TYPE, "minecraft:magic");
        this.withProperty(ARMOR_STAND_ENABLE_DAMAGE, true);
        this.withProperty(ARMOR_STAND_DAMAGE_ON_LAST_TICK, false);
        this.withProperty(ARMOR_STAND_KNOCKBACK_ON_LAST_TICK, 0.0F);
        this.withProperty(ARMOR_STAND_TARGET_COMMAND_RADIUS, 3.0F);
        this.withProperty(ARMOR_STAND_PULL_STRENGTH, 0.0F);
        this.withProperty(ARMOR_STAND_INVERT_PULL, false);
        this.withProperty(ARMOR_STAND_POWER, "");
        this.withProperty(ARMOR_STAND_FIRST_TICK_COMMANDS, new String[]{});
        this.withProperty(ARMOR_STAND_COMMANDS, new String[]{});
        this.withProperty(ARMOR_STAND_LAST_TICK_COMMANDS, new String[]{});
        this.withProperty(ARMOR_STAND_TARGET_FIRST_TICK_COMMANDS, new String[]{});
        this.withProperty(ARMOR_STAND_TARGET_COMMANDS, new String[]{});
        this.withProperty(ARMOR_STAND_TARGET_LAST_TICK_COMMANDS, new String[]{});
        this.withProperty(ARMOR_STAND_LOOPING_SOUND, "");
        this.withProperty(ARMOR_STAND_LOOPING_SOUND_VOLUME, 1.0F);
        this.withProperty(ARMOR_STAND_LOOPING_SOUND_PITCH, 1.0F);
        this.withProperty(ARMOR_STAND_DESTROY_BLOCKS, false);
        this.withProperty(ARMOR_STAND_DESTROY_BLOCK_RADIUS, 0.0F);
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
                entry.getProperty(MAX_WALL_THICKNESS),
                entry.getProperty(IGNORE_PITCH),
                entry.getProperty(SPAWN_ARMOR_STAND),
                entry.getProperty(ARMOR_STAND_INTERVAL),
                entry.getProperty(ARMOR_STAND_LIFETIME),
                entry.getProperty(ARMOR_STAND_AOE_DAMAGE),
                entry.getProperty(ARMOR_STAND_AOE_RADIUS),
                entry.getProperty(ARMOR_STAND_AOE_DAMAGE_TYPE),
                entry.getProperty(ARMOR_STAND_ENABLE_DAMAGE),
                entry.getProperty(ARMOR_STAND_DAMAGE_ON_LAST_TICK),
                entry.getProperty(ARMOR_STAND_KNOCKBACK_ON_LAST_TICK),
                entry.getProperty(ARMOR_STAND_TARGET_COMMAND_RADIUS),
                entry.getProperty(ARMOR_STAND_PULL_STRENGTH),
                entry.getProperty(ARMOR_STAND_INVERT_PULL),
                entry.getProperty(ARMOR_STAND_POWER),
                entry.getProperty(ARMOR_STAND_FIRST_TICK_COMMANDS),
                entry.getProperty(ARMOR_STAND_COMMANDS),
                entry.getProperty(ARMOR_STAND_LAST_TICK_COMMANDS),
                entry.getProperty(ARMOR_STAND_TARGET_FIRST_TICK_COMMANDS),
                entry.getProperty(ARMOR_STAND_TARGET_COMMANDS),
                entry.getProperty(ARMOR_STAND_TARGET_LAST_TICK_COMMANDS),
                entry.getProperty(ARMOR_STAND_LOOPING_SOUND),
                entry.getProperty(ARMOR_STAND_LOOPING_SOUND_VOLUME),
                entry.getProperty(ARMOR_STAND_LOOPING_SOUND_PITCH),
                entry.getProperty(ARMOR_STAND_DESTROY_BLOCKS),
                entry.getProperty(ARMOR_STAND_DESTROY_BLOCK_RADIUS)
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
        return "Deals damage in a forward-traveling line.";
    }
}
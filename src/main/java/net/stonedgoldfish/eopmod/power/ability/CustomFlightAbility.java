package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomFlightAbility extends Ability {

    public static final PalladiumProperty<Float> SPEED =
            new FloatProperty("speed").configurable("Base flight speed.");

    public static final PalladiumProperty<Float> SPRINT_MULTIPLIER =
            new FloatProperty("sprint_multiplier").configurable("Speed multiplier while sprint flying.");

    public static final PalladiumProperty<Float> ACCELERATION =
            new FloatProperty("acceleration").configurable("How quickly the player accelerates.");

    public static final PalladiumProperty<Float> DRAG =
            new FloatProperty("drag").configurable("How slowly the player loses momentum when not moving.");

    public static final PalladiumProperty<Boolean> ALLOW_SPRINT =
            new BooleanProperty("allow_sprint").configurable("Whether sprint flying is allowed.");

    public record FlightSettings(
            float speed,
            float sprintMultiplier,
            float acceleration,
            float drag,
            boolean allowSprint
    ) {}

    private static final Map<UUID, FlightSettings> FLIGHT_SETTINGS = new HashMap<>();
    private static final Map<UUID, Boolean> FLYING_STATE = new HashMap<>();
    private static final Map<UUID, Boolean> SPRINT_FLYING_STATE = new HashMap<>();

    public CustomFlightAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FEATHER));

        this.withProperty(SPEED, 0.35F);
        this.withProperty(SPRINT_MULTIPLIER, 2.0F);
        this.withProperty(ACCELERATION, 0.35F);
        this.withProperty(DRAG, 0.90F);
        this.withProperty(ALLOW_SPRINT, true);
    }

    public static void resetSprintFlyingHitbox(Player player) {
        if (!isSprintFlying(player)) {
            return;
        }

        player.setPos(
                player.getX(),
                player.getY() + 1.2D,
                player.getZ()
        );

        setSprintFlying(player, false);
        player.refreshDimensions();
    }

    public static boolean isSprintFlying(Player player) {
        return SPRINT_FLYING_STATE.getOrDefault(player.getUUID(), false);
    }

    public static void setSprintFlying(Player player, boolean sprintFlying) {
        boolean old = isSprintFlying(player);

        if (old == sprintFlying) {
            return;
        }

        SPRINT_FLYING_STATE.put(player.getUUID(), sprintFlying);
        player.refreshDimensions();
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player player) {
            enableFlightAbility(player, entry);
        }
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player player) {
            enableFlightAbility(player, entry);
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            disableFlightAbility(player);
        }
    }

    private static void enableFlightAbility(Player player, AbilityInstance entry) {
        FLIGHT_SETTINGS.put(player.getUUID(), new FlightSettings(
                entry.getProperty(SPEED),
                entry.getProperty(SPRINT_MULTIPLIER),
                entry.getProperty(ACCELERATION),
                entry.getProperty(DRAG),
                entry.getProperty(ALLOW_SPRINT)
        ));
    }

    private static void disableFlightAbility(Player player) {
        FLIGHT_SETTINGS.remove(player.getUUID());
        FLYING_STATE.remove(player.getUUID());
        SPRINT_FLYING_STATE.remove(player.getUUID());
        player.refreshDimensions();

        player.setNoGravity(false);
    }

    public static boolean hasCustomFlight(Player player) {
        return FLIGHT_SETTINGS.containsKey(player.getUUID());
    }

    public static FlightSettings getSettings(Player player) {
        return FLIGHT_SETTINGS.get(player.getUUID());
    }

    public static boolean isFlying(Player player) {
        return FLYING_STATE.getOrDefault(player.getUUID(), false);
    }

    public static void setFlying(Player player, boolean flying) {
        if (!hasCustomFlight(player)) {
            flying = false;
        }

        boolean wasFlying = isFlying(player);

        FLYING_STATE.put(player.getUUID(), flying);
        player.setNoGravity(flying);

        if (wasFlying != flying) {
            player.refreshDimensions();
        }

        if (!flying) {
            resetSprintFlyingHitbox(player);
            player.fallDistance = 0.0F;
        }
    }

    public static void toggleFlying(Player player) {
        setFlying(player, !isFlying(player));
    }

    @Override
    public String getDocumentationDescription() {
        return "Allows custom momentum-based flight with configurable speed, acceleration, drag, and sprint multiplier.";
    }
}
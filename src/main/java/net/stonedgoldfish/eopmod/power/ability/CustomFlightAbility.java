package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
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

    public record FlightSettings(float speed, float sprintMultiplier, float acceleration, float drag, boolean allowSprint) {}

    private static final Map<UUID, FlightSettings> FLYING_PLAYERS = new HashMap<>();

    public CustomFlightAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FEATHER));

        this.withProperty(SPEED, 0.35F);
        this.withProperty(SPRINT_MULTIPLIER, 2.0F);
        this.withProperty(ACCELERATION, 0.35F);
        this.withProperty(DRAG, 0.90F);
        this.withProperty(ALLOW_SPRINT, true);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player player) {
            enableFlight(player, entry);
        }
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player player) {
            enableFlight(player, entry);
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            disableFlight(player);
        }
    }

    private static void enableFlight(Player player, AbilityInstance entry) {
        player.getAbilities().mayfly = true;
        player.getAbilities().setFlyingSpeed(0.0F);
        player.onUpdateAbilities();

        FLYING_PLAYERS.put(player.getUUID(), new FlightSettings(
                entry.getProperty(SPEED),
                entry.getProperty(SPRINT_MULTIPLIER),
                entry.getProperty(ACCELERATION),
                entry.getProperty(DRAG),
                entry.getProperty(ALLOW_SPRINT)
        ));
    }

    private static void disableFlight(Player player) {
        FLYING_PLAYERS.remove(player.getUUID());

        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
        }
    }

    public static boolean hasCustomFlight(Player player) {
        return FLYING_PLAYERS.containsKey(player.getUUID());
    }

    public static FlightSettings getSettings(Player player) {
        return FLYING_PLAYERS.get(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Allows custom momentum-based flight with configurable speed, acceleration, drag, and sprint multiplier.";
    }
}
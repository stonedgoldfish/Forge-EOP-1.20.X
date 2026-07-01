package net.stonedgoldfish.eopmod.power.ability;

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

public class NoClickAbility extends Ability {

    public static final PalladiumProperty<Boolean> LEFT_CLICK = new BooleanProperty("left_click").configurable("Disable left click");
    public static final PalladiumProperty<Boolean> RIGHT_CLICK = new BooleanProperty("right_click").configurable("Disable right click");

    private static final Map<UUID, Map<Integer, Settings>> BLOCKED_PLAYERS = new HashMap<>();

    public record Settings(
            boolean leftClick,
            boolean rightClick
    ) {}

    public NoClickAbility() {
        this.withProperty(ICON, new ItemIcon(Items.BARRIER));
        this.withProperty(LEFT_CLICK, true);
        this.withProperty(RIGHT_CLICK, true);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUUID();
        int instanceId = System.identityHashCode(entry);

        if (enabled) {
            BLOCKED_PLAYERS
                    .computeIfAbsent(playerId, id -> new HashMap<>())
                    .put(instanceId, new Settings(
                            entry.getProperty(LEFT_CLICK),
                            entry.getProperty(RIGHT_CLICK)
                    ));
        } else {
            removeInstance(playerId, instanceId);
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        removeInstance(player.getUUID(), System.identityHashCode(entry));
    }

    private static void removeInstance(UUID playerId, int instanceId) {
        Map<Integer, Settings> settings = BLOCKED_PLAYERS.get(playerId);

        if (settings == null) {
            return;
        }

        settings.remove(instanceId);

        if (settings.isEmpty()) {
            BLOCKED_PLAYERS.remove(playerId);
        }
    }

    public static boolean blocksLeftClick(Player player) {
        Map<Integer, Settings> settings = BLOCKED_PLAYERS.get(player.getUUID());

        if (settings == null) {
            return false;
        }

        for (Settings value : settings.values()) {
            if (value.leftClick()) {
                return true;
            }
        }

        return false;
    }

    public static boolean blocksRightClick(Player player) {
        Map<Integer, Settings> settings = BLOCKED_PLAYERS.get(player.getUUID());

        if (settings == null) {
            return false;
        }

        for (Settings value : settings.values()) {
            if (value.rightClick()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBlocked(Player player) {
        Map<Integer, Settings> settings = BLOCKED_PLAYERS.get(player.getUUID());
        return settings != null && !settings.isEmpty();
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents the player from left clicking, right clicking, or both.";
    }
}
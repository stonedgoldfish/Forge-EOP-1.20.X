package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.client.Minecraft;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScreenShakeAbility extends Ability {

    public static final PalladiumProperty<Float> INTENSITY =
            new FloatProperty("intensity")
                    .configurable("How strong the screen shake is.");

    public static final PalladiumProperty<Float> SPEED =
            new FloatProperty("speed")
                    .configurable("How fast the screen shake moves.");

    public static final PalladiumProperty<Boolean> AFFECT_OTHERS =
            new BooleanProperty("affect_others")
                    .configurable("Whether this ability also shakes nearby players' screens.");

    public static final PalladiumProperty<Float> RANGE =
            new FloatProperty("range")
                    .configurable("Range in blocks in which other players are affected.");

    private static final int SHAKE_TIMEOUT_TICKS = 5;

    private static final Map<UUID, Map<UUID, ShakeSettings>> SHAKING_PLAYERS = new HashMap<>();
    private static final Map<UUID, Set<UUID>> PLAYERS_AFFECTED_BY_SOURCE = new HashMap<>();

    public record ShakeSettings(float intensity, float speed, long lastUpdatedTick) {}

    public ScreenShakeAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ECHO_SHARD));
        this.withProperty(INTENSITY, 2.0F);
        this.withProperty(SPEED, 1.0F);
        this.withProperty(AFFECT_OTHERS, false);
        this.withProperty(RANGE, 16.0F);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide) {
            return;
        }

        UUID sourceUUID = entity.getUUID();

        if (!enabled) {
            removeSource(sourceUUID);
            return;
        }

        float intensity = entry.getProperty(INTENSITY);
        float speed = entry.getProperty(SPEED);
        boolean affectOthers = entry.getProperty(AFFECT_OTHERS);
        float range = entry.getProperty(RANGE);
        float rangeSqr = range * range;
        long currentTick = entity.level().getGameTime();

        ShakeSettings settings = new ShakeSettings(intensity, speed, currentTick);
        Set<UUID> currentlyAffected = new HashSet<>();

        for (Player targetPlayer : entity.level().players()) {
            boolean isSourcePlayer = targetPlayer.getUUID().equals(sourceUUID);

            if (!isSourcePlayer && !affectOthers) {
                continue;
            }

            if (!isSourcePlayer && entity.distanceToSqr(targetPlayer) > rangeSqr) {
                continue;
            }

            UUID targetUUID = targetPlayer.getUUID();

            SHAKING_PLAYERS
                    .computeIfAbsent(targetUUID, uuid -> new HashMap<>())
                    .put(sourceUUID, settings);

            currentlyAffected.add(targetUUID);
        }

        Set<UUID> previouslyAffected = PLAYERS_AFFECTED_BY_SOURCE.get(sourceUUID);

        if (previouslyAffected != null) {
            for (UUID oldTarget : previouslyAffected) {
                if (!currentlyAffected.contains(oldTarget)) {
                    removeSourceFromTarget(sourceUUID, oldTarget);
                }
            }
        }

        PLAYERS_AFFECTED_BY_SOURCE.put(sourceUUID, currentlyAffected);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            removeSource(entity.getUUID());
        }
    }

    private static void removeSource(UUID sourceUUID) {
        Set<UUID> affectedPlayers = PLAYERS_AFFECTED_BY_SOURCE.remove(sourceUUID);

        if (affectedPlayers != null) {
            for (UUID targetUUID : affectedPlayers) {
                removeSourceFromTarget(sourceUUID, targetUUID);
            }
        }

        for (Map<UUID, ShakeSettings> sources : SHAKING_PLAYERS.values()) {
            sources.remove(sourceUUID);
        }

        SHAKING_PLAYERS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static void removeSourceFromTarget(UUID sourceUUID, UUID targetUUID) {
        Map<UUID, ShakeSettings> sources = SHAKING_PLAYERS.get(targetUUID);

        if (sources == null) {
            return;
        }

        sources.remove(sourceUUID);

        if (sources.isEmpty()) {
            SHAKING_PLAYERS.remove(targetUUID);
        }
    }

    public static ShakeSettings getShake(Player player) {
        Map<UUID, ShakeSettings> sources = SHAKING_PLAYERS.get(player.getUUID());

        if (sources == null || sources.isEmpty()) {
            return null;
        }

        long currentTick = player.level().getGameTime();

        Iterator<Map.Entry<UUID, ShakeSettings>> iterator = sources.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ShakeSettings> entry = iterator.next();

            if (currentTick - entry.getValue().lastUpdatedTick() > SHAKE_TIMEOUT_TICKS) {
                iterator.remove();
            }
        }

        if (sources.isEmpty()) {
            SHAKING_PLAYERS.remove(player.getUUID());
            return null;
        }

        ShakeSettings strongest = null;

        for (ShakeSettings settings : sources.values()) {
            if (strongest == null || settings.intensity() > strongest.intensity()) {
                strongest = settings;
            }
        }

        return strongest;
    }

    @Override
    public String getDocumentationDescription() {
        return "Shakes the player's screen while active, optionally affecting nearby players.";
    }
}
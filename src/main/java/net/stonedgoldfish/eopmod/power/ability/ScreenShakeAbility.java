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
import net.threetag.palladium.util.property.StringArrayProperty;

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

    public static final PalladiumProperty<String[]> RESISTANCE_TAGS =
            new StringArrayProperty("resistance_tags")
                    .configurable("Entity tags that reduce incoming screen shake intensity.");

    public static final PalladiumProperty<Boolean> END_BURST =
            new BooleanProperty("end_burst")
                    .configurable("When the ability ends, double the screen shake for 1 second and fade out.");

    public static final PalladiumProperty<Boolean> ONLY_GROUNDED_TARGETS =
            new BooleanProperty("only_grounded_targets")
                    .configurable("If true, only players currently on the ground will be affected by screen shake.");

    private static final int SHAKE_TIMEOUT_TICKS = 5;
    private static final int END_BURST_DURATION_TICKS = 20;

    private static final Map<UUID, Map<ShakeSource, ShakeSettings>> SHAKING_PLAYERS = new HashMap<>();
    private static final Map<ShakeSource, Set<UUID>> PLAYERS_AFFECTED_BY_SOURCE = new HashMap<>();

    public record ShakeSource(UUID entityUUID, int abilityInstanceID) {}

    public record ShakeSettings(
            float intensity,
            float speed,
            long lastUpdatedTick,
            boolean fading,
            long fadeStartTick,
            long fadeEndTick,
            boolean endBurst
    ) {}

    public ScreenShakeAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ECHO_SHARD));
        this.withProperty(INTENSITY, 2.0F);
        this.withProperty(SPEED, 1.0F);
        this.withProperty(AFFECT_OTHERS, false);
        this.withProperty(RANGE, 16.0F);
        this.withProperty(RESISTANCE_TAGS, new String[0]);
        this.withProperty(END_BURST, false);
        this.withProperty(ONLY_GROUNDED_TARGETS, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide) {
            return;
        }

        ShakeSource source = new ShakeSource(entity.getUUID(), System.identityHashCode(entry));

        if (!enabled) {
            endOrRemoveSource(source, entity.level().getGameTime(), entry.getProperty(END_BURST));
            return;
        }

        float intensity = entry.getProperty(INTENSITY);
        float speed = entry.getProperty(SPEED);
        boolean affectOthers = entry.getProperty(AFFECT_OTHERS);
        float range = entry.getProperty(RANGE);
        String[] resistanceTags = entry.getProperty(RESISTANCE_TAGS);
        boolean onlyGroundedTargets = entry.getProperty(ONLY_GROUNDED_TARGETS);

        float rangeSqr = range * range;
        long currentTick = entity.level().getGameTime();

        Set<UUID> currentlyAffected = new HashSet<>();

        for (Player targetPlayer : entity.level().players()) {
            boolean isSourcePlayer = targetPlayer.getUUID().equals(source.entityUUID());

            if (!isSourcePlayer && !affectOthers) {
                continue;
            }

            if (!isSourcePlayer && entity.distanceToSqr(targetPlayer) > rangeSqr) {
                continue;
            }

            if (onlyGroundedTargets && !targetPlayer.onGround()) {
                continue;
            }

            float finalIntensity = intensity;

            for (String tag : resistanceTags) {
                if (targetPlayer.getTags().contains(tag)) {
                    finalIntensity *= 0.2F;
                    break;
                }
            }

            ShakeSettings settings = new ShakeSettings(
                    finalIntensity,
                    speed,
                    currentTick,
                    false,
                    0L,
                    0L,
                    entry.getProperty(END_BURST)
            );

            UUID targetUUID = targetPlayer.getUUID();

            SHAKING_PLAYERS
                    .computeIfAbsent(targetUUID, uuid -> new HashMap<>())
                    .put(source, settings);

            currentlyAffected.add(targetUUID);
        }

        Set<UUID> previouslyAffected = PLAYERS_AFFECTED_BY_SOURCE.get(source);

        if (previouslyAffected != null) {
            for (UUID oldTarget : previouslyAffected) {
                if (!currentlyAffected.contains(oldTarget)) {
                    removeSourceFromTarget(source, oldTarget);
                }
            }
        }

        PLAYERS_AFFECTED_BY_SOURCE.put(source, currentlyAffected);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            ShakeSource source = new ShakeSource(entity.getUUID(), System.identityHashCode(entry));
            endOrRemoveSource(source, entity.level().getGameTime(), entry.getProperty(END_BURST));
        }
    }

    private static void endOrRemoveSource(ShakeSource source, long currentTick, boolean endBurst) {
        if (endBurst) {
            startEndBurst(source, currentTick);
        } else {
            removeSource(source);
        }
    }

    private static void startEndBurst(ShakeSource source, long currentTick) {
        Set<UUID> affectedPlayers = PLAYERS_AFFECTED_BY_SOURCE.remove(source);

        if (affectedPlayers == null) {
            return;
        }

        for (UUID targetUUID : affectedPlayers) {
            Map<ShakeSource, ShakeSettings> sources = SHAKING_PLAYERS.get(targetUUID);

            if (sources == null) {
                continue;
            }

            ShakeSettings oldSettings = sources.get(source);

            if (oldSettings == null) {
                continue;
            }

            sources.put(source, new ShakeSettings(
                    oldSettings.intensity() * 2.0F,
                    oldSettings.speed(),
                    currentTick,
                    true,
                    currentTick,
                    currentTick + END_BURST_DURATION_TICKS,
                    oldSettings.endBurst()
            ));
        }
    }

    private static void removeSource(ShakeSource source) {
        Set<UUID> affectedPlayers = PLAYERS_AFFECTED_BY_SOURCE.remove(source);

        if (affectedPlayers != null) {
            for (UUID targetUUID : affectedPlayers) {
                removeSourceFromTarget(source, targetUUID);
            }
        }

        for (Map<ShakeSource, ShakeSettings> sources : SHAKING_PLAYERS.values()) {
            sources.remove(source);
        }

        SHAKING_PLAYERS.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static void removeSourceFromTarget(ShakeSource source, UUID targetUUID) {
        Map<ShakeSource, ShakeSettings> sources = SHAKING_PLAYERS.get(targetUUID);

        if (sources == null) {
            return;
        }

        sources.remove(source);

        if (sources.isEmpty()) {
            SHAKING_PLAYERS.remove(targetUUID);
        }
    }

    public static ShakeSettings getShake(Player player) {
        Map<ShakeSource, ShakeSettings> sources = SHAKING_PLAYERS.get(player.getUUID());

        if (sources == null || sources.isEmpty()) {
            return null;
        }

        long currentTick = player.level().getGameTime();

        Iterator<Map.Entry<ShakeSource, ShakeSettings>> iterator = sources.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ShakeSource, ShakeSettings> entry = iterator.next();
            ShakeSettings settings = entry.getValue();

            if (settings.fading()) {
                if (currentTick >= settings.fadeEndTick()) {
                    iterator.remove();
                }
            } else if (currentTick - settings.lastUpdatedTick() > SHAKE_TIMEOUT_TICKS) {
                if (settings.endBurst()) {
                    entry.setValue(new ShakeSettings(
                            settings.intensity() * 2.0F,
                            settings.speed(),
                            currentTick,
                            true,
                            currentTick,
                            currentTick + END_BURST_DURATION_TICKS,
                            true
                    ));
                } else {
                    iterator.remove();
                }
            }
        }

        if (sources.isEmpty()) {
            SHAKING_PLAYERS.remove(player.getUUID());
            return null;
        }

        ShakeSettings strongest = null;
        float strongestIntensity = 0.0F;

        for (ShakeSettings settings : sources.values()) {
            float effectiveIntensity = getEffectiveIntensity(settings, currentTick);

            if (strongest == null || effectiveIntensity > strongestIntensity) {
                strongest = settings;
                strongestIntensity = effectiveIntensity;
            }
        }

        if (strongest == null) {
            return null;
        }

        return new ShakeSettings(
                strongestIntensity,
                strongest.speed(),
                strongest.lastUpdatedTick(),
                strongest.fading(),
                strongest.fadeStartTick(),
                strongest.fadeEndTick(),
                strongest.endBurst()
        );
    }

    private static float getEffectiveIntensity(ShakeSettings settings, long currentTick) {
        if (!settings.fading()) {
            return settings.intensity();
        }

        float duration = settings.fadeEndTick() - settings.fadeStartTick();

        if (duration <= 0.0F) {
            return 0.0F;
        }

        float remaining = settings.fadeEndTick() - currentTick;
        float fadeMultiplier = Math.max(0.0F, Math.min(1.0F, remaining / duration));

        return settings.intensity() * fadeMultiplier;
    }

    @Override
    public String getDocumentationDescription() {
        return "Shakes the player's screen while active, optionally affecting nearby players.";
    }
}
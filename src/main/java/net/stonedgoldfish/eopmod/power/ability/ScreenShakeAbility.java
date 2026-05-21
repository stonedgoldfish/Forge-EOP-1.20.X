package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScreenShakeAbility extends Ability {

    public static final PalladiumProperty<Float> INTENSITY =
            new FloatProperty("intensity")
                    .configurable("How strong the screen shake is.");

    public static final PalladiumProperty<Float> SPEED =
            new FloatProperty("speed")
                    .configurable("How fast the screen shake moves.");

    private static final Map<UUID, ShakeSettings> SHAKING_PLAYERS = new HashMap<>();

    public record ShakeSettings(float intensity, float speed) {}

    public ScreenShakeAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ECHO_SHARD));
        this.withProperty(INTENSITY, 2.0F);
        this.withProperty(SPEED, 1.0F);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (enabled) {
            SHAKING_PLAYERS.put(
                    player.getUUID(),
                    new ShakeSettings(
                            entry.getProperty(INTENSITY),
                            entry.getProperty(SPEED)
                    )
            );
        } else {
            SHAKING_PLAYERS.remove(player.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity instanceof Player player) {
            SHAKING_PLAYERS.remove(player.getUUID());
        }
    }

    public static ShakeSettings getShake(Player player) {
        return SHAKING_PLAYERS.get(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Shakes the player's screen while active.";
    }
}
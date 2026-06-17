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

    public static final PalladiumProperty<Boolean> LEFT_CLICK =
            new BooleanProperty("left_click").configurable("Disable left click");

    public static final PalladiumProperty<Boolean> RIGHT_CLICK =
            new BooleanProperty("right_click").configurable("Disable right click");

    private static final Map<UUID, Settings> BLOCKED_PLAYERS = new HashMap<>();

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

        if (enabled) {
            BLOCKED_PLAYERS.put(
                    player.getUUID(),
                    new Settings(
                            entry.getProperty(LEFT_CLICK),
                            entry.getProperty(RIGHT_CLICK)
                    )
            );
        } else {
            BLOCKED_PLAYERS.remove(player.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity instanceof Player player) {
            BLOCKED_PLAYERS.remove(player.getUUID());
        }
    }

    public static boolean blocksLeftClick(Player player) {
        Settings settings = BLOCKED_PLAYERS.get(player.getUUID());
        return settings != null && settings.leftClick();
    }

    public static boolean blocksRightClick(Player player) {
        Settings settings = BLOCKED_PLAYERS.get(player.getUUID());
        return settings != null && settings.rightClick();
    }

    public static boolean isBlocked(Player player) {
        return BLOCKED_PLAYERS.containsKey(player.getUUID());
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents the player from left clicking, right clicking, or both.";
    }
}
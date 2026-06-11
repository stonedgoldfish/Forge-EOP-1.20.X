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

public class NoInteractionAbility extends Ability {

    public static final PalladiumProperty<Boolean> BLOCKS = new BooleanProperty("blocks").configurable("Disable block interaction");
    public static final PalladiumProperty<Boolean> ITEMS = new BooleanProperty("items").configurable("Disable item interaction");
    public static final PalladiumProperty<Boolean> ENTITIES = new BooleanProperty("entities").configurable("Disable entity interaction");

    private static final Map<UUID, Settings> BLOCKED_PLAYERS = new HashMap<>();

    public record Settings(
            boolean blocks,
            boolean items,
            boolean entities
    ) {}

    public NoInteractionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.BARRIER));

        this.withProperty(BLOCKS, true);
        this.withProperty(ITEMS, true);
        this.withProperty(ENTITIES, true);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        if (enabled) {
            BLOCKED_PLAYERS.put(
                    player.getUUID(),
                    new Settings(
                            entry.getProperty(BLOCKS),
                            entry.getProperty(ITEMS),
                            entry.getProperty(ENTITIES)
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

    public static boolean blocksBlocks(Player player) {
        Settings settings = BLOCKED_PLAYERS.get(player.getUUID());
        return settings != null && settings.blocks();
    }

    public static boolean blocksItems(Player player) {
        Settings settings = BLOCKED_PLAYERS.get(player.getUUID());
        return settings != null && settings.items();
    }

    public static boolean blocksEntities(Player player) {
        Settings settings = BLOCKED_PLAYERS.get(player.getUUID());
        return settings != null && settings.entities();
    }

    public static boolean isBlocked(Player player) {
        Settings settings = BLOCKED_PLAYERS.get(player.getUUID());
        return settings != null;
    }

    @Override
    public String getDocumentationDescription() {
        return "Prevents the player from interacting with blocks, items, or entities.";
    }
}
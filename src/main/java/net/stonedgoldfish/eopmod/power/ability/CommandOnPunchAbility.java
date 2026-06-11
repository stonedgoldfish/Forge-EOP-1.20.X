package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.stonedgoldfish.eopmod.util.EOPTargeting;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringArrayProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandOnPunchAbility extends Ability {

    public static final PalladiumProperty<String[]> COMMANDS = new StringArrayProperty("commands").configurable("Commands executed as the entity punched by the caster");
    public static final PalladiumProperty<Boolean> ALLOW_PROJECTILES = new BooleanProperty("allow_projectiles").configurable("If true, commands can trigger from projectile/ranged damage caused by the caster");
    private static final Map<UUID, Settings> ACTIVE_PLAYERS = new HashMap<>();

    public record Settings(
            String[] commands,
            boolean allowProjectiles
    ) {}

    public CommandOnPunchAbility() {
        this.withProperty(ICON, new ItemIcon(Items.IRON_SWORD));
        this.withProperty(COMMANDS, new String[]{});
        this.withProperty(ALLOW_PROJECTILES, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (entity.level().isClientSide) {
            return;
        }

        if (enabled) {
            ACTIVE_PLAYERS.put(
                    player.getUUID(),
                    new Settings(
                            entry.getProperty(COMMANDS),
                            entry.getProperty(ALLOW_PROJECTILES)
                    )
            );
        } else {
            ACTIVE_PLAYERS.remove(player.getUUID());
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity instanceof Player player) {
            ACTIVE_PLAYERS.remove(player.getUUID());
        }
    }

    public static boolean hasCommands(Player player) {
        return ACTIVE_PLAYERS.containsKey(player.getUUID());
    }

    public static void runCommands(Player caster, LivingEntity target, boolean projectileHit) {
        Settings settings = ACTIVE_PLAYERS.get(caster.getUUID());

        if (settings == null || settings.commands() == null || settings.commands().length == 0) {
            return;
        }

        if (projectileHit && !settings.allowProjectiles()) {
            return;
        }

        if (!EOPTargeting.isValidTarget(caster, target)) {
            return;
        }

        if (target.getServer() == null) {
            return;
        }

        for (String command : settings.commands()) {
            if (command == null || command.isBlank()) {
                continue;
            }

            String cleanedCommand = command.startsWith("/")
                    ? command.substring(1)
                    : command;

            target.getServer().getCommands().performPrefixedCommand(
                    target.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2),
                    cleanedCommand
            );
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Executes commands as the entity punched by the caster.";
    }
}
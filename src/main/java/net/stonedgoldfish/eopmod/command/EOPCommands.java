package net.stonedgoldfish.eopmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;

import java.util.HashSet;
import java.util.Set;

public class EOPCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("echoesofpower")
                .requires(source -> source.hasPermission(2));

        var entityArg = Commands.argument("entity", EntityArgument.player());

        var add = Commands.literal("add");
        var remove = Commands.literal("remove");

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            String display = power.display();
            String key = power.key();

            add.then(
                    Commands.literal(display)
                            .executes(ctx -> runPowerCommand(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "entity"),
                                    "add",
                                    key
                            ))
            );

            remove.then(
                    Commands.literal(display)
                            .executes(ctx -> runPowerCommand(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "entity"),
                                    "remove",
                                    key
                            ))
            );
        }

        remove.then(
                Commands.literal("*")
                        .executes(ctx -> removeAll(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "entity")
                        ))
        );

        entityArg.then(add);
        entityArg.then(remove);

        root.then(entityArg);

        dispatcher.register(root);
    }

    private static int runPowerCommand(CommandSourceStack source, ServerPlayer target, String action, String baseKey) {
        String resolvedKey = baseKey;

        if (resolvedKey.equals("wyvern")) {
            if (target.getTags().contains("EOP.C.Magala")) {
                resolvedKey = "wyvern_corrupt";
            } else if (target.getTags().contains("EOP.S.Magala")) {
                resolvedKey = "wyvern_adult";
            }
        }

        if (resolvedKey.equals("flame") && action.equals("remove")) {
            runSilent(source, target, "superpower remove eop:flame @s");
            runSilent(source, target, "superpower remove eop:soul_flame @s");
            return 1;
        }

        runSilent(source, target, "superpower " + action + " eop:" + resolvedKey + " @s");
        return 1;
    }

    private static int removeAll(CommandSourceStack source, ServerPlayer target) {
        Set<String> keys = new HashSet<>();

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            keys.add(power.key());
        }

        keys.add("wyvern_corrupt");
        keys.add("wyvern_adult");
        keys.add("soul_flame");

        for (String key : keys) {
            runSilent(source, target, "superpower remove eop:" + key + " @s");
        }

        return 1;
    }

    private static void runSilent(CommandSourceStack source, ServerPlayer target, String command) {
        source.getServer().getCommands().performPrefixedCommand(
                target.createCommandSourceStack().withSuppressedOutput(),
                command
        );
    }
}
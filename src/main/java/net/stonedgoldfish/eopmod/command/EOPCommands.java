package net.stonedgoldfish.eopmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.power.EOPPowerConstants;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;

import java.util.HashSet;
import java.util.Set;

public class EOPCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("echoesofpower")
                .requires(source -> source.hasPermission(2));

        // OLD POWER ADD/REMOVE COMMAND
        var addPower = Commands.literal("add_power");
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
        addPower.then(entityArg);

        root.then(addPower);

        // NEW SKILL POINTS COMMAND
        var skillPoints = Commands.literal("skill_points")
                .then(Commands.argument("entity", EntityArgument.player())
                        .then(buildPowerOperationCommand("skill_points")));

        root.then(skillPoints);

        // NEW LEVEL COMMAND
        var level = Commands.literal("level")
                .then(Commands.argument("entity", EntityArgument.player())
                        .then(buildPowerOperationCommand("level")));

        root.then(level);

        dispatcher.register(root);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildPowerOperationCommand(String type) {
        var powerRoot = Commands.literal("power");

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            String display = power.display();
            String key = power.key();

            var powerLiteral = Commands.literal(display);

            powerLiteral.then(Commands.literal("set")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(ctx -> modifyProperty(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "entity"),
                                    key,
                                    type,
                                    "set",
                                    IntegerArgumentType.getInteger(ctx, "amount")
                            ))));

            powerLiteral.then(Commands.literal("add")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(ctx -> modifyProperty(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "entity"),
                                    key,
                                    type,
                                    "add",
                                    IntegerArgumentType.getInteger(ctx, "amount")
                            ))));

            powerLiteral.then(Commands.literal("remove")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(ctx -> modifyProperty(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "entity"),
                                    key,
                                    type,
                                    "remove",
                                    IntegerArgumentType.getInteger(ctx, "amount")
                            ))));

            powerRoot.then(powerLiteral);
        }

        return powerRoot;
    }

    private static int modifyProperty(
            CommandSourceStack source,
            ServerPlayer target,
            String powerKey,
            String type,
            String operation,
            int amount
    ) {
        if (type.equals("skill_points")) {
            int current = EOPPalladiumProperties.getSkillPoints(target, powerKey);
            int result = applyOperation(current, operation, amount);

            EOPPalladiumProperties.setSkillPoints(target, powerKey, result);

            source.sendSuccess(
                    () -> Component.literal("Set " + powerKey + " skill points to " + result),
                    true
            );

            return 1;
        }

        if (type.equals("level")) {
            int current = EOPPalladiumProperties.getLevel(target, powerKey);
            int result = applyOperation(current, operation, amount);

            result = Math.max(1, Math.min(EOPPowerConstants.MAX_LEVEL, result));

            EOPPalladiumProperties.setLevel(target, powerKey, result);

            int finalResult = result;
            source.sendSuccess(
                    () -> Component.literal("Set " + powerKey + " level to " + finalResult),
                    true
            );

            return 1;
        }

        return 0;
    }

    private static int applyOperation(int current, String operation, int amount) {
        return switch (operation) {
            case "set" -> amount;
            case "add" -> current + amount;
            case "remove" -> Math.max(0, current - amount);
            default -> current;
        };
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
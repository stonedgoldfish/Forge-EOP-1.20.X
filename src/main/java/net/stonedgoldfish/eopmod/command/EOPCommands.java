package net.stonedgoldfish.eopmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.stonedgoldfish.eopmod.util.EOPGameRules;
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

        // POWER ADD/REMOVE COMMAND
        var powerCommand = Commands.literal("power");
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
        powerCommand.then(entityArg);
        root.then(powerCommand);

        // SKILL POINTS COMMAND
        var skillPoints = Commands.literal("skill_points");
        var skillEntityArg = Commands.argument("entity", EntityArgument.player());

        skillEntityArg.then(Commands.literal("set")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyAllProperties(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "entity"),
                                "skill_points",
                                "set",
                                IntegerArgumentType.getInteger(ctx, "amount")
                        ))));

        skillEntityArg.then(Commands.literal("add")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyAllProperties(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "entity"),
                                "skill_points",
                                "add",
                                IntegerArgumentType.getInteger(ctx, "amount")
                        ))));

        skillEntityArg.then(Commands.literal("remove")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyAllProperties(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "entity"),
                                "skill_points",
                                "remove",
                                IntegerArgumentType.getInteger(ctx, "amount")
                        ))));

        skillPoints.then(skillEntityArg);
        root.then(skillPoints);

        // LEVEL COMMAND
        var level = Commands.literal("level");
        var levelEntityArg = Commands.argument("entity", EntityArgument.player());

        levelEntityArg.then(Commands.literal("set")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyAllProperties(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "entity"),
                                "level",
                                "set",
                                IntegerArgumentType.getInteger(ctx, "amount")
                        ))));

        levelEntityArg.then(Commands.literal("add")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyAllProperties(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "entity"),
                                "level",
                                "add",
                                IntegerArgumentType.getInteger(ctx, "amount")
                        ))));

        levelEntityArg.then(Commands.literal("remove")
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> modifyAllProperties(
                                ctx.getSource(),
                                EntityArgument.getPlayer(ctx, "entity"),
                                "level",
                                "remove",
                                IntegerArgumentType.getInteger(ctx, "amount")
                        ))));

        level.then(levelEntityArg);
        root.then(level);

        var gamerule = Commands.literal("gamerule");

        gamerule.then(
                Commands.literal("destructionMode")

                        // QUERY CURRENT VALUE
                        .executes(ctx -> {

                            boolean current = EOPGameRules.isDestructionMode(ctx.getSource().getServer());

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(
                                            "Gamerule destructionMode is currently set to: " + current
                                    ),
                                    false
                            );

                            return 1;
                        })

                        // SET VALUE
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> {

                                    boolean value = BoolArgumentType.getBool(ctx, "value");

                                    EOPGameRules.setDestructionMode(ctx.getSource().getServer(), value);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Gamerule destructionMode is now set to: " + value
                                            ),
                                            true
                                    );

                                    return 1;
                                }))
        );

        root.then(gamerule);

        dispatcher.register(root);
    }

    private static int modifyAllProperties(
            CommandSourceStack source,
            ServerPlayer target,
            String type,
            String operation,
            int amount
    ) {
        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            String powerKey = power.key();

            if (type.equals("skill_points")) {
                int current = EOPPalladiumProperties.getSkillPoints(target, powerKey);
                int result = applyOperation(current, operation, amount);

                EOPPalladiumProperties.setSkillPoints(target, powerKey, result);
            }

            if (type.equals("level")) {
                int current = EOPPalladiumProperties.getLevel(target, powerKey);
                int result = applyOperation(current, operation, amount);

                result = Math.max(1, Math.min(EOPPowerConstants.MAX_LEVEL, result));

                EOPPalladiumProperties.setLevel(target, powerKey, result);
            }
        }

        return 1;
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
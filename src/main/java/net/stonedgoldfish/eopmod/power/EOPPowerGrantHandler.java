package net.stonedgoldfish.eopmod.power;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.threetag.palladium.power.SuperpowerUtil;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EOPPowerGrantHandler {

    public enum GrantResult {
        SUCCESS,
        FAILED_CHANCE,
        INVALID_POWER,
        ALREADY_HAS_POWER,
        SOLO_POWER_BLOCKED,
        PLAYER_HAS_SOLO_POWER,
        POWER_LIMIT_REACHED,
        HAS_FUSION_POWER
    }

    public enum GrantSource {
        CHIP,
        ITEM
    }

    private static final String CHIMERA_CORE_TAG = "EOP.Chimera.Core";
    public static final String DNA_CORRUPTED_TAG = "EOP.DNA.Corrupted";

    public static GrantResult tryGrantPower(
            ServerPlayer player,
            String powerKey,
            double successRate
    ) {
        EOPPowerRegistry.EOPPower power = EOPPowerRegistry.getByKey(powerKey);

        if (power == null) {
            return GrantResult.INVALID_POWER;
        }

        if (hasPower(player, powerKey)) {
            return GrantResult.ALREADY_HAS_POWER;
        }

        if (playerHasFusionPower(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
            return GrantResult.HAS_FUSION_POWER;
        }

        int powerAmount = EOPPalladiumProperties.getPowerAmount(player);
        boolean hasChimeraCore = player.getTags().contains(CHIMERA_CORE_TAG);
        boolean usedChimeraCore = false;

        if (powerAmount >= 1) {
            if (powerAmount == 1 && hasChimeraCore) {
                usedChimeraCore = true;
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
                return GrantResult.POWER_LIMIT_REACHED;
            }
        }

        if (hasPower(player, powerKey)) {
            return GrantResult.ALREADY_HAS_POWER;
        }

        if (power.soloPower() && hasAnyPower(player)) {
            return GrantResult.SOLO_POWER_BLOCKED;
        }

        if (playerHasSoloPower(player)) {
            return GrantResult.PLAYER_HAS_SOLO_POWER;
        }

        double roll = player.getRandom().nextDouble() * 100.0;

        if (roll >= successRate) {
            return GrantResult.FAILED_CHANCE;
        }

        grantPower(player, powerKey);
        runPowerSuccessFunction(player, powerKey);
        if (usedChimeraCore) {
            player.removeTag(CHIMERA_CORE_TAG);
        }

        return GrantResult.SUCCESS;
    }

    public static boolean playerHasFusionPower(ServerPlayer player) {
        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            if (!power.fusionPower()) {
                continue;
            }

            if (hasPower(player, power.key())) {
                return true;
            }
        }

        return false;
    }

    public static GrantResult tryGrantPowerFromSource(
            ServerPlayer player,
            String powerKey,
            double successRate
    ) {
        return tryGrantPower(player, powerKey, successRate);
    }

    public static boolean hasPower(ServerPlayer player, String powerKey) {
        ResourceLocation powerId =
                ResourceLocation.fromNamespaceAndPath("eop", powerKey);

        return SuperpowerUtil.hasSuperpower(player, powerId);
    }

    public static boolean hasAnyPower(ServerPlayer player) {
        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            if (hasPower(player, power.key())) {
                return true;
            }
        }

        return false;
    }

    public static boolean playerHasSoloPower(ServerPlayer player) {
        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            if (!power.soloPower()) {
                continue;
            }

            if (hasPower(player, power.key())) {
                return true;
            }
        }

        return false;
    }

    private static void grantPower(ServerPlayer player, String powerKey) {
        player.getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack()
                        .withSuppressedOutput()
                        .withPermission(2),
                "superpower add eop:" + powerKey + " @s"
        );

        EOPPalladiumProperties.setLevel(player, powerKey, 1);
        EOPPalladiumProperties.setXp(player, powerKey, 0);
        EOPPalladiumProperties.setSkillPoints(player, powerKey, 0);
    }

    public static void replacePowersWithFusion(
            ServerPlayer player,
            String firstPower,
            String secondPower,
            String fusionPower
    ) {
        removePower(player, firstPower);
        removePower(player, secondPower);

        grantPower(player, fusionPower);
        runPowerSuccessFunction(player, fusionPower);

        EOPPowerRegistry.EOPPower power = EOPPowerRegistry.getByKey(fusionPower);
        String powerName = power != null ? power.display().replace("_", " ") : fusionPower;
        int color = power != null ? power.titleColor() : 0xFFFFFF;

        player.sendSystemMessage(
                Component.literal("You've gained the power ")
                        .append(Component.literal(powerName).withStyle(style -> style.withColor(color)))
                        .append("!")
        );
    }

    private static void removePower(ServerPlayer player, String powerKey) {
        player.getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack()
                        .withSuppressedOutput()
                        .withPermission(2),
                "superpower remove eop:" + powerKey + " @s"
        );
    }

    private static void runPowerSuccessFunction(ServerPlayer player, String powerKey) {
        String functionName = powerKey + "_commands";

        player.getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack()
                        .withSuppressedOutput()
                        .withPermission(2),
                "function eop:power_obtain/" + functionName
        );
    }

    public static void sendResultMessage(
            ServerPlayer player,
            GrantResult result,
            String powerKey,
            GrantSource source
    ) {
        EOPPowerRegistry.EOPPower power = EOPPowerRegistry.getByKey(powerKey);

        String powerName = power != null
                ? power.display().replace("_", " ")
                : powerKey;

        switch (result) {
            case SUCCESS -> {
                int color = power != null ? power.titleColor() : 0xFFFFFF;

                player.sendSystemMessage(
                        Component.literal("You've gained the power ")
                                .append(
                                        Component.literal(powerName)
                                                .withStyle(style -> style.withColor(color))
                                )
                                .append("!")
                );
            }

            case POWER_LIMIT_REACHED,
                 HAS_FUSION_POWER -> player.sendSystemMessage(
                    Component.literal("Your body cannot handle the strain!")
                            .withStyle(ChatFormatting.DARK_RED)
            );

            case FAILED_CHANCE -> {
                String message = switch (source) {
                    case CHIP -> "The chip failed.";
                    case ITEM -> "Something went wrong. You did not get the power.";
                };

                player.sendSystemMessage(
                        Component.literal(message)
                                .withStyle(ChatFormatting.RED)
                );
            }

            case ALREADY_HAS_POWER -> player.sendSystemMessage(
                    Component.literal("You already have this power.")
                            .withStyle(ChatFormatting.YELLOW)
            );

            case SOLO_POWER_BLOCKED -> player.sendSystemMessage(
                    Component.literal("This power can only be obtained if you have no other powers.")
                            .withStyle(ChatFormatting.RED)
            );

            case PLAYER_HAS_SOLO_POWER -> player.sendSystemMessage(
                    Component.literal("You already have a solo power.")
                            .withStyle(ChatFormatting.RED)
            );

            case INVALID_POWER -> player.sendSystemMessage(
                    Component.literal("Invalid power.")
                            .withStyle(ChatFormatting.RED)
            );
        }
    }

    public static boolean tryFuseCurrentPowers(ServerPlayer player) {
        List<String> ownedPowers = new ArrayList<>();

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            if (hasPower(player, power.key())) {
                ownedPowers.add(power.key());
            }
        }

        if (ownedPowers.size() != 2) {
            playFailureSound(player);

            player.sendSystemMessage(
                    Component.literal("You need exactly two powers to use this.")
                            .withStyle(ChatFormatting.RED)
            );

            return false;
        }

        String firstPower = ownedPowers.get(0);
        String secondPower = ownedPowers.get(1);

        if (!EOPPowerRegistry.isFusionComponent(firstPower)
                || !EOPPowerRegistry.isFusionComponent(secondPower)) {
            playFailureSound(player);

            player.sendSystemMessage(
                    Component.literal("These powers cannot be fused.")
                            .withStyle(ChatFormatting.RED)
            );

            return false;
        }

        Optional<String> resultOptional =
                EOPFusionRegistry.getFusionResult(firstPower, secondPower);

        if (resultOptional.isEmpty()) {
            playFailureSound(player);

            player.sendSystemMessage(
                    Component.literal("These powers have no known fusion.")
                            .withStyle(ChatFormatting.RED)
            );

            return false;
        }

        String fusionPowerKey = resultOptional.get();

        replacePowersWithFusion(
                player,
                firstPower,
                secondPower,
                fusionPowerKey
        );

        playFusionSuccessEffects(player);

        return true;
    }

    public static boolean tryUseChimeraCore(ServerPlayer player) {
        if (player.getTags().contains(CHIMERA_CORE_TAG)) {
            playFailureSound(player);

            player.sendSystemMessage(
                    Component.literal("Already used!")
                            .withStyle(ChatFormatting.YELLOW)
            );

            return false;
        }

        if (player.getTags().contains(DNA_CORRUPTED_TAG)) {
            playFailureSound(player);

            player.sendSystemMessage(
                    Component.literal("Your DNA is corrupted!")
                            .withStyle(ChatFormatting.RED)
            );

            return false;
        }

        boolean corrupted = player.getRandom().nextDouble() < 0.20D;

        if (corrupted) {
            player.addTag(DNA_CORRUPTED_TAG);

            player.sendSystemMessage(
                    Component.literal("Your DNA has been corrupted!")
                            .withStyle(ChatFormatting.DARK_RED)
            );
        } else {
            player.addTag(CHIMERA_CORE_TAG);

            player.sendSystemMessage(
                    Component.literal("Your DNA accepts the Chimera Core.")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
            );
        }

        playChimeraCoreSuccessEffects(player);

        return true;
    }

    private static void playChimeraCoreSuccessEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        level.sendParticles(
                new DustParticleOptions(new Vector3f(1.0F, 0.4F, 0.4F), 1.2F),
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                40,
                0.5D,
                0.7D,
                0.5D,
                0.02D
        );
    }

    private static void playFailureSound(ServerPlayer player) {
        player.playNotifySound(
                SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),
                SoundSource.PLAYERS,
                1.0F,
                0.5F
        );
    }

    private static void playFusionSuccessEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS,
                1.0F,
                2.0F
        );

        level.sendParticles(
                new DustParticleOptions(new Vector3f(0.6F, 0.2F, 0.8F), 1.4F),
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                45,
                0.6D,
                0.8D,
                0.6D,
                0.02D
        );

        level.sendParticles(
                new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.2F),
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                35,
                0.5D,
                0.7D,
                0.5D,
                0.03D
        );
    }
}
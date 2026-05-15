package net.stonedgoldfish.eopmod.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.power.ability.ImmuneToEffectAbility;
import net.stonedgoldfish.eopmod.power.ability.NoNaturalRegenAbility;
import net.minecraft.world.entity.player.Player;
import net.stonedgoldfish.eopmod.power.ability.GillsAbility;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.stonedgoldfish.eopmod.command.EOPCommands;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.stonedgoldfish.eopmod.power.EOPPowerConstants;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EOPForgeEvents {

    private static boolean hasEopPower(ServerPlayer player, String powerKey) {
        ResourceLocation powerId = ResourceLocation.fromNamespaceAndPath("eop", powerKey);

        return net.threetag.palladium.power.SuperpowerUtil.hasSuperpower(player, powerId);
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (ImmuneToEffectAbility.isImmuneTo(
                event.getEntity(),
                event.getEffectInstance().getEffect()
        )) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (NoNaturalRegenAbility.hasNoNaturalRegen(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.player != null && NoNaturalRegenAbility.shouldHideHungerBar(minecraft.player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!GillsAbility.hasGills(player)) {
            return;
        }

        if (player.isEyeInFluidType(net.minecraftforge.common.ForgeMod.WATER_TYPE.get())) {
            event.setCanBreathe(true);
            event.setCanRefillAir(true);
        } else {
            event.setCanBreathe(false);
            event.setCanRefillAir(false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        EOPPalladiumProperties.setClimbExtra(player, player.getTags().contains("EOP.Extra.Climb"));
        EOPPalladiumProperties.setNightVisionExtra(player, player.getTags().contains("EOP.Extra.Night.Vision"));
        EOPPalladiumProperties.setSmeltingExtra(player, player.getTags().contains("EOP.Extra.Smelting"));
        EOPPalladiumProperties.setFireResistanceExtra(player, player.getTags().contains("EOP.Extra.Fire.Resistance"));
        EOPPalladiumProperties.setEntitySenseExtra(player, player.getTags().contains("EOP.Extra.Entity.Sense"));
        EOPPalladiumProperties.setSuperJumpExtra(player, player.getTags().contains("EOP.Extra.Super.Jump"));
        EOPPalladiumProperties.setEraseExtra(player, player.getTags().contains("EOP.Extra.Erase"));
        EOPPalladiumProperties.setExtraReachExtra(player, player.getTags().contains("EOP.Extra.Extra.Reach"));
        EOPPalladiumProperties.setSlowFallExtra(player, player.getTags().contains("EOP.Extra.Slow.Fall"));
        EOPPalladiumProperties.setLightExtra(player, player.getTags().contains("EOP.Extra.Light"));
        EOPPalladiumProperties.setWaterBreathingExtra(player, player.getTags().contains("EOP.Extra.Water.Breathing"));
        EOPPalladiumProperties.setFrostWalkerExtra(player, player.getTags().contains("EOP.Extra.Frost.Walker"));

        if (!CustomFlightAbility.hasCustomFlight(player)) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05F);
                player.onUpdateAbilities();
            }

            return;
        }

        player.getAbilities().mayfly = true;
        player.getAbilities().setFlyingSpeed(0.0F);
        player.onUpdateAbilities();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        EOPCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        for (EOPPowerRegistry.EOPPower power : EOPPowerRegistry.getAll()) {
            String powerKey = power.key();

            if (!hasEopPower(player, powerKey)) {
                continue;
            }

            int currentXp = EOPPalladiumProperties.getXp(player, powerKey);
            int currentLevel = EOPPalladiumProperties.getLevel(player, powerKey);
            int maxXp = EOPPowerConstants.getMaxXpForLevel(currentLevel);

            float mobMaxHealth = event.getEntity().getMaxHealth();

            int xpGain = Math.round(mobMaxHealth / 5.0F);

            xpGain = Math.max(1, xpGain);
            xpGain = Math.min(50, xpGain);
            currentXp += xpGain;

            if (currentLevel < EOPPowerConstants.MAX_LEVEL) {
                if (currentXp >= maxXp) {
                    currentXp = 0;
                    currentLevel++;

                    int currentSkillPoints = EOPPalladiumProperties.getSkillPoints(player, powerKey);
                    EOPPalladiumProperties.setSkillPoints(player, powerKey, currentSkillPoints + 3);

                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§6" + power.display().replace("_", " ")
                                            + " leveled up to Level "
                                            + currentLevel + "!"
                            )
                    );
                }
            } else {
                currentXp = EOPPowerConstants.getMaxXpForLevel(currentLevel);
            }

            EOPPalladiumProperties.setXp(player, powerKey, currentXp);
            EOPPalladiumProperties.setLevel(player, powerKey, currentLevel);
        }
    }
}
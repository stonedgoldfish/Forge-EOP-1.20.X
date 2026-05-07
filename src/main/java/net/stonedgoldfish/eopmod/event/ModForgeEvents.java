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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.stonedgoldfish.eopmod.power.ability.GillsAbility;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModForgeEvents {

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

        if (player.isEyeInFluid(FluidTags.WATER)) {
            event.setCanBreathe(true);
            event.setCanRefillAir(true);
        } else {
            event.setCanBreathe(false);
            event.setCanRefillAir(false);
        }
    }
}
package net.stonedgoldfish.eopmod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.threetag.palladiumcore.forge.PalladiumCoreForge;
import org.slf4j.Logger;
import net.stonedgoldfish.eopmod.power.ability.EOPAbilities;
import net.stonedgoldfish.eopmod.power.condition.EOPConditions;
import net.stonedgoldfish.eopmod.client.EOPClientEvents;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.effect.EOPEffects;
import net.stonedgoldfish.eopmod.entity.EOPEntities;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EOPMod.MOD_ID)
public class EOPMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "eop";
    public static final Logger LOGGER = LogUtils.getLogger();


    public EOPMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        PalladiumCoreForge.registerModEventBus(MOD_ID, modEventBus);
        EOPAbilities.ABILITIES.register();
        EOPConditions.CONDITIONS.register();
        EOPPalladiumProperties.init();
        EOPNetwork.register();
        EOPEntities.ENTITIES.register(modEventBus);
        EOPEffects.MOB_EFFECTS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        if (net.threetag.palladiumcore.util.Platform.isClient()) {
            EOPClientEvents.init();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}

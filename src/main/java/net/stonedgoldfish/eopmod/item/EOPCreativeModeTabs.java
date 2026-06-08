package net.stonedgoldfish.eopmod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class EOPCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "eopmod");

    public static final RegistryObject<CreativeModeTab> EOP_TAB =
            CREATIVE_MODE_TABS.register("eop_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("creativetab.eopmod.eop_tab"))
                            .icon(() -> EOPItems.ARGON_CRYSTAL.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(EOPItems.ARGON_CRYSTAL.get());
                                output.accept(EOPItems.ARGON_PEGMATITE.get());
                                output.accept(EOPItems.DEEPSLATE_ARGON_PEGMATITE.get());
                                output.accept(EOPItems.ARGON_HELMET.get());
                                output.accept(EOPItems.ARGON_CHESTPLATE.get());
                                output.accept(EOPItems.ARGON_LEGGINGS.get());
                                output.accept(EOPItems.ARGON_BOOTS.get());
                                output.accept(EOPItems.ARGON_SWORD.get());
                                output.accept(EOPItems.ARGON_PICKAXE.get());
                                output.accept(EOPItems.ARGON_AXE.get());
                                output.accept(EOPItems.ARGON_SHOVEL.get());
                                output.accept(EOPItems.ARGON_HOE.get());
                            })
                            .build()
            );
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
package net.stonedgoldfish.eopmod.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EOPMod.MOD_ID);

    public static final RegistryObject<MenuType<GeneticSequencerMenu>> GENETIC_SEQUENCER_MENU =
            MENUS.register("genetic_sequencer_menu",
                    () -> IForgeMenuType.create(GeneticSequencerMenu::new));
}
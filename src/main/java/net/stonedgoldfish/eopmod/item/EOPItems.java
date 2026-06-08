package net.stonedgoldfish.eopmod.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.block.EOPBlocks;

public class EOPItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EOPMod.MOD_ID);

    public static final RegistryObject<Item> ARGON_CRYSTAL =
            ITEMS.register("argon_crystal", () ->
                    new ArgonCrystalItem(new Item.Properties().stacksTo(1))
            );
    public static final RegistryObject<Item> ARGON_PEGMATITE =
            ITEMS.register("argon_pegmatite", () ->
                    new BlockItem(EOPBlocks.ARGON_PEGMATITE.get(), new Item.Properties())
            );
    public static final RegistryObject<Item> DEEPSLATE_ARGON_PEGMATITE =
            ITEMS.register("deepslate_argon_pegmatite", () ->
                    new BlockItem(EOPBlocks.DEEPSLATE_ARGON_PEGMATITE.get(), new Item.Properties())
            );
    public static final RegistryObject<Item> ARGON_HELMET =
            ITEMS.register("argon_helmet", () ->
                    new ArgonArmorItem(EOPArmorMaterials.ARGON, ArmorItem.Type.HELMET, new Item.Properties())
            );

    public static final RegistryObject<Item> ARGON_CHESTPLATE =
            ITEMS.register("argon_chestplate", () ->
                    new ArgonArmorItem(EOPArmorMaterials.ARGON, ArmorItem.Type.CHESTPLATE, new Item.Properties())
            );

    public static final RegistryObject<Item> ARGON_LEGGINGS =
            ITEMS.register("argon_leggings", () ->
                    new ArgonArmorItem(EOPArmorMaterials.ARGON, ArmorItem.Type.LEGGINGS, new Item.Properties())
            );

    public static final RegistryObject<Item> ARGON_BOOTS =
            ITEMS.register("argon_boots", () ->
                    new ArgonArmorItem(EOPArmorMaterials.ARGON, ArmorItem.Type.BOOTS, new Item.Properties())
            );
    public static final RegistryObject<Item> ARGON_SWORD =
            ITEMS.register("argon_sword", () ->
                    new ArgonSwordItem(EOPTiers.ARGON, 3, -2.4F, new Item.Properties())
            );

    public static final RegistryObject<Item> ARGON_PICKAXE =
            ITEMS.register("argon_pickaxe", () ->
                    new ArgonPickaxeItem(EOPTiers.ARGON, 1, -2.8F, new Item.Properties())
            );

    public static final RegistryObject<Item> ARGON_AXE =
            ITEMS.register("argon_axe", () ->
                    new ArgonAxeItem(EOPTiers.ARGON, 5.0F, -3.0F, new Item.Properties())
            );

    public static final RegistryObject<Item> ARGON_SHOVEL =
            ITEMS.register("argon_shovel", () ->
                    new ArgonShovelItem(EOPTiers.ARGON, 1.5F, -3.0F, new Item.Properties())
            );

    public static final RegistryObject<Item> ARGON_HOE =
            ITEMS.register("argon_hoe", () ->
                    new ArgonHoeItem(EOPTiers.ARGON, -4, 0.0F, new Item.Properties())
            );
}
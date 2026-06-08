package net.stonedgoldfish.eopmod.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EOPMod.MOD_ID);

    public static final RegistryObject<Block> TEMPORARY_OBSIDIAN =
            BLOCKS.register("temporary_obsidian", () ->
                    new TemporaryObsidianBlock(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .randomTicks()
                    )
            );
    public static final RegistryObject<Block> DIMENSION_BIT =
            BLOCKS.register("dimension_bit", () ->
                    new Block(BlockBehaviour.Properties
                            .of()
                            .strength(-1.0F, 3600000.0F)
                            .noLootTable()
                            .sound(SoundType.STONE)
                    )
            );
    public static final RegistryObject<Block> ARGON_PEGMATITE =
            BLOCKS.register("argon_pegmatite", () ->
                    new DropExperienceBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .strength(3.0F, 3.0F)
                                    .requiresCorrectToolForDrops(),
                            UniformInt.of(3, 7)
                    )
            );
    public static final RegistryObject<Block> DEEPSLATE_ARGON_PEGMATITE =
            BLOCKS.register("deepslate_argon_pegmatite", () ->
                    new DropExperienceBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.DEEPSLATE)
                                    .strength(4.5F, 3.0F)
                                    .requiresCorrectToolForDrops(),
                            UniformInt.of(3, 7)
                    )
            );
}
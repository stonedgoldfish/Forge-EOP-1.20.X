package net.stonedgoldfish.eopmod.worldgen.dimension;

import net.minecraft.resources.ResourceKey;
import net.stonedgoldfish.eopmod.EOPMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class EOPDimensions {
    public static final ResourceKey<Level> POCKET_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("eop", "pocket_dimension")
    );
}

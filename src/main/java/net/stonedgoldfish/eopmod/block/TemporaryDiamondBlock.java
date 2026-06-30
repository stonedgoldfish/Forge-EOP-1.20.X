package net.stonedgoldfish.eopmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TemporaryDiamondBlock extends Block {

    public TemporaryDiamondBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state,
                        net.minecraft.world.level.Level level,
                        BlockPos pos,
                        BlockState oldState,
                        boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 200); // 10 seconds
        }
    }

    @Override
    public void tick(BlockState state,
                     ServerLevel level,
                     BlockPos pos,
                     RandomSource random) {

        level.destroyBlock(pos, false);
    }
}
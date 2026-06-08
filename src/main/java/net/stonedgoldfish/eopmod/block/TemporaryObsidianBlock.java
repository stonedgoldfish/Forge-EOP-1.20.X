package net.stonedgoldfish.eopmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TemporaryObsidianBlock extends Block {

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    public TemporaryObsidianBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);

        if (age < 3) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            level.scheduleTick(pos, this, random.nextInt(40, 80));
        } else {
            level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        }
    }
}
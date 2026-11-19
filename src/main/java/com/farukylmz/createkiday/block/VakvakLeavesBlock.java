package com.farukylmz.createkiday.block;

import com.farukylmz.createkiday.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class VakvakLeavesBlock extends LeavesBlock {
    public VakvakLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        BlockPos fruitPos = pos.below();
        if (random.nextInt(220) == 0 && level.isEmptyBlock(fruitPos)
                && ModBlocks.VAKVAK_FRUIT.get().defaultBlockState().canSurvive(level, fruitPos)) {
            level.setBlock(fruitPos, ModBlocks.VAKVAK_FRUIT.get().defaultBlockState(), 3);
        }
    }
}

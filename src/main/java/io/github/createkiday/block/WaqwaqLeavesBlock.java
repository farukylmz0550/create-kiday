package io.github.createkiday.block;

import io.github.createkiday.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class WaqwaqLeavesBlock extends LeavesBlock {
    public WaqwaqLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PERSISTENT);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        BlockPos fruitPos = pos.below();
        if (!state.getValue(PERSISTENT) && random.nextInt(1200) == 0 && level.isEmptyBlock(fruitPos)
                && ModBlocks.WAQWAQ_FRUIT.get().defaultBlockState().canSurvive(level, fruitPos)) {
            level.setBlock(fruitPos, ModBlocks.WAQWAQ_FRUIT.get().defaultBlockState(), 3);
        }
    }
}

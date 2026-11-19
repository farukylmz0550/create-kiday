package com.farukylmz.createkiday.worldgen;

import com.farukylmz.createkiday.registry.ModBlocks;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Narrow, straight Vakvak trunk with two to four distinct branch arms and leaf clusters. */
public final class VakvakTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_HEIGHT = 8;
    private static final int MAX_HEIGHT = 12;

    public VakvakTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos base = context.origin();
        RandomSource random = context.random();
        if (!canGrowOn(level, base.below()) || !spaceIsClear(level, base, MAX_HEIGHT + 3)) return false;

        int height = MIN_HEIGHT + random.nextInt(MAX_HEIGHT - MIN_HEIGHT + 1);
        int branchCount = 2 + random.nextInt(3);
        BlockState log = ModBlocks.VAKVAK_LOG.get().defaultBlockState().setValue(
                net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int y = 0; y < height; y++) level.setBlock(base.above(y), log, 3);

        Direction[] directions = shuffledHorizontalDirections(random);
        List<BlockPos> branchTips = new ArrayList<>(branchCount);
        for (int branch = 0; branch < branchCount; branch++) {
            Direction direction = directions[branch];
            int startY = height - 3 - branch / 2;
            int length = 2 + random.nextInt(3);
            BlockPos tip = base.above(startY);
            for (int step = 1; step <= length; step++) {
                BlockPos branchPos = base.above(startY + (step - 1) / 2).relative(direction, step);
                BlockState branchLog = ModBlocks.VAKVAK_LOG.get().defaultBlockState().setValue(
                        net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                        direction.getAxis());
                level.setBlock(branchPos, branchLog, 3);
                tip = branchPos;
            }
            branchTips.add(tip);
        }

        BlockPos crown = base.above(height);
        makeCluster(level, crown, 2);
        for (BlockPos tip : branchTips) makeCluster(level, tip.above(), 1 + random.nextInt(2));
        return true;
    }

    private static boolean canGrowOn(LevelReader level, BlockPos soil) {
        return level.getBlockState(soil).is(net.minecraft.tags.BlockTags.DIRT);
    }

    private static boolean spaceIsClear(LevelReader level, BlockPos base, int height) {
        for (int y = 0; y < height; y++) {
            BlockState state = level.getBlockState(base.above(y));
            if (!state.isAir() && !state.is(ModBlocks.VAKVAK_SAPLING.get())
                    && !state.is(ModBlocks.VAKVAK_LEAVES.get())) return false;
        }
        return true;
    }

    private static Direction[] shuffledHorizontalDirections(RandomSource random) {
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = directions[i]; directions[i] = directions[j]; directions[j] = tmp;
        }
        return directions;
    }

    private static void makeCluster(WorldGenLevel level, BlockPos center, int radius) {
        BlockState leaves = ModBlocks.VAKVAK_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.DISTANCE, 7);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distance = x * x + y * y + z * z;
                    if (distance > radius * radius + 1) continue;
                    BlockPos pos = center.offset(x, y, z);
                    BlockState existing = level.getBlockState(pos);
                    if (existing.isAir() || existing.is(ModBlocks.VAKVAK_LEAVES.get())) level.setBlock(pos, leaves, 3);
                }
            }
        }
    }
}

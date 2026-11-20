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

/** Narrow, straight Waqwaq trunk with two to four distinct branch arms and leaf clusters. */
public final class WaqwaqTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_HEIGHT = 8;
    private static final int MAX_HEIGHT = 12;

    public WaqwaqTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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
        BlockState log = ModBlocks.WAQWAQ_LOG.get().defaultBlockState().setValue(
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
                BlockState branchLog = ModBlocks.WAQWAQ_LOG.get().defaultBlockState().setValue(
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
        plantInitialFruit(level, random, crown, branchTips);
        return true;
    }

    private static void plantInitialFruit(WorldGenLevel level, RandomSource random, BlockPos crown,
            List<BlockPos> branchTips) {
        List<BlockPos> supports = new ArrayList<>();
        collectFruitSupports(level, crown, 2, supports);
        for (BlockPos tip : branchTips) collectFruitSupports(level, tip.above(), 2, supports);
        int target = 2 + random.nextInt(4);
        int placed = 0;
        while (placed < target && !supports.isEmpty()) {
            BlockPos support = supports.remove(random.nextInt(supports.size()));
            BlockPos fruitPos = support.below();
            BlockState fruit = ModBlocks.WAQWAQ_FRUIT.get().defaultBlockState();
            if (level.isEmptyBlock(fruitPos) && fruit.canSurvive(level, fruitPos)) {
                level.setBlock(fruitPos, fruit, 3);
                placed++;
            }
        }
        // Keep the promised minimum even when the random branch layout obstructs some hanging spots.
        Direction[] fallback = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
        for (Direction direction : fallback) {
            if (placed >= 2) break;
            BlockPos support = crown.below().relative(direction, 2);
            BlockPos fruitPos = support.below();
            if (level.isEmptyBlock(support) && level.isEmptyBlock(fruitPos)) {
                level.setBlock(support, ModBlocks.WAQWAQ_LEAVES.get().defaultBlockState(), 3);
                level.setBlock(fruitPos, ModBlocks.WAQWAQ_FRUIT.get().defaultBlockState(), 3);
                placed++;
            }
        }
    }

    private static void collectFruitSupports(WorldGenLevel level, BlockPos center, int radius,
            List<BlockPos> supports) {
        for (int x = -radius; x <= radius; x++) for (int y = -radius; y <= radius; y++)
            for (int z = -radius; z <= radius; z++) {
                BlockPos support = center.offset(x, y, z);
                if (level.getBlockState(support).is(ModBlocks.WAQWAQ_LEAVES.get())
                        && level.isEmptyBlock(support.below()) && !supports.contains(support)) supports.add(support);
            }
    }

    private static boolean canGrowOn(LevelReader level, BlockPos soil) {
        return level.getBlockState(soil).is(net.minecraft.tags.BlockTags.DIRT);
    }

    private static boolean spaceIsClear(LevelReader level, BlockPos base, int height) {
        for (int y = 0; y < height; y++) {
            BlockState state = level.getBlockState(base.above(y));
            if (!state.isAir() && !state.is(ModBlocks.WAQWAQ_SAPLING.get())
                    && !state.is(ModBlocks.WAQWAQ_LEAVES.get()) && !state.canBeReplaced()) return false;
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
        BlockState leaves = ModBlocks.WAQWAQ_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.DISTANCE, 7);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distance = x * x + y * y + z * z;
                    if (distance > radius * radius + 1) continue;
                    BlockPos pos = center.offset(x, y, z);
                    BlockState existing = level.getBlockState(pos);
                    if (existing.isAir() || existing.is(ModBlocks.WAQWAQ_LEAVES.get()) || existing.canBeReplaced())
                        level.setBlock(pos, leaves, 3);
                }
            }
        }
    }
}

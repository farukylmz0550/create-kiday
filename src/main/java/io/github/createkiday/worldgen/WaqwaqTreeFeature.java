package io.github.createkiday.worldgen;

import io.github.createkiday.registry.ModBlocks;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Broad, irregular Waqwaq canopy on a vertical trunk with visible upper branch arms. */
public final class WaqwaqTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_HEIGHT = 12;
    private static final int MAX_HEIGHT = 16;
    private static final List<Heading> HEADINGS = List.of(
            new Heading(0, -1, true), new Heading(1, -1, true), new Heading(1, 0, true),
            new Heading(1, 1, true), new Heading(0, 1, true), new Heading(-1, 1, false),
            new Heading(-1, 0, true), new Heading(-1, -1, false));

    public WaqwaqTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos base = context.origin();
        RandomSource random = context.random();
        if (!canGrowOn(level, base.below())) return false;

        int height = MIN_HEIGHT + random.nextInt(MAX_HEIGHT - MIN_HEIGHT + 1);
        int branchCount = 4 + random.nextInt(4);
        List<Branch> branches = makeBranches(random, height, branchCount);
        if (!trunkIsClear(level, base, height) || !branchesAreClear(level, base, branches)) return false;

        BlockState trunk = ModBlocks.WAQWAQ_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int y = 0; y < height; y++) level.setBlock(base.above(y), trunk, 3);

        List<BlockPos> branchTips = new ArrayList<>(branches.size());
        for (Branch branch : branches) {
            BlockPos tip = base.above(branch.startY());
            for (int step = 1; step <= branch.length(); step++) {
                BlockPos branchPos = branchPosition(base, branch, step);
                BlockState branchLog = ModBlocks.WAQWAQ_LOG.get().defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, branch.axisAt(step));
                level.setBlock(branchPos, branchLog, 3);
                tip = branchPos;
            }
            branchTips.add(tip);
        }

        makeBroadCrown(level, random, base.above(height - 1));
        for (Branch branch : branches) {
            BlockPos tip = branchPosition(base, branch, branch.length());
            int clusterRadius = 1 + random.nextInt(2);
            makeLeafCluster(level, tip.above(), clusterRadius);
            if (branch.length() >= 5 && random.nextBoolean()) {
                makeLeafCluster(level, branchPosition(base, branch, branch.length() - 2).above(), 1);
            }
        }

        plantInitialFruit(level, random, base, height);
        return true;
    }

    private static List<Branch> makeBranches(RandomSource random, int height, int branchCount) {
        List<Heading> headings = new ArrayList<>(HEADINGS);
        for (int i = headings.size() - 1; i > 0; i--) {
            int swap = random.nextInt(i + 1);
            Heading heading = headings.get(i);
            headings.set(i, headings.get(swap));
            headings.set(swap, heading);
        }

        List<Branch> branches = new ArrayList<>(branchCount);
        int firstLevel = Math.max(5, height / 2);
        int lastLevel = height - 2;
        for (int i = 0; i < branchCount; i++) {
            int startY = firstLevel + (lastLevel - firstLevel) * i / (branchCount - 1);
            Heading heading = headings.get(i);
            branches.add(new Branch(heading, startY, 3 + random.nextInt(4)));
        }
        return branches;
    }

    private static BlockPos branchPosition(BlockPos base, Branch branch, int step) {
        Heading heading = branch.heading();
        int xSteps;
        int zSteps;
        if (heading.dx() == 0) {
            xSteps = 0;
            zSteps = step;
        } else if (heading.dz() == 0) {
            xSteps = step;
            zSteps = 0;
        } else {
            xSteps = heading.firstAxisX() ? (step + 1) / 2 : step / 2;
            zSteps = step - xSteps;
        }
        return base.offset(heading.dx() * xSteps, branch.startY() + (step - 1) / 3,
                heading.dz() * zSteps);
    }

    private static boolean branchesAreClear(LevelReader level, BlockPos base, List<Branch> branches) {
        for (Branch branch : branches) {
            for (int step = 1; step <= branch.length(); step++) {
                if (!canReplaceForTree(level.getBlockState(branchPosition(base, branch, step)))) return false;
            }
        }
        return true;
    }

    private static boolean trunkIsClear(LevelReader level, BlockPos base, int height) {
        for (int y = 0; y < height + 5; y++) {
            if (!canReplaceForTree(level.getBlockState(base.above(y)))) return false;
        }
        return true;
    }

    private static boolean canReplaceForTree(BlockState state) {
        return state.isAir() || state.is(ModBlocks.WAQWAQ_LEAVES.get()) || state.canBeReplaced();
    }

    private static boolean canGrowOn(LevelReader level, BlockPos soil) {
        return level.getBlockState(soil).is(BlockTags.DIRT);
    }

    private static void makeBroadCrown(WorldGenLevel level, RandomSource random, BlockPos center) {
        BlockState leaves = leaves();
        int radiusX = 4 + random.nextInt(2);
        int radiusZ = 4 + random.nextInt(2);
        int radiusY = 3 + random.nextInt(2);
        int driftX = random.nextInt(3) - 1;
        int driftZ = random.nextInt(3) - 1;

        for (int y = -radiusY; y <= radiusY; y++) {
            float vertical = y / (float) radiusY;
            float layerScale = (float) Math.sqrt(Math.max(0.0F, 1.0F - vertical * vertical));
            int layerRadiusX = Math.max(1, Math.round(radiusX * layerScale + random.nextFloat() * 0.7F));
            int layerRadiusZ = Math.max(1, Math.round(radiusZ * layerScale + random.nextFloat() * 0.7F));
            int centerX = driftX + (y > 1 ? 1 : 0);
            int centerZ = driftZ + (y < -1 ? -1 : 0);

            for (int x = -layerRadiusX; x <= layerRadiusX; x++) {
                for (int z = -layerRadiusZ; z <= layerRadiusZ; z++) {
                    float ellipse = x * x / (float) (layerRadiusX * layerRadiusX)
                            + z * z / (float) (layerRadiusZ * layerRadiusZ);
                    if (ellipse > 1.0F + random.nextFloat() * 0.12F) continue;
                    if (random.nextInt(13) == 0) continue;
                    placeLeaves(level, center.offset(centerX + x, y, centerZ + z), leaves);
                }
            }
        }
    }

    private static void makeLeafCluster(WorldGenLevel level, BlockPos center, int radius) {
        BlockState leaves = leaves();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius + 1) continue;
                    placeLeaves(level, center.offset(x, y, z), leaves);
                }
            }
        }
    }

    private static BlockState leaves() {
        return ModBlocks.WAQWAQ_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.DISTANCE, 7);
    }

    private static void placeLeaves(WorldGenLevel level, BlockPos pos, BlockState leaves) {
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(ModBlocks.WAQWAQ_LEAVES.get()) || existing.canBeReplaced()) {
            level.setBlock(pos, leaves, 3);
        }
    }

    private static void plantInitialFruit(WorldGenLevel level, RandomSource random, BlockPos base, int height) {
        List<BlockPos> supports = new ArrayList<>();
        for (int y = height / 2; y <= height + 5; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos support = base.offset(x, y, z);
                    if (level.getBlockState(support).is(ModBlocks.WAQWAQ_LEAVES.get())
                            && level.isEmptyBlock(support.below())) {
                        supports.add(support);
                    }
                }
            }
        }

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

        Direction[] fallback = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
        for (Direction direction : fallback) {
            if (placed >= 2) break;
            BlockPos support = base.above(height).relative(direction, 2);
            BlockPos fruitPos = support.below();
            if (level.isEmptyBlock(support) && level.isEmptyBlock(fruitPos)) {
                level.setBlock(support, leaves(), 3);
                level.setBlock(fruitPos, ModBlocks.WAQWAQ_FRUIT.get().defaultBlockState(), 3);
                placed++;
            }
        }
    }

    private record Heading(int dx, int dz, boolean firstAxisX) {
    }

    private record Branch(Heading heading, int startY, int length) {
        private Direction.Axis axisAt(int step) {
            if (heading.dx() == 0) return Direction.Axis.Z;
            if (heading.dz() == 0) return Direction.Axis.X;
            int xSteps = heading.firstAxisX() ? (step + 1) / 2 : step / 2;
            int previousXSteps = heading.firstAxisX() ? step / 2 : (step - 1) / 2;
            return xSteps != previousXSteps ? Direction.Axis.X : Direction.Axis.Z;
        }
    }
}

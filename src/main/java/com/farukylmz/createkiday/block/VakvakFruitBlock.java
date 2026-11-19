package com.farukylmz.createkiday.block;

import com.farukylmz.createkiday.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.farukylmz.createkiday.registry.ModSounds;

public final class VakvakFruitBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    private static final VoxelShape SHAPE = Block.box(5, 4, 5, 11, 15, 11);

    public VakvakFruitBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected boolean canSurvive(BlockState state, Level level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(ModBlocks.VAKVAK_LEAVES.get());
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }
        if (state.getValue(AGE) < 2 && random.nextInt(6) == 0) {
            level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
        } else if (state.getValue(AGE) == 2 && random.nextInt(180) == 0) {
            level.playSound(null, pos, ModSounds.VAKVAK_FRUIT_CHIME.get(), SoundSource.BLOCKS, 0.55F, 0.85F + random.nextFloat() * 0.3F);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 2) return InteractionResult.PASS;
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(ModBlocks.VAKVAK_FRUIT_ITEM.get()));
            level.setBlock(pos, state.setValue(AGE, 0), 2);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

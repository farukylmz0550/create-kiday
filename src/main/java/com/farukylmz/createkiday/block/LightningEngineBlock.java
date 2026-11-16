package com.farukylmz.createkiday.block;

import com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity;
import com.farukylmz.createkiday.registry.ModBlockEntities;
import com.farukylmz.createkiday.gameplay.LightningEngineEvents;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class LightningEngineBlock extends HorizontalKineticBlock implements EntityBlock {
    public LightningEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !oldState.is(this))
            LightningEngineEvents.registerEngine(serverLevel, pos);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightningEngineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.LIGHTNING_ENGINE.get() ? (world, pos, blockState, entity) -> ((LightningEngineBlockEntity) entity).tick() : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // RPM is configured by the Create-style ScrollValueBehaviour rendered
        // on the engine face, matching the Rotation Speed Controller UI.
        return InteractionResult.PASS;
    }
}

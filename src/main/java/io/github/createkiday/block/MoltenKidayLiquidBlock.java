package io.github.createkiday.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/** A placeable molten-metal block that burns entities like lava. */
public final class MoltenKidayLiquidBlock extends LiquidBlock {
    public MoltenKidayLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.lavaHurt();
        super.entityInside(state, level, pos, entity);
    }
}

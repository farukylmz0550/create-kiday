package io.github.createkiday.fluid;

import io.github.createkiday.registry.ModBlocks;
import io.github.createkiday.registry.ModFluids;
import io.github.createkiday.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.neoforged.neoforge.fluids.FluidType;

/** Lava-behaving Kiday metal with its own registry identity and textures. */
public abstract class MoltenKidayFluid extends LavaFluid {
    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_MOLTEN_KIDAY.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.MOLTEN_KIDAY.get();
    }

    @Override
    public Item getBucket() {
        return ModItems.MOLTEN_KIDAY_BUCKET.get();
    }

    @Override
    public FluidType getFluidType() {
        return ModFluids.MOLTEN_KIDAY_TYPE.get();
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.MOLTEN_KIDAY.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == ModFluids.MOLTEN_KIDAY.get() || fluid == ModFluids.FLOWING_MOLTEN_KIDAY.get();
    }

    public static final class Flowing extends MoltenKidayFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static final class Source extends MoltenKidayFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}

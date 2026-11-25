package io.github.createkiday.mixin;

import io.github.createkiday.registry.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps vanilla farmland fully hydrated only inside the Storm Grove biome. */
@Mixin(FarmBlock.class)
public abstract class StormGroveFarmlandMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void createKiday$hydratePlacedFarmland(BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> callback) {
        BlockState placed = callback.getReturnValue();
        if (placed != null && context.getLevel().getBiome(context.getClickedPos()).is(ModBiomes.STORM_GROVE)) {
            callback.setReturnValue(placed.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE));
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void createKiday$keepStormGroveFarmlandMoist(BlockState state, ServerLevel level, BlockPos pos,
            RandomSource random, CallbackInfo callback) {
        if (!level.getBiome(pos).is(ModBiomes.STORM_GROVE)) return;

        if (state.getValue(FarmBlock.MOISTURE) != FarmBlock.MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
        }
        callback.cancel();
    }
}

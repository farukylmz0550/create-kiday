package io.github.createkiday.mixin;

import io.github.createkiday.registry.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public abstract class StormGroveHoeMixin {
    @Inject(method = "useOn", at = @At("TAIL"))
    private void createKiday$hydrateNewlyTilledSoil(UseOnContext context,
            CallbackInfoReturnable<InteractionResult> callback) {
        if (callback.getReturnValue() == null || !callback.getReturnValue().consumesAction()) return;
        var level = context.getLevel();
        if (level.isClientSide) return;
        BlockPos pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (state.is(Blocks.FARMLAND) && level.getBiome(pos).is(ModBiomes.STORM_GROVE)
                && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
        }
    }
}

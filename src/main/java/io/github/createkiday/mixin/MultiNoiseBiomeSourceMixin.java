package io.github.createkiday.mixin;

import io.github.createkiday.gameplay.StormGroveBiomeResolver;
import io.github.createkiday.gameplay.StormGroveRegionCoordinates;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("HEAD"), cancellable = true)
    private void createKiday$createWideStormGroves(int quartX, int quartY, int quartZ,
            Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> callback) {
        MultiNoiseBiomeSource source = (MultiNoiseBiomeSource) (Object) this;
        if (!source.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) return;

        // Biome source coordinates are quart coordinates (4 blocks). Dividing by four
        // gives chunk coordinates, so all region constants must be converted to chunks too.
        int chunkX = Math.floorDiv(quartX, 4);
        int chunkZ = Math.floorDiv(quartZ, 4);
        if (!StormGroveRegionCoordinates.isGroveChunk(chunkX)
                || !StormGroveRegionCoordinates.isGroveChunk(chunkZ)) return;

        Holder<Biome> stormGrove = StormGroveBiomeResolver.get();
        if (stormGrove != null) callback.setReturnValue(stormGrove);
    }
}

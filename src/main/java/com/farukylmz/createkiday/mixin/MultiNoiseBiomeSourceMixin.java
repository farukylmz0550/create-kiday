package com.farukylmz.createkiday.mixin;

import com.farukylmz.createkiday.registry.ModBiomes;
import com.farukylmz.createkiday.gameplay.StormGroveBiomeResolver;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
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

        int blockX = QuartPos.toBlock(quartX);
        int blockY = QuartPos.toBlock(quartY);
        int blockZ = QuartPos.toBlock(quartZ);
        if (blockY < 0) return;

        int cellX = Math.floorDiv(blockX, ModBiomes.STORM_GROVE_REGION_SIZE);
        int cellZ = Math.floorDiv(blockZ, ModBiomes.STORM_GROVE_REGION_SIZE);
        int localX = Math.floorMod(blockX, ModBiomes.STORM_GROVE_REGION_SIZE);
        int localZ = Math.floorMod(blockZ, ModBiomes.STORM_GROVE_REGION_SIZE);
        int margin = (ModBiomes.STORM_GROVE_REGION_SIZE - ModBiomes.STORM_GROVE_WIDTH) / 2;
        if (localX < margin || localX >= margin + ModBiomes.STORM_GROVE_WIDTH
                || localZ < margin || localZ >= margin + ModBiomes.STORM_GROVE_WIDTH) return;

        int centerQuartX = QuartPos.fromBlock(cellX * ModBiomes.STORM_GROVE_REGION_SIZE
                + ModBiomes.STORM_GROVE_REGION_SIZE / 2);
        int centerQuartZ = QuartPos.fromBlock(cellZ * ModBiomes.STORM_GROVE_REGION_SIZE
                + ModBiomes.STORM_GROVE_REGION_SIZE / 2);
        Climate.TargetPoint climate = sampler.sample(centerQuartX, QuartPos.fromBlock(64), centerQuartZ);
        float continentalness = Climate.unquantizeCoord(climate.continentalness());
        float erosion = Climate.unquantizeCoord(climate.erosion());
        if (continentalness < -0.1F || erosion < 0.2F) return;

        long hash = cellX * 341873128712L + cellZ * 132897987541L
                + climate.temperature() * 31L + climate.humidity() * 17L;
        if (Math.floorMod(hash, 6L) != 0L) return;

        Holder<Biome> stormGrove = StormGroveBiomeResolver.get();
        if (stormGrove != null) callback.setReturnValue(stormGrove);
    }
}

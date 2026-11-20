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

        int localX = Math.floorMod(blockX, ModBiomes.STORM_GROVE_REGION_SIZE);
        int localZ = Math.floorMod(blockZ, ModBiomes.STORM_GROVE_REGION_SIZE);
        int margin = (ModBiomes.STORM_GROVE_REGION_SIZE - ModBiomes.STORM_GROVE_WIDTH) / 2;
        if (localX < margin || localX >= margin + ModBiomes.STORM_GROVE_WIDTH
                || localZ < margin || localZ >= margin + ModBiomes.STORM_GROVE_WIDTH) return;

        // Evaluate at a fixed climate height so biome selection does not flicker between
        // underground and surface quart samples. The repeating footprint is independent of
        // neighboring biome identities, so Storm Groves are not tied to mangrove swamps.
        Climate.TargetPoint climate = sampler.sample(quartX, QuartPos.fromBlock(64), quartZ);
        float continentalness = Climate.unquantizeCoord(climate.continentalness());
        float erosion = Climate.unquantizeCoord(climate.erosion());
        float weirdness = Climate.unquantizeCoord(climate.weirdness());
        // Permit ordinary inland/coastal land broadly, while avoiding oceans and steep
        // mountain climate bands to retain the grove's lowland character.
        if (continentalness < -0.05F || erosion < 0.25F || Math.abs(weirdness) > 0.65F) return;

        Holder<Biome> stormGrove = StormGroveBiomeResolver.get();
        if (stormGrove != null) callback.setReturnValue(stormGrove);
    }
}

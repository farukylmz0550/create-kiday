package com.farukylmz.createkiday.mixin;

import com.farukylmz.createkiday.gameplay.StormGroveBiomeResolver;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin {
    @Inject(method = "possibleBiomes", at = @At("RETURN"), cancellable = true)
    private void createKiday$includeStormGroveInPossibleBiomes(
            CallbackInfoReturnable<Set<Holder<Biome>>> callback) {
        if (!((Object) this instanceof MultiNoiseBiomeSource source)
                || !source.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) return;

        Holder<Biome> stormGrove = StormGroveBiomeResolver.get();
        if (stormGrove == null || callback.getReturnValue().contains(stormGrove)) return;

        Set<Holder<Biome>> possibleBiomes = new HashSet<>(callback.getReturnValue());
        possibleBiomes.add(stormGrove);
        callback.setReturnValue(Set.copyOf(possibleBiomes));
    }
}

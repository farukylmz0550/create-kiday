package io.github.createkiday.mixin;

import io.github.createkiday.registry.ModBiomes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class StormGroveZombieSunMixin {
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void createKiday$protectUndeadFromStormGroveSun(CallbackInfoReturnable<Boolean> callback) {
        Mob mob = (Mob) (Object) this;
        if ((mob instanceof Zombie || mob instanceof AbstractSkeleton)
                && mob.level().getBiome(mob.blockPosition()).is(ModBiomes.STORM_GROVE)) {
            callback.setReturnValue(false);
        }
    }
}

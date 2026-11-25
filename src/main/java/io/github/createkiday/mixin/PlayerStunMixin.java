package io.github.createkiday.mixin;

import io.github.createkiday.registry.ModMobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerStunMixin {
    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 createKiday$stopStunnedMovement(Vec3 input) {
        Player player = (Player) (Object) this;
        if (!player.hasEffect(ModMobEffects.STUN)) return input;
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(0, velocity.y, 0);
        player.setSprinting(false);
        return Vec3.ZERO;
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void createKiday$preventStunnedJump(CallbackInfo callback) {
        if (((Player) (Object) this).hasEffect(ModMobEffects.STUN)) callback.cancel();
    }
}

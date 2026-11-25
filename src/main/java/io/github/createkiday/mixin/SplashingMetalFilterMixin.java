package io.github.createkiday.mixin;

import io.github.createkiday.item.HotKidayItem;
import io.github.createkiday.registry.ModDataComponents;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Create's processing ingredients identify the shared hot item by item id only. */
@Mixin(SplashingRecipe.class)
public abstract class SplashingMetalFilterMixin {
    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("RETURN"), cancellable = true)
    private void createKiday$requireMatchingCastingMetal(SingleRecipeInput input, Level level,
            CallbackInfoReturnable<Boolean> callback) {
        filterHotMetal((SplashingRecipe) (Object) this, input, level, callback);
    }

    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("RETURN"), cancellable = true)
    private void createKiday$requireMatchingCastingMetalInput(RecipeInput input, Level level,
            CallbackInfoReturnable<Boolean> callback) {
        if (input instanceof SingleRecipeInput single)
            filterHotMetal((SplashingRecipe) (Object) this, single, level, callback);
    }

    private static void filterHotMetal(SplashingRecipe recipe, SingleRecipeInput single, Level level,
            CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() || !(single.item().getItem() instanceof HotKidayItem)) return;

        ItemStack hot = single.item();
        String metal = hot.get(ModDataComponents.METAL_TYPE.get());
        String expectedMetal = expectedMetal(recipe, level);
        if (expectedMetal == null || !expectedMetal.equals(metal)) callback.setReturnValue(false);
    }

    private static String expectedMetal(SplashingRecipe recipe, Level level) {
        ItemStack result = ((ProcessingRecipe<?, ?>) recipe).getResultItem(level.registryAccess());
        if (result.isEmpty()) return null;
        String path = BuiltInRegistries.ITEM.getKey(result.getItem()).getPath();
        if (path.startsWith("salur_")) return "salur";
        if (path.startsWith("kiday_")) return "kiday";
        return null;
    }
}

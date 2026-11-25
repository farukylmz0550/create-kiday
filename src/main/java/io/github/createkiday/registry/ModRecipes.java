package io.github.createkiday.registry;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.recipe.CastingRecipe;
import io.github.createkiday.recipe.MoldShapingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateKidayMod.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CastingRecipe>> CASTING_SERIALIZER = SERIALIZERS.register("casting", CastingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MoldShapingRecipe>> MOLD_SHAPING_SERIALIZER = SERIALIZERS.register("mold_shaping", MoldShapingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CastingRecipe>> CASTING_TYPE = TYPES.register("casting", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "casting")));
    public static final DeferredHolder<RecipeType<?>, RecipeType<MoldShapingRecipe>> MOLD_SHAPING_TYPE = TYPES.register("mold_shaping", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "mold_shaping")));

    private ModRecipes() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
}

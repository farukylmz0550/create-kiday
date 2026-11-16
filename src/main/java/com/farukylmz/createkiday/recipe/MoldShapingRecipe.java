package com.farukylmz.createkiday.recipe;

import com.farukylmz.createkiday.registry.ModRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record MoldShapingRecipe(Ingredient sample, Item blankMold, Item result, boolean consumeSample) implements Recipe<SingleRecipeInput> {
    @Override public boolean matches(SingleRecipeInput input, Level level) { return sample.test(input.item()); }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return new ItemStack(result); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return new ItemStack(result); }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.MOLD_SHAPING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.MOLD_SHAPING_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<MoldShapingRecipe> {
        private static final MapCodec<MoldShapingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("sample").forGetter(MoldShapingRecipe::sample),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("blank_mold").forGetter(MoldShapingRecipe::blankMold),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(MoldShapingRecipe::result),
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("consume_sample", true).forGetter(MoldShapingRecipe::consumeSample)
        ).apply(instance, MoldShapingRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, MoldShapingRecipe> STREAM = StreamCodec.of(
                (buf, value) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, value.sample);
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(value.blankMold));
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(value.result));
                    buf.writeBoolean(value.consumeSample);
                },
                buf -> new MoldShapingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), BuiltInRegistries.ITEM.get(buf.readResourceLocation()), BuiltInRegistries.ITEM.get(buf.readResourceLocation()), buf.readBoolean()));
        @Override public MapCodec<MoldShapingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, MoldShapingRecipe> streamCodec() { return STREAM; }
    }
}

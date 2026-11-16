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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public record CastingRecipe(Item mold, Fluid fluid, int amount, Item result, int metalUnits, int processingTime) implements Recipe<SingleRecipeInput> {
    @Override public boolean matches(SingleRecipeInput input, Level level) { return input.item().is(mold); }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return new ItemStack(result); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return new ItemStack(result); }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.CASTING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.CASTING_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<CastingRecipe> {
        private static final MapCodec<CastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("mold").forGetter(CastingRecipe::mold),
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(CastingRecipe::fluid),
                com.mojang.serialization.Codec.INT.fieldOf("amount").forGetter(CastingRecipe::amount),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(CastingRecipe::result),
                com.mojang.serialization.Codec.INT.fieldOf("metal_units").forGetter(CastingRecipe::metalUnits),
                com.mojang.serialization.Codec.INT.fieldOf("processing_time").forGetter(CastingRecipe::processingTime)
        ).apply(instance, CastingRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM = StreamCodec.of(
                (buf, value) -> {
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(value.mold));
                    buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(value.fluid));
                    buf.writeVarInt(value.amount);
                    buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(value.result));
                    buf.writeVarInt(value.metalUnits);
                    buf.writeVarInt(value.processingTime);
                },
                buf -> new CastingRecipe(BuiltInRegistries.ITEM.get(buf.readResourceLocation()), BuiltInRegistries.FLUID.get(buf.readResourceLocation()), buf.readVarInt(), BuiltInRegistries.ITEM.get(buf.readResourceLocation()), buf.readVarInt(), buf.readVarInt()));

        @Override public MapCodec<CastingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> streamCodec() { return STREAM; }
    }
}

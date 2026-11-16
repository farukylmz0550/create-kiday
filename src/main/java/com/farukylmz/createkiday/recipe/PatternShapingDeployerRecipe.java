package com.farukylmz.createkiday.recipe;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.item.KidayMoldItem;
import com.farukylmz.createkiday.registry.ModItems;
import com.simibubi.create.AllRecipeTypes;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/** Runtime Create Deployer recipe: Blank Pattern + sample -> matching reusable Pattern. */
public final class PatternShapingDeployerRecipe implements Recipe<RecipeWrapper> {
    private final Item result;
    private final Item sample;

    public PatternShapingDeployerRecipe(Item sample, Item result) {
        this.sample = sample;
        this.result = result;
    }

    public static PatternShapingDeployerRecipe forSample(ItemStack stack) {
        List<Item> samples = List.of(
                ModItems.KIDAY_INGOT.get(), ModItems.SWORD_HEAD.get(), ModItems.PICKAXE_HEAD.get(),
                ModItems.AXE_HEAD.get(), ModItems.SHOVEL_HEAD.get(), ModItems.HOE_HEAD.get(),
                ModItems.KIDAY_HELMET.get(), ModItems.KIDAY_CHESTPLATE.get(), ModItems.KIDAY_LEGGINGS.get(),
                ModItems.KIDAY_BOOTS.get(), com.farukylmz.createkiday.registry.ModBlocks.KIDAY_TRAPDOOR_ITEM.get(), Items.ENDER_PEARL);
        for (int i = 0; i < samples.size(); i++) {
            if (stack.is(samples.get(i))) return new PatternShapingDeployerRecipe(samples.get(i), ModItems.SHAPED_MOLDS.get(i).get());
        }
        return null;
    }

    public static ResourceLocation id(Item result) {
        return ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID,
                "pattern_deployer_" + BuiltInRegistries.ITEM.getKey(result).getPath());
    }

    public Item result() {
        return result;
    }

    @Override public boolean matches(RecipeWrapper input, Level level) {
        return input.size() >= 2 && input.getItem(0).is(ModItems.BLANK_MOLD.get()) && input.getItem(1).is(sample);
    }
    @Override public ItemStack assemble(RecipeWrapper input, HolderLookup.Provider registries) { return new ItemStack(result); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 2; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return new ItemStack(result); }
    @Override public RecipeSerializer<?> getSerializer() { return AllRecipeTypes.DEPLOYING.getSerializer(); }
    @Override public RecipeType<?> getType() { return AllRecipeTypes.DEPLOYING.getType(); }
    @Override public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.BLANK_MOLD.get()), Ingredient.of(sample));
    }
}

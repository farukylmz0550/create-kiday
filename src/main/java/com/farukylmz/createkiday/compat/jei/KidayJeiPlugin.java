package com.farukylmz.createkiday.compat.jei;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.recipe.CastingRecipe;
import com.farukylmz.createkiday.recipe.MoldShapingRecipe;
import com.farukylmz.createkiday.registry.ModBlocks;
import com.farukylmz.createkiday.registry.ModItems;
import com.farukylmz.createkiday.registry.ModRecipes;
import com.farukylmz.createkiday.KidayBalance;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import com.mojang.logging.LogUtils;

@JeiPlugin
public final class KidayJeiPlugin implements IModPlugin {
    private static final RecipeType<CastingRecipe> CASTING = RecipeType.create(CreateKidayMod.MOD_ID, "casting", CastingRecipe.class);
    private static final RecipeType<MoldShapingRecipe> MOLD_SHAPING = RecipeType.create(CreateKidayMod.MOD_ID, "mold_shaping", MoldShapingRecipe.class);
    private static volatile int registeredCastingRecipes;
    private static volatile int registeredPatternRecipes;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CastingCategory(gui), new MoldShapingCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CastingRecipe> casting = Minecraft.getInstance().level == null
                ? builtInCastingRecipes()
                : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipes.CASTING_TYPE.get()).stream().map(holder -> holder.value()).toList();
        List<MoldShapingRecipe> patterns = Minecraft.getInstance().level == null
                ? builtInPatternRecipes()
                : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipes.MOLD_SHAPING_TYPE.get()).stream().map(holder -> holder.value()).toList();
        registration.addRecipes(CASTING, casting);
        registration.addRecipes(MOLD_SHAPING, patterns);
        registration.addItemStackInfo(new ItemStack(ModItems.MOLTEN_KIDAY_BUCKET.get()), Component.translatable("jei.create_kiday.molten_bucket"));
        registeredCastingRecipes = casting.size();
        registeredPatternRecipes = patterns.size();
        LogUtils.getLogger().info("KIDAY_JEI_RECIPES_PASS casting={} patterns={}", casting.size(), patterns.size());
    }

    public static boolean allRecipesRegistered() {
        return registeredCastingRecipes == ModItems.SHAPED_MOLDS.size()
                && registeredPatternRecipes == ModItems.SHAPED_MOLDS.size();
    }

    private static List<CastingRecipe> builtInCastingRecipes() {
        return java.util.stream.IntStream.range(0, ModItems.SHAPED_MOLDS.size()).mapToObj(index -> {
            var pattern = ModItems.SHAPED_MOLDS.get(index).get();
            int units = pattern.metalUnits();
            return new CastingRecipe(pattern, com.farukylmz.createkiday.registry.ModFluids.MOLTEN_KIDAY.get(),
                    KidayBalance.requiredFluid(units), ModItems.HOT_PRODUCTS.get(index).get(), units, units * 20);
        }).toList();
    }

    private static List<MoldShapingRecipe> builtInPatternRecipes() {
        List<net.minecraft.world.item.Item> samples = List.of(
                ModItems.KIDAY_INGOT.get(), ModItems.SWORD_HEAD.get(), ModItems.PICKAXE_HEAD.get(),
                ModItems.AXE_HEAD.get(), ModItems.SHOVEL_HEAD.get(), ModItems.HOE_HEAD.get(),
                ModItems.KIDAY_HELMET.get(), ModItems.KIDAY_CHESTPLATE.get(), ModItems.KIDAY_LEGGINGS.get(),
                ModItems.KIDAY_BOOTS.get(), com.farukylmz.createkiday.registry.ModBlocks.KIDAY_TRAPDOOR_ITEM.get(), Items.ENDER_PEARL);
        return java.util.stream.IntStream.range(0, ModItems.SHAPED_MOLDS.size()).mapToObj(index ->
                new MoldShapingRecipe(Ingredient.of(samples.get(index)), ModItems.BLANK_MOLD.get(),
                        ModItems.SHAPED_MOLDS.get(index).get(), true)).toList();
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.CASTING_TABLE.get(), CASTING);
        registration.addRecipeCatalyst(ModItems.BLANK_MOLD.get(), MOLD_SHAPING);
    }

    private static final class CastingCategory implements IRecipeCategory<CastingRecipe> {
        private final IDrawable icon;
        private CastingCategory(IGuiHelper gui) { icon = gui.createDrawableItemLike(ModBlocks.CASTING_TABLE.get()); }
        @Override public RecipeType<CastingRecipe> getRecipeType() { return CASTING; }
        @Override public Component getTitle() { return Component.translatable("jei.create_kiday.casting"); }
        @Override public IDrawable getIcon() { return icon; }
        @Override public int getWidth() { return 112; }
        @Override public int getHeight() { return 38; }
        @Override public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {
            builder.addInputSlot(2, 10).setStandardSlotBackground().addItemStack(new ItemStack(recipe.mold()));
            builder.addInputSlot(28, 10).setStandardSlotBackground().setFluidRenderer(1_152, false, 16, 16).addFluidStack(recipe.fluid(), recipe.amount());
            builder.addOutputSlot(88, 10).setOutputSlotBackground().addItemStack(new ItemStack(recipe.result()));
        }
    }

    private static final class MoldShapingCategory implements IRecipeCategory<MoldShapingRecipe> {
        private final IDrawable icon;
        private MoldShapingCategory(IGuiHelper gui) { icon = gui.createDrawableItemLike(ModItems.BLANK_MOLD.get()); }
        @Override public RecipeType<MoldShapingRecipe> getRecipeType() { return MOLD_SHAPING; }
        @Override public Component getTitle() { return Component.translatable("jei.create_kiday.mold_shaping"); }
        @Override public IDrawable getIcon() { return icon; }
        @Override public int getWidth() { return 112; }
        @Override public int getHeight() { return 38; }
        @Override public void setRecipe(IRecipeLayoutBuilder builder, MoldShapingRecipe recipe, IFocusGroup focuses) {
            builder.addInputSlot(2, 10).setStandardSlotBackground().addItemStack(new ItemStack(recipe.blankMold()));
            builder.addInputSlot(28, 10).setStandardSlotBackground().addIngredients(recipe.sample());
            builder.addOutputSlot(88, 10).setOutputSlotBackground().addItemStack(new ItemStack(recipe.result()));
        }
    }
}

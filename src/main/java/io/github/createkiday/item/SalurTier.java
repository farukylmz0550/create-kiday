package io.github.createkiday.item;

import io.github.createkiday.SalurBalance;
import io.github.createkiday.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum SalurTier implements Tier {
    INSTANCE;
    @Override public int getUses() { return SalurBalance.TOOL_DURABILITY; }
    @Override public float getSpeed() { return 6.0F; }
    @Override public float getAttackDamageBonus() { return 2.5F; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_IRON_TOOL; }
    @Override public int getEnchantmentValue() { return SalurBalance.ENCHANTABILITY; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.SALUR_INGOT.get()); }
}

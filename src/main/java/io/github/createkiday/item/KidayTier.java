package io.github.createkiday.item;

import io.github.createkiday.KidayBalance;
import io.github.createkiday.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum KidayTier implements Tier {
    INSTANCE;

    @Override public int getUses() { return KidayBalance.TOOL_DURABILITY; }
    @Override public float getSpeed() { return 8.5F; }
    @Override public float getAttackDamageBonus() { return 3.5F; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    @Override public int getEnchantmentValue() { return 13; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.KIDAY_INGOT.get()); }
}

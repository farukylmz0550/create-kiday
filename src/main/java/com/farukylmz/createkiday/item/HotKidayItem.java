package com.farukylmz.createkiday.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** An automation-only intermediate. It intentionally never enters a creative tab. */
public final class HotKidayItem extends Item {
    public HotKidayItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.create_kiday.hot_product").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.create_kiday.bulk_wash").withStyle(ChatFormatting.GRAY));
    }
}

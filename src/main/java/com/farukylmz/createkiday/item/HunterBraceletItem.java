package com.farukylmz.createkiday.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class HunterBraceletItem extends Item {
    private static final String BRACELET_ID = "bracelet_id";

    public HunterBraceletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static UUID getOrCreateId(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag();
        if (!tag.hasUUID(BRACELET_ID)) {
            tag.putUUID(BRACELET_ID, UUID.randomUUID());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return tag.getUUID(BRACELET_ID);
    }

    public static Optional<UUID> getId(ItemStack stack) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.hasUUID(BRACELET_ID) ? Optional.of(tag.getUUID(BRACELET_ID)) : Optional.empty();
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide()) getOrCreateId(stack);
        super.onCraftedBy(stack, level, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && getId(stack).isEmpty()) getOrCreateId(stack);
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.create_kiday.bracelet").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (flag.isAdvanced()) getId(stack).ifPresent(id -> tooltip.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY)));
    }
}

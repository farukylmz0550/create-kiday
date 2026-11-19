package com.farukylmz.createkiday.item;

import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** Create-compatible filter whose rules are item tags entered by the player. */
public final class TagFilterItem extends FilterItem {
    public static final String ACCEPT_TAGS = "AcceptTags";
    public static final String REJECT_TAGS = "RejectTags";

    public TagFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) com.farukylmz.createkiday.client.TagFilterScreen.open(stack, hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public List<Component> makeSummary(ItemStack stack) {
        var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return List.of(Component.translatable("item.create_kiday.tag_filter.summary",
                tag.getString(ACCEPT_TAGS).isBlank() ? "—" : tag.getString(ACCEPT_TAGS),
                tag.getString(REJECT_TAGS).isBlank() ? "—" : tag.getString(REJECT_TAGS)));
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,
            net.minecraft.world.entity.player.Inventory inventory, Player player) {
        return null; // The item opens its lightweight client editor directly.
    }

    @Override
    public net.minecraft.core.component.DataComponentType<?> getComponentType() {
        return net.minecraft.core.component.DataComponents.CUSTOM_DATA;
    }

    @Override
    public FilterItemStack makeStackWrapper(ItemStack stack) {
        return new TagFilterStack(stack);
    }

    @Override
    public ItemStack[] getFilterItems(ItemStack stack) {
        return new ItemStack[0];
    }

    private static final class TagFilterStack extends FilterItemStack {
        private TagFilterStack(ItemStack stack) { super(stack); }

        @Override
        public boolean test(Level level, ItemStack candidate, boolean respectNbt) {
            var data = item().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            List<String> accepts = parse(data.getString(ACCEPT_TAGS));
            List<String> rejects = parse(data.getString(REJECT_TAGS));
            for (String id : rejects) if (matches(candidate, id)) return false;
            if (accepts.isEmpty()) return true;
            for (String id : accepts) if (matches(candidate, id)) return true;
            return false;
        }

        private static boolean matches(ItemStack stack, String value) {
            ResourceLocation location = ResourceLocation.tryParse(value.startsWith("#") ? value.substring(1) : value);
            if (location == null) return false;
            return stack.is(TagKey.create(Registries.ITEM, location));
        }

        private static List<String> parse(String value) {
            List<String> result = new ArrayList<>();
            for (String part : value.split("[,\\n]")) {
                String cleaned = part.trim();
                if (cleaned.startsWith("#")) cleaned = cleaned.substring(1);
                if (ResourceLocation.tryParse(cleaned) != null && !result.contains(cleaned)) result.add(cleaned);
            }
            return result;
        }
    }
}

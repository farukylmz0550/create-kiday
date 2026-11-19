package com.farukylmz.createkiday.item;

import com.farukylmz.createkiday.registry.ModDataComponents;
import com.farukylmz.createkiday.registry.ModMenus;
import com.farukylmz.createkiday.menu.TagFilterMenu;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/** Create-native item filter evaluated against the candidate's live item tags. */
public final class TagFilterItem extends FilterItem {
    public TagFilterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static TagKey<net.minecraft.world.item.Item> tagKey(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return TagKey.create(Registries.ITEM, location == null
                ? ResourceLocation.fromNamespaceAndPath("create_kiday", "invalid") : location);
    }

    @Override
    public List<Component> makeSummary(ItemStack stack) {
        TagFilterData data = stack.getOrDefault(ModDataComponents.TAG_FILTER_RULES.get(), TagFilterData.EMPTY);
        return List.of(Component.translatable("item.create_kiday.tag_filter.summary", data.tags().size(),
                Component.translatable("item.create_kiday.tag_filter.match." + data.matchMode().name().toLowerCase()),
                Component.translatable("item.create_kiday.tag_filter.type." + data.filterMode().name().toLowerCase())));
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TagFilterMenu(ModMenus.TAG_FILTER.get(), id, inventory, player.getMainHandItem());
    }

    @Override
    public net.minecraft.core.component.DataComponentType<?> getComponentType() {
        return ModDataComponents.TAG_FILTER_RULES.get();
    }

    @Override
    public FilterItemStack makeStackWrapper(ItemStack stack) {
        return new TagFilterStack(stack);
    }

    @Override
    public ItemStack[] getFilterItems(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY)
                .nonEmptyStream().map(ItemStack::copy).toArray(ItemStack[]::new);
    }

    private static final class TagFilterStack extends FilterItemStack {
        private TagFilterStack(ItemStack stack) { super(stack); }

        @Override
        public boolean test(net.minecraft.world.level.Level level, ItemStack candidate, boolean respectNbt) {
            TagFilterData data = item().getOrDefault(ModDataComponents.TAG_FILTER_RULES.get(), TagFilterData.EMPTY);
            boolean selectedTagsMatch;
            if (data.tags().isEmpty()) {
                selectedTagsMatch = false;
            } else if (data.matchMode() == TagFilterData.MatchMode.ALL) {
                selectedTagsMatch = data.tags().stream().allMatch(id -> hasTag(candidate, id));
            } else {
                selectedTagsMatch = data.tags().stream().anyMatch(id -> hasTag(candidate, id));
            }

            // An empty whitelist matches nothing; an empty blacklist matches everything.
            return data.filterMode() == TagFilterData.FilterMode.WHITELIST
                    ? selectedTagsMatch : !selectedTagsMatch;
        }

        private static boolean hasTag(ItemStack stack, String id) {
            ResourceLocation parsed = ResourceLocation.tryParse(id);
            return parsed != null && stack.is(TagKey.create(Registries.ITEM, parsed));
        }
    }
}

package com.farukylmz.createkiday.menu;

import com.farukylmz.createkiday.item.TagFilterData;
import com.farukylmz.createkiday.item.TagFilterItem;
import com.farukylmz.createkiday.registry.ModDataComponents;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.filter.AbstractFilterMenu;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Uses Create's held-item menu and ghost inventory so examples are copied, never consumed. */
public final class TagFilterMenu extends AbstractFilterMenu {
    private TagFilterData rules = TagFilterData.EMPTY;

    public TagFilterMenu(net.minecraft.world.inventory.MenuType<?> type, int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(type, id, inventory, buffer);
    }

    public TagFilterMenu(net.minecraft.world.inventory.MenuType<?> type, int id, Inventory inventory, ItemStack filterStack) {
        super(type, id, inventory, filterStack);
    }

    @Override
    protected void initAndReadInventory(ItemStack filterStack) {
        super.initAndReadInventory(filterStack);
        ItemContainerContents examples = filterStack.getOrDefault(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY);
        ItemStack sample = examples.copyOne();
        if (!sample.isEmpty()) ghostInventory.setStackInSlot(0, sample);
        rules = filterStack.getOrDefault(ModDataComponents.TAG_FILTER_RULES.get(), TagFilterData.EMPTY);
    }

    @Override
    protected void saveData(ItemStack filterStack) {
        super.saveData(filterStack);
        filterStack.set(ModDataComponents.TAG_FILTER_RULES.get(), rules);
    }

    @Override protected int getPlayerInventoryXOffset() { return 51; }
    @Override protected int getPlayerInventoryYOffset() { return 107; }

    @Override
    protected void addFilterSlots() {
        addSlot(new SlotItemHandler(ghostInventory, 0, 16, 27));
    }

    @Override protected ItemStackHandler createGhostInventory() { return new ItemStackHandler(1); }

    @Override
    public void clearContents() {
        super.clearContents();
        rules = TagFilterData.EMPTY;
        saveData((ItemStack) contentHolder);
    }

    public TagFilterData rules() { return rules; }

    public ItemStack sample() {
        return ghostInventory.getStackInSlot(0);
    }

    public List<ItemStack> matchingPreview(int limit) {
        return player.level().registryAccess().lookupOrThrow(Registries.ITEM).listElements()
                .map(holder -> new ItemStack(holder.value()))
                .filter(stack -> !stack.isEmpty() && matches(stack, rules))
                .limit(limit).toList();
    }

    public void setRules(TagFilterData next) {
        rules = new TagFilterData(next.tags(), next.matchMode(), next.filterMode());
        saveData((ItemStack) contentHolder);
    }

    private boolean matches(ItemStack stack, TagFilterData data) {
        if (data.tags().isEmpty()) return data.filterMode() == TagFilterData.FilterMode.BLACKLIST;
        boolean matches = data.matchMode() == TagFilterData.MatchMode.ALL
                ? data.tags().stream().allMatch(id -> stack.is(TagFilterItem.tagKey(id)))
                : data.tags().stream().anyMatch(id -> stack.is(TagFilterItem.tagKey(id)));
        return data.filterMode() == TagFilterData.FilterMode.WHITELIST ? matches : !matches;
    }
}

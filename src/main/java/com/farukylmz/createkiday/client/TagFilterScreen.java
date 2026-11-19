package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.item.TagFilterData;
import com.farukylmz.createkiday.menu.TagFilterMenu;
import com.farukylmz.createkiday.network.SaveTagFilterPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Tag picker styled with Create's squared, dark panels and copper accents. */
public final class TagFilterScreen extends AbstractContainerScreen<TagFilterMenu> {
    private static final int ROWS = 4;
    private static final int ROW_HEIGHT = 12;
    private static final int LIST_X = 82;
    private static final int LIST_Y = 82;
    private static final int LIST_WIDTH = 155;
    private static final int SAMPLE_X = 14;
    private static final int SAMPLE_Y = 68;
    private static final int SAMPLE_WIDTH = 58;
    private static final int SAMPLE_ROWS = 5;

    private final List<String> allTags = new ArrayList<>();
    private List<String> visibleTags = List.of();
    private List<ItemStack> previewItems = List.of();
    private EditBox searchBox;
    private EditBox manualTagBox;
    private Button matchButton;
    private Button filterButton;
    private int tagScroll;
    private int sampleScroll;

    public TagFilterScreen(TagFilterMenu menu, net.minecraft.world.entity.player.Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 330;
        imageHeight = 228;
        inventoryLabelY = 1000;
        refreshTags();
        refreshPreview();
    }

    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(font, leftPos + 82, topPos + 29, 155, 18,
                Component.translatable("screen.create_kiday.tag_filter.search"));
        searchBox.setMaxLength(128);
        searchBox.setHint(Component.translatable("screen.create_kiday.tag_filter.search_hint"));
        searchBox.setResponder(ignored -> refreshVisibleTags());
        addRenderableWidget(searchBox);

        manualTagBox = new EditBox(font, leftPos + 82, topPos + 53, 120, 18,
                Component.translatable("screen.create_kiday.tag_filter.manual"));
        manualTagBox.setMaxLength(TagFilterData.MAX_TAG_ID_LENGTH);
        manualTagBox.setHint(Component.literal("namespace:tag"));
        addRenderableWidget(manualTagBox);

        addRenderableWidget(Button.builder(Component.translatable("screen.create_kiday.tag_filter.add"), button -> addManualTag())
                .bounds(leftPos + 205, topPos + 53, 32, 18).build());
        matchButton = Button.builder(matchLabel(), button -> toggleMatchMode())
                .bounds(leftPos + 245, topPos + 29, 74, 18).build();
        addRenderableWidget(matchButton);
        filterButton = Button.builder(filterLabel(), button -> toggleFilterMode())
                .bounds(leftPos + 245, topPos + 53, 74, 18).build();
        addRenderableWidget(filterButton);
        addRenderableWidget(Button.builder(Component.translatable("screen.create_kiday.tag_filter.reset"), button -> resetRules())
                .bounds(leftPos + 245, topPos + 77, 74, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(leftPos + imageWidth - 58, topPos + 201, 48, 18).build());
        setInitialFocus(searchBox);
    }

    private void refreshTags() {
        allTags.clear();
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            client.level.registryAccess().lookupOrThrow(Registries.ITEM).listTagIds()
                    .map(tag -> tag.location().toString())
                    .sorted(Comparator.naturalOrder())
                    .forEach(allTags::add);
        }
        refreshVisibleTags();
    }

    private void refreshVisibleTags() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(java.util.Locale.ROOT);
        List<String> choices = new ArrayList<>(allTags);
        for (String selected : menu.rules().tags()) if (!choices.contains(selected)) choices.add(selected);
        choices.sort(Comparator.naturalOrder());
        visibleTags = choices.stream().filter(id -> id.toLowerCase(java.util.Locale.ROOT).contains(query)).toList();
        tagScroll = Math.min(tagScroll, Math.max(0, visibleTags.size() - ROWS));
    }

    private void addManualTag() {
        String raw = manualTagBox.getValue().trim();
        ResourceLocation id = ResourceLocation.tryParse(raw.startsWith("#") ? raw.substring(1) : raw);
        if (id == null || menu.rules().tags().contains(id.toString()) || menu.rules().tags().size() >= TagFilterData.MAX_TAGS) return;
        applyRules(new TagFilterData(append(menu.rules().tags(), id.toString()), menu.rules().matchMode(), menu.rules().filterMode()));
        refreshVisibleTags();
        manualTagBox.setValue("");
        refreshPreview();
    }

    private List<String> append(List<String> values, String next) {
        List<String> updated = new ArrayList<>(values);
        updated.add(next);
        return updated;
    }

    private void toggleTag(String id) {
        List<String> selected = new ArrayList<>(menu.rules().tags());
        if (!selected.remove(id)) {
            if (selected.size() >= TagFilterData.MAX_TAGS) return;
            selected.add(id);
        }
        applyRules(new TagFilterData(selected, menu.rules().matchMode(), menu.rules().filterMode()));
        refreshVisibleTags();
        refreshPreview();
    }

    private void toggleMatchMode() {
        TagFilterData.MatchMode next = menu.rules().matchMode() == TagFilterData.MatchMode.ANY
                ? TagFilterData.MatchMode.ALL : TagFilterData.MatchMode.ANY;
        applyRules(new TagFilterData(menu.rules().tags(), next, menu.rules().filterMode()));
        refreshPreview();
        rebuildModeButtons();
    }

    private void toggleFilterMode() {
        TagFilterData.FilterMode next = menu.rules().filterMode() == TagFilterData.FilterMode.WHITELIST
                ? TagFilterData.FilterMode.BLACKLIST : TagFilterData.FilterMode.WHITELIST;
        applyRules(new TagFilterData(menu.rules().tags(), menu.rules().matchMode(), next));
        refreshPreview();
        rebuildModeButtons();
    }

    private void resetRules() {
        menu.clearContents();
        menu.setRules(TagFilterData.EMPTY);
        PacketDistributor.sendToServer(new SaveTagFilterPayload(menu.containerId, TagFilterData.EMPTY, true));
        tagScroll = 0;
        sampleScroll = 0;
        refreshPreview();
        rebuildModeButtons();
    }

    private void applyRules(TagFilterData data) {
        menu.setRules(data);
        PacketDistributor.sendToServer(new SaveTagFilterPayload(menu.containerId, data, false));
    }

    private Component matchLabel() {
        return Component.translatable("screen.create_kiday.tag_filter.match_mode",
                Component.translatable("item.create_kiday.tag_filter.match." + menu.rules().matchMode().name().toLowerCase()));
    }

    private Component filterLabel() {
        return Component.translatable("screen.create_kiday.tag_filter.filter_mode",
                Component.translatable("item.create_kiday.tag_filter.type." + menu.rules().filterMode().name().toLowerCase()));
    }

    private void rebuildModeButtons() {
        if (matchButton != null) matchButton.setMessage(matchLabel());
        if (filterButton != null) filterButton.setMessage(filterLabel());
    }

    private void refreshPreview() {
        previewItems = menu.matchingPreview(4);
    }

    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (Minecraft.getInstance().screen instanceof TagFilterScreen screen) {
            screen.refreshTags();
            screen.refreshPreview();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF171715);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + 24, 0xFF3B3832);
        graphics.fill(x + 2, y + 25, x + imageWidth - 2, y + 132, 0xFF24231F);
        graphics.fill(x + 2, y + 134, x + imageWidth - 2, y + imageHeight - 2, 0xFF302E29);
        graphics.fill(x + 77, y + 27, x + 240, y + 131, 0xFF191917);
        graphics.fill(x + 8, y + 62, x + 74, y + 131, 0xFF191917);
        graphics.fill(x + 241, y + 101, x + imageWidth - 8, y + 131, 0xFF191917);
        graphics.fill(x + 2, y + 24, x + imageWidth - 2, y + 25, 0xFFB66A38);
        AllGuiTextures.PLAYER_INVENTORY.render(graphics, x + 79, y + 138);
        graphics.drawString(font, title, x + 10, y + 9, 0xFFE1D5C1, false);
        graphics.drawString(font, Component.translatable("screen.create_kiday.tag_filter.example"), x + 10, y + 29, 0xFFD2A273, false);
        graphics.drawString(font, Component.translatable("screen.create_kiday.tag_filter.sample_tags"), x + 10, y + 51, 0xFFD2A273, false);
        graphics.drawString(font, Component.translatable("screen.create_kiday.tag_filter.tags"), x + 80, y + 69, 0xFFD2A273, false);
        graphics.drawString(font, Component.translatable("screen.create_kiday.tag_filter.preview"), x + 245, y + 91, 0xFFD2A273, false);

        ItemStack sample = menu.sample();
        if (sample.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.create_kiday.tag_filter.empty_sample"), x + 10, y + 120, 0xFFAAAAAA, false);
        } else {
            List<String> sampleTags = sample.getTags().map(tag -> tag.location().toString()).sorted().toList();
            int first = Math.min(sampleScroll, Math.max(0, sampleTags.size() - SAMPLE_ROWS));
            for (int row = 0; row < SAMPLE_ROWS && first + row < sampleTags.size(); row++) {
                drawClipped(graphics, "#" + sampleTags.get(first + row), x + SAMPLE_X, y + SAMPLE_Y + row * ROW_HEIGHT,
                        SAMPLE_WIDTH, 0xFFC8C8C8);
            }
        }

        int firstTag = Math.min(tagScroll, Math.max(0, visibleTags.size() - ROWS));
        for (int row = 0; row < ROWS && firstTag + row < visibleTags.size(); row++) {
            String id = visibleTags.get(firstTag + row);
            int rowY = y + LIST_Y + row * ROW_HEIGHT;
            boolean selected = menu.rules().tags().contains(id);
            if (selected) graphics.fill(x + LIST_X - 2, rowY - 1, x + LIST_X + LIST_WIDTH - 3, rowY + 10, 0xFF51412D);
            graphics.drawString(font, selected ? "◆" : "◇", x + LIST_X, rowY, selected ? 0xFFE8A754 : 0xFFAAAAAA, false);
            drawClipped(graphics, "#" + id, x + LIST_X + 12, rowY, LIST_WIDTH - 16,
                    selected ? 0xFFFFE6B9 : 0xFFD2D0CA);
        }

        int previewX = x + 248;
        for (int index = 0; index < previewItems.size(); index++) {
            int col = index % 2;
            int row = index / 2;
            graphics.renderItem(previewItems.get(index), previewX + col * 31, y + 106 + row * 18);
        }
    }

    private void drawClipped(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        graphics.enableScissor(x, y, x + width, y + font.lineHeight);
        graphics.drawString(font, text, x, y, color, false);
        graphics.disableScissor();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The custom Create-style panel draws labels in renderBg so all regions stay aligned.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (int) mouseX - leftPos;
            int y = (int) mouseY - topPos;
            if (x >= LIST_X && x <= LIST_X + LIST_WIDTH && y >= LIST_Y && y < LIST_Y + ROWS * ROW_HEIGHT) {
                int index = tagScroll + (y - LIST_Y) / ROW_HEIGHT;
                if (index < visibleTags.size()) toggleTag(visibleTags.get(index));
                return true;
            }
            if (x >= SAMPLE_X && x <= SAMPLE_X + SAMPLE_WIDTH && y >= SAMPLE_Y && y < SAMPLE_Y + SAMPLE_ROWS * ROW_HEIGHT) {
                List<String> tags = menu.sample().getTags().map(tag -> tag.location().toString()).sorted().toList();
                int index = sampleScroll + (y - SAMPLE_Y) / ROW_HEIGHT;
                if (index < tags.size()) {
                    String sampleTag = tags.get(index);
                    toggleTag(sampleTag);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int x = (int) mouseX - leftPos;
        int y = (int) mouseY - topPos;
        if (x >= LIST_X && x <= LIST_X + LIST_WIDTH && y >= LIST_Y && y < LIST_Y + ROWS * ROW_HEIGHT) {
            tagScroll = clampScroll(tagScroll - (int) Math.signum(deltaY), visibleTags.size(), ROWS);
            return true;
        }
        if (x >= SAMPLE_X && x <= SAMPLE_X + SAMPLE_WIDTH && y >= SAMPLE_Y && y < SAMPLE_Y + SAMPLE_ROWS * ROW_HEIGHT) {
            int sampleCount = (int) menu.sample().getTags().count();
            sampleScroll = clampScroll(sampleScroll - (int) Math.signum(deltaY), sampleCount, SAMPLE_ROWS);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private static int clampScroll(int value, int size, int visible) {
        return Math.max(0, Math.min(value, Math.max(0, size - visible)));
    }
}

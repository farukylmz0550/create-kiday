package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.item.KidayChronicleItem;
import java.util.List;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Read-only Chronicle that resolves every page from the client's active language. */
public final class KidayChronicleScreen extends Screen {
    private int firstPage;
    private PageButton backButton;
    private PageButton forwardButton;

    public KidayChronicleScreen() {
        super(GameNarrator.NO_TITLE);
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new KidayChronicleScreen());
    }

    @Override
    protected void init() {
        int left = (width - 384) / 2;
        backButton = addRenderableWidget(new PageButton(left + 43, 159, false, button -> turn(-2), true));
        forwardButton = addRenderableWidget(new PageButton(left + 308, 159, true, button -> turn(2), true));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 - 100, 196, 200, 20).build());
        updateButtons();
    }

    private void turn(int pages) {
        firstPage = Math.clamp(firstPage + pages, 0, KidayChronicleItem.PAGE_COUNT - 2);
        firstPage -= firstPage % 2;
        updateButtons();
    }

    private void updateButtons() {
        if (backButton != null) backButton.visible = firstPage > 0;
        if (forwardButton != null) forwardButton.visible = firstPage < KidayChronicleItem.PAGE_COUNT - 2;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 266 && firstPage > 0) {
            backButton.onPress();
            return true;
        }
        if (keyCode == 267 && firstPage < KidayChronicleItem.PAGE_COUNT - 2) {
            forwardButton.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        int left = (width - 384) / 2;
        graphics.blit(BookViewScreen.BOOK_LOCATION, left, 2, 0, 0, 192, 192);
        graphics.blit(BookViewScreen.BOOK_LOCATION, left + 192, 2, 0, 0, 192, 192);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = (width - 384) / 2;
        drawPage(graphics, left, firstPage);
        drawPage(graphics, left + 192, firstPage + 1);
    }

    private void drawPage(GuiGraphics graphics, int left, int page) {
        Component indicator = Component.translatable(
                "book.pageIndicator", page + 1, KidayChronicleItem.PAGE_COUNT);
        graphics.drawString(font, indicator, left + 148 - font.width(indicator), 18, 0, false);
        Component text = Component.translatable("book.create_kiday.chronicle.page_" + (page + 1));
        List<FormattedCharSequence> lines = font.split(text, 114);
        for (int line = 0; line < Math.min(14, lines.size()); line++) {
            graphics.drawString(font, lines.get(line), left + 36, 32 + line * 9, 0, false);
        }
    }
}

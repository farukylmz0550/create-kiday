package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.item.TagFilterItem;
import com.farukylmz.createkiday.network.SaveTagFilterPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;

/** Small editor for accepted and rejected item tag identifiers. */
public final class TagFilterScreen extends Screen {
    private final InteractionHand hand;
    private EditBox acceptBox;
    private EditBox rejectBox;

    private TagFilterScreen(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("screen.create_kiday.tag_filter"));
        this.hand = hand;
        var data = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        acceptValue = data.getString(TagFilterItem.ACCEPT_TAGS);
        rejectValue = data.getString(TagFilterItem.REJECT_TAGS);
    }
    private final String acceptValue;
    private final String rejectValue;

    public static void open(ItemStack stack, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new TagFilterScreen(stack, hand));
    }

    @Override protected void init() {
        int left = width / 2 - 120;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> save())
                .bounds(width / 2 - 50, height / 2 + 48, 100, 20).build());
        acceptBox = new EditBox(font, left, height / 2 - 26, 240, 20, Component.translatable("screen.create_kiday.accept"));
        acceptBox.setMaxLength(2048); acceptBox.setValue(acceptValue); addRenderableWidget(acceptBox);
        rejectBox = new EditBox(font, left, height / 2 + 12, 240, 20, Component.translatable("screen.create_kiday.reject"));
        rejectBox.setMaxLength(2048); rejectBox.setValue(rejectValue); addRenderableWidget(rejectBox);
        setInitialFocus(acceptBox);
    }

    private void save() {
        PacketDistributor.sendToServer(new SaveTagFilterPayload(hand, acceptBox.getValue(), rejectBox.getValue()));
        onClose();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = width / 2 - 120;
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 68, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.create_kiday.accept"), x, height / 2 - 39, 0xA0E0A0);
        graphics.drawString(font, Component.translatable("screen.create_kiday.reject"), x, height / 2 - 1, 0xF09090);
        graphics.drawCenteredString(font, Component.translatable("screen.create_kiday.hint"), width / 2, height / 2 + 39, 0xAAAAAA);
    }
}

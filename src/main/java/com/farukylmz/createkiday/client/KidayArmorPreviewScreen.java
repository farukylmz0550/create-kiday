package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Render-only acceptance view that cannot be closed by container-menu sync. */
public final class KidayArmorPreviewScreen extends Screen {
    public KidayArmorPreviewScreen() {
        super(Component.literal("Kiday Armor Runtime Preview"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Let Screen apply its menu background first; drawing before this call
        // would blur the armor evidence along with the world behind it.
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, width, height, 0xFF18121E);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFF2D7E5);
        graphics.drawCenteredString(font, "Vanilla armor UV on the live player model", width / 2, 34, 0xFFC7A8B8);
        if (minecraft != null && minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsAngle(
                    graphics, width / 2 - 130, 48, width / 2 + 130, height - 18,
                    Math.min(110, height / 4), 0.0625F, 0.0F, 0.08F, minecraft.player);
            renderItemPreview(graphics, ModBlocks.KIDAY_DOOR_ITEM.toStack(), width / 2 - 205, height / 2 - 26,
                    "Door item");
            renderItemPreview(graphics, ModBlocks.KIDAY_TRAPDOOR_ITEM.toStack(), width / 2 + 157, height / 2 - 26,
                    "Trapdoor item");
        }
    }

    private void renderItemPreview(GuiGraphics graphics, ItemStack stack, int x, int y, String label) {
        graphics.drawCenteredString(font, label, x + 24, y - 14, 0xFFC7A8B8);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(3.0F, 3.0F, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package com.farukylmz.createkiday.item;

import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import com.farukylmz.createkiday.network.OpenChroniclePayload;

public final class KidayChronicleItem extends WrittenBookItem {
    public static final int PAGE_COUNT = 8;
    public static final List<Component> PAGES = IntStream.rangeClosed(1, PAGE_COUNT)
            .mapToObj(page -> (Component) Component.translatable("book.create_kiday.chronicle.page_" + page))
            .toList();

    public KidayChronicleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createStack() {
        ItemStack stack = new ItemStack(com.farukylmz.createkiday.registry.ModItems.KIDAY_CHRONICLE.get());
        applyContent(stack);
        return stack;
    }

    private static void applyContent(ItemStack stack) {
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(""), "", 0,
                PAGES.stream().map(Filterable::passThrough).toList(), true));
    }

    @Override
    public Component getName(ItemStack stack) {
        // WrittenBookItem normally replaces the translated item name with its
        // raw title string. Keep this custom book responsive to client language.
        return Component.translatable("item.create_kiday.kiday_chronicle");
    }

    @Override
    public ItemStack getDefaultInstance() {
        return createStack();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // Migrate books from older releases that stored Turkish literal pages.
        applyContent(stack);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, OpenChroniclePayload.INSTANCE);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) tooltip.add(Component.translatable("tooltip.create_kiday.chronicle_author").withStyle(ChatFormatting.GRAY));
    }
}

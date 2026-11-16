package com.farukylmz.createkiday.item;

import com.farukylmz.createkiday.KidayBalance;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class KidayMoldItem extends Item {
    private final int metalUnits;

    public KidayMoldItem(int metalUnits) {
        super(new Properties().stacksTo(1).durability(KidayBalance.MOLD_USES));
        if (metalUnits < 1 || metalUnits > 8) throw new IllegalArgumentException("metalUnits");
        this.metalUnits = metalUnits;
    }

    public int metalUnits() {
        return metalUnits;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int remaining = KidayBalance.MOLD_USES - stack.getDamageValue();
        tooltip.add(Component.translatable("tooltip.create_kiday.mold_uses", remaining, KidayBalance.MOLD_USES).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.create_kiday.mold_fluid", KidayBalance.requiredFluid(metalUnits)).withStyle(ChatFormatting.GRAY));
    }
}

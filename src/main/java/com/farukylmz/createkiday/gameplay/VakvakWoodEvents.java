package com.farukylmz.createkiday.gameplay;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.registry.ModBlocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class VakvakWoodEvents {
    private VakvakWoodEvents() {}

    @SubscribeEvent
    public static void stripVakvakWood(BlockEvent.BlockToolModificationEvent event) {
        if (!event.getItemAbility().equals(ItemAbilities.AXE_STRIP)) return;
        if (event.getState().is(ModBlocks.VAKVAK_LOG.get())) {
            event.setFinalState(ModBlocks.STRIPPED_VAKVAK_LOG.get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, event.getState().getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)));
        } else if (event.getState().is(ModBlocks.VAKVAK_WOOD_BLOCK.get())) {
            event.setFinalState(ModBlocks.STRIPPED_VAKVAK_WOOD.get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, event.getState().getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)));
        }
    }
}

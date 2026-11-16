package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateKidayMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KIDAY = TABS.register("kiday", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.create_kiday"))
            .icon(() -> ModItems.KIDAY_INGOT.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModBlocks.KIDAY_ORE_ITEM.get());
                output.accept(ModBlocks.DEEPSLATE_KIDAY_ORE_ITEM.get());
                output.accept(ModBlocks.KIDAY_BLOCK_ITEM.get());
                output.accept(ModBlocks.CASTING_TABLE_ITEM.get());
                output.accept(ModBlocks.LIGHTNING_ENGINE_ITEM.get());
                output.accept(ModBlocks.KIDAY_DOOR_ITEM.get());
                output.accept(ModBlocks.KIDAY_TRAPDOOR_ITEM.get());
                ModItems.visibleIngredients().forEach(item -> output.accept(item.get()));
                ModItems.visibleEquipment().forEach(item -> output.accept(item.get() == ModItems.KIDAY_CHRONICLE.get()
                        ? ModItems.KIDAY_CHRONICLE.get().getDefaultInstance() : item.get().getDefaultInstance()));
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}

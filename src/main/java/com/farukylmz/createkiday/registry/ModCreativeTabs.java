package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateKidayMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KIDAY_MINERALS = TABS.register("kiday_minerals",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_kiday.minerals"))
                    .icon(() -> ModBlocks.KIDAY_ORE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.KIDAY_ORE_ITEM.get());
                        output.accept(ModBlocks.DEEPSLATE_KIDAY_ORE_ITEM.get());
                        output.accept(ModBlocks.KIDAY_BLOCK_ITEM.get());
                        output.accept(ModItems.RAW_KIDAY.get());
                        output.accept(ModItems.KIDAY_INGOT.get());
                        output.accept(ModItems.KIDAY_SHEET.get());
                        output.accept(ModItems.MOLTEN_KIDAY_BUCKET.get());
                        ModItems.HOT_PRODUCTS.forEach(item -> output.accept(item.get()));
                    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KIDAY_WOOD = TABS.register("kiday_wood",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_kiday.wood"))
                    .icon(() -> ModBlocks.WAQWAQ_LEAVES_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.WAQWAQ_LOG_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_WOOD_ITEM.get());
                        output.accept(ModBlocks.STRIPPED_WAQWAQ_LOG_ITEM.get());
                        output.accept(ModBlocks.STRIPPED_WAQWAQ_WOOD_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_PLANKS_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_STAIRS_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_SLAB_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_FENCE_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_FENCE_GATE_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_PRESSURE_PLATE_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_BUTTON_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_LEAVES_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_SAPLING_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_FRUIT_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_DOOR_ITEM.get());
                        output.accept(ModBlocks.WAQWAQ_TRAPDOOR_ITEM.get());
                        output.accept(ModItems.WAQWAQ_BOAT.get());
                        output.accept(ModItems.WAQWAQ_CHEST_BOAT.get());
                    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KIDAY_OTHER = TABS.register("kiday_other",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_kiday.other"))
                    .icon(() -> ModBlocks.CASTING_TABLE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.CASTING_TABLE_ITEM.get());
                        output.accept(ModBlocks.LIGHTNING_ENGINE_ITEM.get());
                        output.accept(ModBlocks.KIDAY_DOOR_ITEM.get());
                        output.accept(ModBlocks.KIDAY_TRAPDOOR_ITEM.get());
                        ModItems.visibleIngredients().stream()
                                .map(holder -> holder.get())
                                .filter(item -> !isMineral(item))
                                .forEach(output::accept);
                        ModItems.visibleEquipment().stream()
                                .map(holder -> holder.get())
                                .filter(item -> item != ModItems.WAQWAQ_BOAT.get() && item != ModItems.WAQWAQ_CHEST_BOAT.get())
                                .forEach(output::accept);
                    }).build());

    private ModCreativeTabs() {}

    private static boolean isMineral(Item item) {
        return item == ModItems.RAW_KIDAY.get()
                || item == ModItems.KIDAY_INGOT.get()
                || item == ModItems.KIDAY_SHEET.get()
                || item == ModItems.MOLTEN_KIDAY_BUCKET.get()
                || ModItems.HOT_PRODUCTS.stream().anyMatch(holder -> holder.get() == item);
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}

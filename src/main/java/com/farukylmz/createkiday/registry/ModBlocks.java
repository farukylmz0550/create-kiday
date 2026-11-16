package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.block.CastingTableBlock;
import com.farukylmz.createkiday.block.LightningEngineBlock;
import com.farukylmz.createkiday.block.MoltenKidayLiquidBlock;
import java.util.function.Supplier;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateKidayMod.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateKidayMod.MOD_ID);

    public static final DeferredBlock<Block> KIDAY_ORE = BLOCKS.register("kiday_ore", () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().strength(3.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> DEEPSLATE_KIDAY_ORE = BLOCKS.register("deepslate_kiday_ore", () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().strength(4.5F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> KIDAY_BLOCK = BLOCKS.registerSimpleBlock("kiday_block", BlockBehaviour.Properties.of().strength(5.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final DeferredBlock<CastingTableBlock> CASTING_TABLE = BLOCKS.register("casting_table", () -> new CastingTableBlock(BlockBehaviour.Properties.of().strength(3.5F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final DeferredBlock<LightningEngineBlock> LIGHTNING_ENGINE = BLOCKS.register("lightning_engine", () -> new LightningEngineBlock(BlockBehaviour.Properties.of().strength(4.0F).requiresCorrectToolForDrops().sound(SoundType.COPPER)));
    public static final DeferredBlock<DoorBlock> KIDAY_DOOR = BLOCKS.register("kiday_door", () -> new DoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.of().strength(5.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> KIDAY_TRAPDOOR = BLOCKS.register("kiday_trapdoor", () -> new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.of().strength(5.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<LiquidBlock> MOLTEN_KIDAY = BLOCKS.register("molten_kiday", () -> new MoltenKidayLiquidBlock(
            ModFluids.MOLTEN_KIDAY.get(),
            BlockBehaviour.Properties.of().replaceable().noCollission().strength(100.0F).lightLevel(state -> 15)
                    .randomTicks().noLootTable().liquid().sound(SoundType.EMPTY)));

    public static final DeferredItem<BlockItem> KIDAY_ORE_ITEM = blockItem("kiday_ore", KIDAY_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_KIDAY_ORE_ITEM = blockItem("deepslate_kiday_ore", DEEPSLATE_KIDAY_ORE);
    public static final DeferredItem<BlockItem> KIDAY_BLOCK_ITEM = blockItem("kiday_block", KIDAY_BLOCK);
    public static final DeferredItem<BlockItem> CASTING_TABLE_ITEM = blockItem("casting_table", CASTING_TABLE);
    public static final DeferredItem<BlockItem> LIGHTNING_ENGINE_ITEM = blockItem("lightning_engine", LIGHTNING_ENGINE);
    public static final DeferredItem<BlockItem> KIDAY_DOOR_ITEM = blockItem("kiday_door", KIDAY_DOOR);
    public static final DeferredItem<BlockItem> KIDAY_TRAPDOOR_ITEM = blockItem("kiday_trapdoor", KIDAY_TRAPDOOR);

    private ModBlocks() {
    }

    private static <T extends Block> DeferredItem<BlockItem> blockItem(String name, Supplier<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}

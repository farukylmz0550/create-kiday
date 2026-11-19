package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.block.CastingTableBlock;
import com.farukylmz.createkiday.block.LightningEngineBlock;
import com.farukylmz.createkiday.block.MoltenKidayLiquidBlock;
import com.farukylmz.createkiday.block.VakvakFruitBlock;
import com.farukylmz.createkiday.block.VakvakLeavesBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;
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

    private static final ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> VAKVAK_TREE = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "vakvak_tree"));
    private static final TreeGrower VAKVAK_GROWER = new TreeGrower("create_kiday:vakvak", Optional.empty(), Optional.of(VAKVAK_TREE), Optional.empty());
    private static final WoodType VAKVAK_WOOD = new WoodType("create_kiday:vakvak", BlockSetType.OAK);

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

    public static final DeferredBlock<RotatedPillarBlock> VAKVAK_LOG = BLOCKS.register("vakvak_log", () -> new RotatedPillarBlock(woodProperties().strength(2.0F)));
    public static final DeferredBlock<RotatedPillarBlock> VAKVAK_WOOD_BLOCK = BLOCKS.register("vakvak_wood", () -> new RotatedPillarBlock(woodProperties().strength(2.0F)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_VAKVAK_LOG = BLOCKS.register("stripped_vakvak_log", () -> new RotatedPillarBlock(woodProperties().strength(2.0F)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_VAKVAK_WOOD = BLOCKS.register("stripped_vakvak_wood", () -> new RotatedPillarBlock(woodProperties().strength(2.0F)));
    public static final DeferredBlock<Block> VAKVAK_PLANKS = BLOCKS.registerSimpleBlock("vakvak_planks", woodProperties().strength(2.0F));
    public static final DeferredBlock<StairBlock> VAKVAK_STAIRS = BLOCKS.register("vakvak_stairs", () -> new StairBlock(VAKVAK_PLANKS.get().defaultBlockState(), woodProperties().strength(2.0F)));
    public static final DeferredBlock<SlabBlock> VAKVAK_SLAB = BLOCKS.register("vakvak_slab", () -> new SlabBlock(woodProperties().strength(2.0F)));
    public static final DeferredBlock<FenceBlock> VAKVAK_FENCE = BLOCKS.register("vakvak_fence", () -> new FenceBlock(woodProperties().strength(2.0F)));
    public static final DeferredBlock<FenceGateBlock> VAKVAK_FENCE_GATE = BLOCKS.register("vakvak_fence_gate", () -> new FenceGateBlock(VAKVAK_WOOD, woodProperties().strength(2.0F)));
    public static final DeferredBlock<PressurePlateBlock> VAKVAK_PRESSURE_PLATE = BLOCKS.register("vakvak_pressure_plate", () -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().mapColor(VAKVAK_PLANKS.get().defaultMapColor()).instrument(NoteBlockInstrument.BASS).noCollission().strength(0.5F)));
    public static final DeferredBlock<ButtonBlock> VAKVAK_BUTTON = BLOCKS.register("vakvak_button", () -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().noCollission().strength(0.5F)));
    public static final DeferredBlock<LeavesBlock> VAKVAK_LEAVES = BLOCKS.register("vakvak_leaves", () -> new VakvakLeavesBlock(BlockBehaviour.Properties.of().mapColor(VAKVAK_PLANKS.get().defaultMapColor()).strength(0.2F).randomTicks().sound(SoundType.CHERRY_LEAVES).noOcclusion().isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
    public static final DeferredBlock<SaplingBlock> VAKVAK_SAPLING = BLOCKS.register("vakvak_sapling", () -> new SaplingBlock(VAKVAK_GROWER, BlockBehaviour.Properties.of().mapColor(VAKVAK_PLANKS.get().defaultMapColor()).noCollission().randomTicks().instabreak().sound(SoundType.CHERRY_SAPLING)));
    public static final DeferredBlock<VakvakFruitBlock> VAKVAK_FRUIT = BLOCKS.register("vakvak_fruit", () -> new VakvakFruitBlock(BlockBehaviour.Properties.of().mapColor(VAKVAK_PLANKS.get().defaultMapColor()).noCollission().randomTicks().instabreak().sound(SoundType.CAVE_VINES)));
    public static final DeferredBlock<DoorBlock> VAKVAK_DOOR = BLOCKS.register("vakvak_door", () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().mapColor(VAKVAK_PLANKS.get().defaultMapColor()).strength(3.0F).noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> VAKVAK_TRAPDOOR = BLOCKS.register("vakvak_trapdoor", () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().mapColor(VAKVAK_PLANKS.get().defaultMapColor()).strength(3.0F).noOcclusion()));

    public static final DeferredItem<BlockItem> KIDAY_ORE_ITEM = blockItem("kiday_ore", KIDAY_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_KIDAY_ORE_ITEM = blockItem("deepslate_kiday_ore", DEEPSLATE_KIDAY_ORE);
    public static final DeferredItem<BlockItem> KIDAY_BLOCK_ITEM = blockItem("kiday_block", KIDAY_BLOCK);
    public static final DeferredItem<BlockItem> CASTING_TABLE_ITEM = blockItem("casting_table", CASTING_TABLE);
    public static final DeferredItem<BlockItem> LIGHTNING_ENGINE_ITEM = blockItem("lightning_engine", LIGHTNING_ENGINE);
    public static final DeferredItem<BlockItem> KIDAY_DOOR_ITEM = blockItem("kiday_door", KIDAY_DOOR);
    public static final DeferredItem<BlockItem> KIDAY_TRAPDOOR_ITEM = blockItem("kiday_trapdoor", KIDAY_TRAPDOOR);
    public static final DeferredItem<BlockItem> VAKVAK_LOG_ITEM = blockItem("vakvak_log", VAKVAK_LOG);
    public static final DeferredItem<BlockItem> VAKVAK_WOOD_ITEM = blockItem("vakvak_wood", VAKVAK_WOOD_BLOCK);
    public static final DeferredItem<BlockItem> STRIPPED_VAKVAK_LOG_ITEM = blockItem("stripped_vakvak_log", STRIPPED_VAKVAK_LOG);
    public static final DeferredItem<BlockItem> STRIPPED_VAKVAK_WOOD_ITEM = blockItem("stripped_vakvak_wood", STRIPPED_VAKVAK_WOOD);
    public static final DeferredItem<BlockItem> VAKVAK_PLANKS_ITEM = blockItem("vakvak_planks", VAKVAK_PLANKS);
    public static final DeferredItem<BlockItem> VAKVAK_STAIRS_ITEM = blockItem("vakvak_stairs", VAKVAK_STAIRS);
    public static final DeferredItem<BlockItem> VAKVAK_SLAB_ITEM = blockItem("vakvak_slab", VAKVAK_SLAB);
    public static final DeferredItem<BlockItem> VAKVAK_FENCE_ITEM = blockItem("vakvak_fence", VAKVAK_FENCE);
    public static final DeferredItem<BlockItem> VAKVAK_FENCE_GATE_ITEM = blockItem("vakvak_fence_gate", VAKVAK_FENCE_GATE);
    public static final DeferredItem<BlockItem> VAKVAK_PRESSURE_PLATE_ITEM = blockItem("vakvak_pressure_plate", VAKVAK_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> VAKVAK_BUTTON_ITEM = blockItem("vakvak_button", VAKVAK_BUTTON);
    public static final DeferredItem<BlockItem> VAKVAK_LEAVES_ITEM = blockItem("vakvak_leaves", VAKVAK_LEAVES);
    public static final DeferredItem<BlockItem> VAKVAK_SAPLING_ITEM = blockItem("vakvak_sapling", VAKVAK_SAPLING);
    public static final DeferredItem<BlockItem> VAKVAK_FRUIT_ITEM = ITEMS.register("vakvak_fruit", () -> new BlockItem(VAKVAK_FRUIT.get(), new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible().effect(new net.minecraft.world.effect.MobEffectInstance(com.farukylmz.createkiday.registry.ModMobEffects.VAKVAK_BLESSING, 1200, 0, false, true, true), 1.0F).build())));
    public static final DeferredItem<BlockItem> VAKVAK_DOOR_ITEM = blockItem("vakvak_door", VAKVAK_DOOR);
    public static final DeferredItem<BlockItem> VAKVAK_TRAPDOOR_ITEM = blockItem("vakvak_trapdoor", VAKVAK_TRAPDOOR);

    private ModBlocks() {
    }

    private static <T extends Block> DeferredItem<BlockItem> blockItem(String name, Supplier<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static BlockBehaviour.Properties woodProperties() {
        return BlockBehaviour.Properties.of().mapColor(VAKVAK_PLANKS_COLOR()).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.CHERRY_WOOD);
    }

    private static net.minecraft.world.level.material.MapColor VAKVAK_PLANKS_COLOR() {
        return net.minecraft.world.level.material.MapColor.COLOR_BROWN;
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}

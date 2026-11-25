package io.github.createkiday.registry;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.block.entity.CastingTableBlockEntity;
import io.github.createkiday.block.entity.LightningEngineBlockEntity;
import io.github.createkiday.block.entity.BiomassGeneratorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastingTableBlockEntity>> CASTING_TABLE = TYPES.register("casting_table", () -> BlockEntityType.Builder.of(CastingTableBlockEntity::new, ModBlocks.CASTING_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightningEngineBlockEntity>> LIGHTNING_ENGINE = TYPES.register("lightning_engine", () -> BlockEntityType.Builder.of(LightningEngineBlockEntity::new, ModBlocks.LIGHTNING_ENGINE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BiomassGeneratorBlockEntity>> BIOMASS_GENERATOR = TYPES.register("biomass_generator", () -> BlockEntityType.Builder.of(BiomassGeneratorBlockEntity::new, ModBlocks.BIOMASS_GENERATOR.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(ModBlockEntities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CASTING_TABLE.get(), (table, side) -> table.outputHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CASTING_TABLE.get(), (table, side) -> table.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BIOMASS_GENERATOR.get(), (generator, side) -> generator.itemHandler());
    }
}

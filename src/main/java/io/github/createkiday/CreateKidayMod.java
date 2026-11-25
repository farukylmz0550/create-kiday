package io.github.createkiday;

import io.github.createkiday.registry.ModBlocks;
import io.github.createkiday.registry.ModBlockEntities;
import io.github.createkiday.registry.ModFluids;
import io.github.createkiday.registry.ModEquipment;
import io.github.createkiday.registry.ModEntities;
import io.github.createkiday.registry.ModRecipes;
import io.github.createkiday.registry.ModMobEffects;
import io.github.createkiday.registry.ModSounds;
import io.github.createkiday.registry.ModDataComponents;
import io.github.createkiday.registry.ModFeatures;
import io.github.createkiday.registry.ModCreativeTabs;
import io.github.createkiday.network.OpenChroniclePayload;
import io.github.createkiday.network.LightningStaffPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import io.github.createkiday.registry.ModItems;
import com.mojang.logging.LogUtils;
import io.github.createkiday.recipe.PatternShapingDeployerRecipe;
import io.github.createkiday.advancement.KidayAdvancements;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import io.github.createkiday.entity.LeiGongEntity;
import io.github.createkiday.entity.StormSpiritEntity;
import io.github.createkiday.registry.ModBiomes;
import io.github.createkiday.gameplay.StormGroveBiomeResolver;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;

@Mod(CreateKidayMod.MOD_ID)
public final class CreateKidayMod {
    public static final String MOD_ID = "create_kiday";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateKidayMod(IEventBus modBus) {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, KidayServerConfig.SPEC);
        ModFluids.register(modBus);
        ModBlocks.register(modBus);
        ModEquipment.register(modBus);
        ModDataComponents.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModEntities.register(modBus);
        ModRecipes.register(modBus);
        ModMobEffects.register(modBus);
        ModSounds.register(modBus);
        ModFeatures.register(modBus);
        ModCreativeTabs.register(modBus);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::registerEntityAttributes);
        modBus.addListener(this::registerSpawnPlacements);
        modBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::addPatternDeployerRecipe);
        NeoForge.EVENT_BUS.addListener(KidayAdvancements::onKidayOreDrops);
        NeoForge.EVENT_BUS.addListener(KidayAdvancements::checkCompletion);
        NeoForge.EVENT_BUS.addListener(KidayAdvancements::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::bindStormGroveBiome);
        NeoForge.EVENT_BUS.addListener(this::refreshStormGroveFeatures);
        NeoForge.EVENT_BUS.addListener(this::clearStormGroveBiome);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FireBlock fire = (FireBlock) Blocks.FIRE;
            // Waqwaq blocks burn when fire touches them, but do not fuel spreading fire.
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_LOG.get(), 5);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_WOOD_BLOCK.get(), 5);
            setWaqwaqFlammable(fire, ModBlocks.STRIPPED_WAQWAQ_LOG.get(), 5);
            setWaqwaqFlammable(fire, ModBlocks.STRIPPED_WAQWAQ_WOOD.get(), 5);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_PLANKS.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_STAIRS.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_SLAB.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_FENCE.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_FENCE_GATE.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_DOOR.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_TRAPDOOR.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_BUTTON.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_PRESSURE_PLATE.get(), 20);
            setWaqwaqFlammable(fire, ModBlocks.WAQWAQ_LEAVES.get(), 60);
        });
    }

    private static void setWaqwaqFlammable(FireBlock fire, Block block, int flammability) {
        fire.setFlammable(block, 0, flammability);
    }


    private void bindStormGroveBiome(ServerAboutToStartEvent event) {
        StormGroveBiomeResolver.bind(event.getServer());
    }

    private void refreshStormGroveFeatures(ServerStartedEvent event) {
        var server = event.getServer();
        // The generator may have validated and cached featuresPerStep before the biome was bound.
        // Rebuild that cache now that Storm Grove is included in the Overworld's possible biomes.
        server.overworld().getChunkSource().getGenerator().refreshFeaturesPerStep();
    }

    private void clearStormGroveBiome(ServerStoppedEvent event) {
        StormGroveBiomeResolver.clear();
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LEI_GONG.get(), LeiGongEntity.createAttributes().build());
        event.put(ModEntities.STORM_SPIRIT.get(), StormSpiritEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.STORM_SPIRIT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> level.getBiome(pos).is(ModBiomes.STORM_GROVE)
                        && Mob.checkMobSpawnRules(type, level, spawnType, pos, random),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private void addPatternDeployerRecipe(DeployerRecipeSearchEvent event) {
        if (!event.getInventory().getItem(0).is(ModItems.BLANK_MOLD.get())) return;
        PatternShapingDeployerRecipe recipe = PatternShapingDeployerRecipe.forSample(event.getInventory().getItem(1));
        if (recipe == null || !event.shouldAddRecipeWithPriority(100)) return;
        event.addRecipe(() -> java.util.Optional.of(new net.minecraft.world.item.crafting.RecipeHolder<>(
                PatternShapingDeployerRecipe.id(recipe.result()), recipe)), 100);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(OpenChroniclePayload.TYPE, OpenChroniclePayload.STREAM_CODEC, OpenChroniclePayload::handle)
                .playToServer(LightningStaffPayload.TYPE, LightningStaffPayload.STREAM_CODEC, LightningStaffPayload::handle);
    }

}

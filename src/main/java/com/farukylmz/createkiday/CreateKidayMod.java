package com.farukylmz.createkiday;

import com.farukylmz.createkiday.registry.ModBlocks;
import com.farukylmz.createkiday.registry.ModBlockEntities;
import com.farukylmz.createkiday.registry.ModFluids;
import com.farukylmz.createkiday.registry.ModEquipment;
import com.farukylmz.createkiday.registry.ModEntities;
import com.farukylmz.createkiday.registry.ModRecipes;
import com.farukylmz.createkiday.registry.ModMobEffects;
import com.farukylmz.createkiday.registry.ModSounds;
import com.farukylmz.createkiday.registry.ModFeatures;
import com.farukylmz.createkiday.registry.ModCreativeTabs;
import com.farukylmz.createkiday.network.LaunchDiskPayload;
import com.farukylmz.createkiday.network.OpenChroniclePayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.farukylmz.createkiday.registry.ModItems;
import com.mojang.logging.LogUtils;
import com.farukylmz.createkiday.recipe.PatternShapingDeployerRecipe;
import com.farukylmz.createkiday.advancement.KidayAdvancements;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import com.farukylmz.createkiday.entity.LeiGongEntity;
import com.farukylmz.createkiday.registry.ModBiomes;
import com.farukylmz.createkiday.gameplay.StormGroveBiomeResolver;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import org.slf4j.Logger;

@Mod(CreateKidayMod.MOD_ID)
public final class CreateKidayMod {
    public static final String MOD_ID = "create_kiday";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateKidayMod(IEventBus modBus) {
        ModFluids.register(modBus);
        ModBlocks.register(modBus);
        ModEquipment.register(modBus);
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
        NeoForge.EVENT_BUS.addListener(this::bindStormGroveBiome);
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

    private void clearStormGroveBiome(ServerStoppedEvent event) {
        StormGroveBiomeResolver.clear();
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LEI_GONG.get(), LeiGongEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.LEI_GONG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> level.getBiome(pos).is(ModBiomes.STORM_GROVE)
                        && net.minecraft.world.entity.monster.Monster.checkAnyLightMonsterSpawnRules(
                                type, level, spawnType, pos, random),
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
                .playToServer(LaunchDiskPayload.TYPE, LaunchDiskPayload.STREAM_CODEC, LaunchDiskPayload::handle)
                .playToClient(OpenChroniclePayload.TYPE, OpenChroniclePayload.STREAM_CODEC, OpenChroniclePayload::handle);
    }

}

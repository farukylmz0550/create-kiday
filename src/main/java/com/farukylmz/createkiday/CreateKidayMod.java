package com.farukylmz.createkiday;

import com.farukylmz.createkiday.registry.ModBlocks;
import com.farukylmz.createkiday.registry.ModBlockEntities;
import com.farukylmz.createkiday.registry.ModFluids;
import com.farukylmz.createkiday.registry.ModEquipment;
import com.farukylmz.createkiday.registry.ModEntities;
import com.farukylmz.createkiday.registry.ModRecipes;
import com.farukylmz.createkiday.registry.ModMobEffects;
import com.farukylmz.createkiday.registry.ModCreativeTabs;
import com.farukylmz.createkiday.registry.ModDataComponents;
import com.farukylmz.createkiday.registry.ModMenus;
import com.farukylmz.createkiday.network.LaunchDiskPayload;
import com.farukylmz.createkiday.network.OpenChroniclePayload;
import com.farukylmz.createkiday.network.SaveTagFilterPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.farukylmz.createkiday.registry.ModItems;
import com.mojang.logging.LogUtils;
import com.farukylmz.createkiday.recipe.PatternShapingDeployerRecipe;
import com.farukylmz.createkiday.advancement.KidayAdvancements;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import com.farukylmz.createkiday.entity.LeiGongEntity;
import com.farukylmz.createkiday.registry.ModBiomes;
import com.farukylmz.createkiday.gameplay.StormGroveBiomeResolver;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
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
        ModDataComponents.register(modBus);
        ModMenus.register(modBus);
        ModBlockEntities.register(modBus);
        ModEntities.register(modBus);
        ModRecipes.register(modBus);
        ModMobEffects.register(modBus);
        ModCreativeTabs.register(modBus);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::addCreativeItems);
        modBus.addListener(this::registerEntityAttributes);
        modBus.addListener(this::registerSpawnPlacements);
        NeoForge.EVENT_BUS.addListener(this::addPatternDeployerRecipe);
        NeoForge.EVENT_BUS.addListener(KidayAdvancements::onKidayOreDrops);
        NeoForge.EVENT_BUS.addListener(KidayAdvancements::checkCompletion);
        NeoForge.EVENT_BUS.addListener(this::bindStormGroveBiome);
        NeoForge.EVENT_BUS.addListener(this::clearStormGroveBiome);
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
                .playToServer(SaveTagFilterPayload.TYPE, SaveTagFilterPayload.STREAM_CODEC, SaveTagFilterPayload::handle)
                .playToClient(OpenChroniclePayload.TYPE, OpenChroniclePayload.STREAM_CODEC, OpenChroniclePayload::handle);
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            ModItems.visibleIngredients().forEach(item -> event.accept(item.get()));
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            ModItems.visibleEquipment().forEach(item -> event.accept(item.get()));
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.LEI_GONG_SPAWN_EGG.get());
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.KIDAY_BLOCK_ITEM);
            event.accept(ModBlocks.KIDAY_DOOR_ITEM);
            event.accept(ModBlocks.KIDAY_TRAPDOOR_ITEM);
            event.accept(ModBlocks.CASTING_TABLE_ITEM);
            event.accept(ModBlocks.LIGHTNING_ENGINE_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModBlocks.KIDAY_ORE_ITEM);
            event.accept(ModBlocks.DEEPSLATE_KIDAY_ORE_ITEM);
        }
    }
}

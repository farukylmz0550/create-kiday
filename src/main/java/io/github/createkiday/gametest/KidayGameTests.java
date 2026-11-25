package io.github.createkiday.gametest;

import io.github.createkiday.block.entity.CastingTableBlockEntity;
import io.github.createkiday.registry.ModBlocks;
import io.github.createkiday.registry.ModItems;
import io.github.createkiday.registry.ModCreativeTabs;
import io.github.createkiday.registry.ModFluids;
import io.github.createkiday.entity.LeiGongEntity;
import io.github.createkiday.gameplay.StormAmuletEvents;
import io.github.createkiday.item.KidayChronicleItem;
import io.github.createkiday.registry.ModBiomes;
import io.github.createkiday.registry.ModDamageTypes;
import io.github.createkiday.registry.ModMobEffects;
import io.github.createkiday.registry.ModEntities;
import io.github.createkiday.recipe.PatternShapingDeployerRecipe;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import java.util.UUID;
import java.util.List;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.util.FakePlayer;

@GameTestHolder("create_kiday")
public final class KidayGameTests {
    private KidayGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void heatedBasinMeltsRawKidayWithoutSuperheat(GameTestHelper helper) {
        MixingRecipe recipe = (MixingRecipe) helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath("create_kiday", "melting_raw_kiday"))
                .orElseThrow().value();
        helper.assertTrue(recipe.getRequiredHeat() == HeatCondition.HEATED,
                "The Raw Kiday melting recipe requires superheat instead of normal heat");
        BlockPos basinPos = new BlockPos(2, 2, 3);
        helper.setBlock(basinPos, AllBlocks.BASIN.get());
        BasinBlockEntity basin = helper.getBlockEntity(basinPos);
        basin.getInputInventory().insertItem(0, ModItems.RAW_KIDAY.toStack(), false);
        helper.assertFalse(BasinRecipe.match(basin, recipe), "A cold Basin could melt Raw Kiday");
        helper.setBlock(basinPos.below(), AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        basin.tick();
        helper.assertTrue(BasinRecipe.match(basin, recipe), "A normally heated Basin rejected the melting recipe");
        helper.assertTrue(BasinRecipe.apply(basin, recipe), "The Create Basin process did not run with normal heat");
        var fluidHandler = helper.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, helper.absolutePos(basinPos), null);
        helper.assertTrue(fluidHandler != null, "The Basin does not expose a fluid output capability");
        FluidStack molten = fluidHandler.drain(1_000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(molten.getFluid() == ModFluids.MOLTEN_KIDAY.get() && molten.getAmount() == 144,
                "Normal heat did not produce 144 mB of Molten Kiday from one Raw Kiday");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void heatedBasinMeltsRawSalurIntoOneIngotVolume(GameTestHelper helper) {
        MixingRecipe recipe = (MixingRecipe) helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath("create_kiday", "melting_raw_salur"))
                .orElseThrow().value();
        helper.assertTrue(recipe.getRequiredHeat() == HeatCondition.HEATED,
                "Raw Salur melting unexpectedly requires superheat");
        BlockPos basinPos = new BlockPos(2, 2, 3);
        helper.setBlock(basinPos, AllBlocks.BASIN.get());
        BasinBlockEntity basin = helper.getBlockEntity(basinPos);
        basin.getInputInventory().insertItem(0, ModItems.RAW_SALUR.toStack(), false);
        helper.setBlock(basinPos.below(), AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        basin.tick();
        helper.assertTrue(BasinRecipe.apply(basin, recipe), "The heated Basin did not melt Raw Salur");
        var fluidHandler = helper.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, helper.absolutePos(basinPos), null);
        helper.assertTrue(fluidHandler != null, "The Basin does not expose its fluid output capability");
        FluidStack molten = fluidHandler.drain(1_000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(molten.getFluid() == ModFluids.MOLTEN_SALUR.get() && molten.getAmount() == 144,
                "Raw Salur did not melt into exactly 144 mB of Molten Salur");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 70)
    public static void oneIngotMoldSelectsTheMatchingMetalWithoutMixing(GameTestHelper helper) {
        BlockPos kidayPos = new BlockPos(2, 1, 2);
        BlockPos salurPos = new BlockPos(5, 1, 2);
        helper.setBlock(kidayPos, ModBlocks.CASTING_TABLE.get());
        helper.setBlock(salurPos, ModBlocks.CASTING_TABLE.get());
        CastingTableBlockEntity kidayTable = helper.getBlockEntity(kidayPos);
        CastingTableBlockEntity salurTable = helper.getBlockEntity(salurPos);
        ItemStack sharedMold = new ItemStack(ModItems.SHAPED_MOLDS.getFirst().get());
        helper.assertTrue(kidayTable.insertMold(sharedMold), "The Kiday table rejected the shared ingot mold");
        helper.assertTrue(salurTable.insertMold(sharedMold), "The Salur table rejected the shared ingot mold");
        helper.assertTrue(kidayTable.fluidHandler().fill(new FluidStack(ModFluids.MOLTEN_KIDAY.get(), 144),
                        IFluidHandler.FluidAction.EXECUTE) == 144,
                "Molten Kiday was not accepted in the shared mold table");
        helper.assertTrue(salurTable.fluidHandler().fill(new FluidStack(ModFluids.MOLTEN_SALUR.get(), 144),
                        IFluidHandler.FluidAction.EXECUTE) == 144,
                "Molten Salur was not accepted in the shared mold table");
        helper.assertTrue(kidayTable.fluidHandler().fill(new FluidStack(ModFluids.MOLTEN_SALUR.get(), 144),
                        IFluidHandler.FluidAction.EXECUTE) == 0
                        && salurTable.fluidHandler().fill(new FluidStack(ModFluids.MOLTEN_KIDAY.get(), 144),
                        IFluidHandler.FluidAction.EXECUTE) == 0,
                "A casting table mixed Kiday and Salur fluids");
        helper.runAtTickTime(25, () -> {
            ItemStack kidayOutput = kidayTable.outputHandler().getStackInSlot(0);
            ItemStack salurOutput = salurTable.outputHandler().getStackInSlot(0);
            helper.assertTrue(kidayOutput.is(ModItems.HOT_PRODUCTS.getFirst().get()), "Molten Kiday did not cast a shared hot ingot");
            helper.assertTrue(salurOutput.is(ModItems.HOT_PRODUCTS.getFirst().get()), "Molten Salur did not cast a shared hot ingot");
            helper.assertTrue("kiday".equals(kidayOutput.get(
                            io.github.createkiday.registry.ModDataComponents.METAL_TYPE.get())),
                    "The Kiday metal identity was lost or mixed");
            helper.assertTrue("salur".equals(salurOutput.get(
                            io.github.createkiday.registry.ModDataComponents.METAL_TYPE.get())),
                    "The Salur metal identity was lost or mixed");
            helper.assertTrue(kidayTable.tank().isEmpty() && salurTable.tank().isEmpty(),
                    "Casting left liquid metal in one of the completed tables");
            helper.assertTrue(kidayTable.patternStack().getDamageValue() == 1
                            && salurTable.patternStack().getDamageValue() == 1,
                    "Casting must damage the shared mold exactly once per process");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1, batch = "casting")
    public static void coolerRecipesRejectUnknownHotArmorMetal(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var kiday = (SplashingRecipe) manager.byKey(
                ResourceLocation.fromNamespaceAndPath("create_kiday", "washing_kiday_helmet"))
                .orElseThrow().value();
        var salur = (SplashingRecipe) manager.byKey(
                ResourceLocation.fromNamespaceAndPath("create_kiday", "washing_salur_helmet"))
                .orElseThrow().value();
        ItemStack kidayHot = ModItems.HOT_PRODUCTS.get(6).toStack();
        kidayHot.set(io.github.createkiday.registry.ModDataComponents.METAL_TYPE.get(), "kiday");
        ItemStack salurHot = ModItems.HOT_PRODUCTS.get(6).toStack();
        salurHot.set(io.github.createkiday.registry.ModDataComponents.METAL_TYPE.get(), "salur");
        ItemStack unknownHot = ModItems.HOT_PRODUCTS.get(6).toStack();
        unknownHot.set(io.github.createkiday.registry.ModDataComponents.METAL_TYPE.get(), "unknown");

        helper.assertTrue(kiday.matches(new SingleRecipeInput(kidayHot), helper.getLevel()),
                "Kiday cooler recipe did not match Kiday hot armor");
        helper.assertTrue(!salur.matches(new SingleRecipeInput(kidayHot), helper.getLevel()),
                "Salur cooler recipe also matched Kiday hot armor");
        helper.assertTrue(salur.matches(new SingleRecipeInput(salurHot), helper.getLevel()),
                "Salur cooler recipe did not match Salur hot armor");
        helper.assertTrue(!kiday.matches(new SingleRecipeInput(salurHot), helper.getLevel()),
                "Kiday cooler recipe also matched Salur hot armor");
        helper.assertTrue(!kiday.matches(new SingleRecipeInput(unknownHot), helper.getLevel())
                        && !salur.matches(new SingleRecipeInput(unknownHot), helper.getLevel()),
                "Hot armor with an unknown metal matched a cooler recipe");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 5, batch = "projectiles")
    public static void creativeDiskThrowsDoNotConsumeDurabilityOrStacks(GameTestHelper helper) {
        ServerPlayer player = new DamageableFakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "creative-disk-user"));
        helper.getLevel().addNewPlayer(player);
        player.setGameMode(GameType.CREATIVE);
        player.setPos(2.5, 2.0, 2.5);
        ItemStack salurDisk = ModItems.SALUR_DISK.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, salurDisk);
        ModItems.SALUR_DISK.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(salurDisk.getDamageValue() == 0 && salurDisk.getCount() == 1,
                "Creative use damaged or consumed the Salur Disc");

        ItemStack tntDisk = ModItems.TNT_DISC.toStack(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, tntDisk);
        ModItems.TNT_DISC.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(tntDisk.getCount() == 2,
                "Creative use consumed the TNT Disc stack");

        ServerPlayer survivalPlayer = new DamageableFakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "survival-disk-user"));
        helper.getLevel().addNewPlayer(survivalPlayer);
        survivalPlayer.setGameMode(GameType.SURVIVAL);
        ItemStack finalUse = ModItems.SALUR_DISK.toStack();
        finalUse.setDamageValue(finalUse.getMaxDamage() - 1);
        survivalPlayer.setItemInHand(InteractionHand.MAIN_HAND, finalUse);
        ModItems.SALUR_DISK.get().use(helper.getLevel(), survivalPlayer, InteractionHand.MAIN_HAND);
        helper.assertTrue(survivalPlayer.getMainHandItem().isEmpty(),
                "The Salur Disc remained in hand after its final durability use");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void salurArmorSpeedModifiersReconcileAndClearWithoutStacking(GameTestHelper helper) {
        ServerPlayer player = new DamageableFakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "salur-armor-wearer"));
        helper.getLevel().addNewPlayer(player);
        player.setGameMode(GameType.SURVIVAL);
        double baseSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        player.setItemSlot(EquipmentSlot.HEAD, ModItems.SALUR_HELMET.toStack());
        player.setItemSlot(EquipmentSlot.CHEST, ModItems.SALUR_CHESTPLATE.toStack());
        player.setItemSlot(EquipmentSlot.LEGS, ModItems.SALUR_LEGGINGS.toStack());
        player.setItemSlot(EquipmentSlot.FEET, ModItems.SALUR_BOOTS.toStack());
        helper.runAtTickTime(22, () -> {
            helper.assertTrue(Math.abs(player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                            - baseSpeed * 1.10) < 0.0001,
                    "Four Salur pieces did not apply exactly a 10% movement bonus; speed="
                            + player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED));
            helper.assertTrue(Math.abs(player.getAttributeValue(
                                    net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                            - baseSpeed * 1.10) < 0.0001,
                    "The periodic armor reconciliation stacked movement modifiers");
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            helper.runAtTickTime(22, () -> {
                helper.assertTrue(Math.abs(player.getAttributeValue(
                                        net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                                - baseSpeed) < 0.0001,
                        "Removing Salur armor left a movement modifier behind; speed="
                                + player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                                + ", headEmpty=" + player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                                + ", chestEmpty=" + player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                                + ", legsEmpty=" + player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                                + ", feetEmpty=" + player.getItemBySlot(EquipmentSlot.FEET).isEmpty());
                helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void insufficientFluidDoesNotStartCasting(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.CASTING_TABLE.get());
        CastingTableBlockEntity table = helper.getBlockEntity(pos);
        table.insertMold(new ItemStack(ModItems.SHAPED_MOLDS.getFirst().get()));
        helper.runAtTickTime(5, () -> {
            helper.assertTrue(table.progress() == 0, "Casting started without 144 mB of fluid");
            helper.assertTrue(table.tank().isEmpty(), "Casting consumed or created unexpected fluid");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void automationCannotInsertOrExtractMold(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.CASTING_TABLE.get());
        CastingTableBlockEntity table = helper.getBlockEntity(pos);
        ItemStack mold = new ItemStack(ModItems.SHAPED_MOLDS.getFirst().get());
        helper.assertTrue(table.outputHandler().insertItem(0, mold, false).getCount() == 1, "Output capability accepted a mold");
        helper.assertTrue(table.outputHandler().extractItem(0, 1, false).isEmpty(), "Output capability exposed a non-output slot");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void castingTableAcceptsOnlyInstalledPatternAmount(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.CASTING_TABLE.get());
        CastingTableBlockEntity table = helper.getBlockEntity(pos);
        FluidStack excess = new FluidStack(ModFluids.MOLTEN_KIDAY.get(), 1_000);
        helper.assertTrue(table.fluidHandler().fill(excess, IFluidHandler.FluidAction.EXECUTE) == 0,
                "Fluid was accepted without an installed Pattern");
        ItemStack pattern = new ItemStack(ModItems.SHAPED_MOLDS.getFirst().get());
        table.insertMold(pattern);
        helper.assertTrue(table.patternStack().is(pattern.getItem()), "The installed Pattern is absent from the block entity");
        helper.assertTrue(table.fluidHandler().getTankCapacity(0) == 144, "The Ingot Pattern capacity is not 144 mB");
        helper.assertTrue(table.fluidHandler().fill(excess, IFluidHandler.FluidAction.EXECUTE) == 144,
                "The table accepted more or less fluid than the Pattern requires");
        CastingTableBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(pos), table.getBlockState(), table);
        helper.assertTrue(table.fluidHandler().fill(excess, IFluidHandler.FluidAction.EXECUTE) == 0,
                "New fluid was accepted during an active casting operation");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void chronicleRightClickHasReadableContent(GameTestHelper helper) {
        ItemStack chronicle = ModItems.KIDAY_CHRONICLE.get().getDefaultInstance();
        helper.assertTrue(chronicle.has(DataComponents.WRITTEN_BOOK_CONTENT), "The chronicle was created without written-book content");
        helper.assertTrue(chronicle.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size() == KidayChronicleItem.PAGES.size(),
                "The chronicle is missing pages");
        for (int page = 0; page < KidayChronicleItem.PAGE_COUNT; page++) {
            var raw = chronicle.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().get(page).raw();
            int pageNumber = page + 1;
            helper.assertTrue(raw.getContents() instanceof TranslatableContents translated
                            && translated.getKey().equals("book.create_kiday.chronicle.page_" + pageNumber),
                    "The chronicle page does not store a translation key instead of raw Java text: " + pageNumber);
        }
        helper.assertTrue(chronicle.getHoverName().getContents() instanceof TranslatableContents translated
                        && translated.getKey().equals("item.create_kiday.kiday_chronicle"),
                "The written book's raw title overrides the translatable item name");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void dedicatedCreativeTabIsRegistered(GameTestHelper helper) {
        ResourceLocation tab = ResourceLocation.fromNamespaceAndPath("create_kiday", "kiday");
        helper.assertTrue(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(ModCreativeTabs.KIDAY.get()).equals(tab),
                "The combined Kiday creative tab is not registered");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void deployerRecipeCreatesMatchingPattern(GameTestHelper helper) {
        ItemStackHandler inventory = new ItemStackHandler(2);
        inventory.setStackInSlot(0, new ItemStack(ModItems.BLANK_MOLD.get()));
        inventory.setStackInSlot(1, new ItemStack(ModItems.PICKAXE_HEAD.get()));
        PatternShapingDeployerRecipe recipe = PatternShapingDeployerRecipe.forSample(inventory.getStackInSlot(1));
        helper.assertTrue(recipe != null && recipe.matches(new RecipeWrapper(inventory), helper.getLevel()),
                "The Deployer could not find the Pattern recipe for the sample");
        helper.assertTrue(recipe.assemble(new RecipeWrapper(inventory), helper.getLevel().registryAccess())
                        .is(ModItems.SHAPED_MOLDS.get(2).get()),
                "The Deployer produced the wrong Pattern");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void moltenKidayBucketPlacesLavaLikeDamagingFluid(GameTestHelper helper) {
        BlockPos relative = new BlockPos(4, 1, 4);
        BlockPos position = helper.absolutePos(relative);
        helper.getLevel().setBlock(position, Blocks.AIR.defaultBlockState(), 3);
        helper.assertTrue(helper.getLevel().getBlockState(position).isAir(), "The bucket test target is not air");
        helper.assertTrue(ModFluids.MOLTEN_KIDAY.get() instanceof net.minecraft.world.level.material.FlowingFluid,
                "Molten Kiday is not a FlowingFluid that BucketItem can place");
        helper.assertTrue(ModFluids.MOLTEN_KIDAY.get().defaultFluidState().createLegacyBlock().is(ModBlocks.MOLTEN_KIDAY.get()),
                "The Molten Kiday fluid state does not convert to its liquid block");
        boolean placed = ModItems.MOLTEN_KIDAY_BUCKET.get().emptyContents(null, helper.getLevel(), position, null,
                ModItems.MOLTEN_KIDAY_BUCKET.toStack());
        helper.assertTrue(placed, "The Molten Kiday bucket did not place fluid in the world");
        helper.assertTrue(helper.getLevel().getFluidState(position).is(ModFluids.MOLTEN_KIDAY.get()),
                "The placed fluid is not a Molten Kiday source");
        helper.assertTrue(helper.getLevel().getFluidState(position).is(FluidTags.LAVA), "Molten Kiday is not in the lava tag");
        helper.assertTrue(helper.getLevel().getMaxLocalRawBrightness(position) >= 15, "Molten Kiday does not emit light like lava");
        var creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(4.5, 1.0, 4.5));
        float before = creeper.getHealth();
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(creeper.getHealth() < before || creeper.getRemainingFireTicks() > 0,
                    "An entity inside Molten Kiday neither burned nor took damage");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void oreWorldgenRegistriesLoadAndReplaceStone(GameTestHelper helper) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("create_kiday", "kiday_ore");
        var configured = helper.getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(id);
        helper.assertTrue(configured != null, "The Kiday ore configured feature registration did not load");
        helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE).containsKey(id),
                "The Kiday ore placed feature registration did not load");
        helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS)
                        .containsKey(ResourceLocation.fromNamespaceAndPath("create_kiday", "add_kiday_ore")),
                "The Kiday ore biome modifier registration did not load");
        BlockPos center = helper.absolutePos(new BlockPos(4, 2, 4));
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -4, -4), center.offset(4, 4, 4)))
            helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
        boolean placed = configured.place(helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(),
                RandomSource.create(42), center);
        long ores = BlockPos.betweenClosedStream(center.offset(-5, -5, -5), center.offset(5, 5, 5))
                .filter(pos -> helper.getLevel().getBlockState(pos).is(ModBlocks.KIDAY_ORE.get())).count();
        helper.assertTrue(placed && ores > 0, "The Kiday ore feature generated no ore in a stone-filled chunk area");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void doorAndTrapdoorUseThinShapes(GameTestHelper helper) {
        var doorBounds = ModBlocks.KIDAY_DOOR.get().defaultBlockState().getShape(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1))).bounds();
        var trapBounds = ModBlocks.KIDAY_TRAPDOOR.get().defaultBlockState().getShape(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 1))).bounds();
        helper.assertTrue(Math.min(doorBounds.getXsize(), doorBounds.getZsize()) <= 3.0 / 16.0,
                "The door collision shape is not thin");
        helper.assertTrue(trapBounds.getYsize() <= 3.0 / 16.0, "The trapdoor collision shape is not thin");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void kidayDoorsRequireRedstoneAndTrackPower(GameTestHelper helper) {
        BlockPos doorPos = new BlockPos(2, 1, 2);
        BlockPos trapdoorPos = new BlockPos(6, 1, 2);
        BlockPos doorPower = doorPos.west();
        BlockPos trapdoorPower = trapdoorPos.west();

        helper.setBlock(doorPos.below(), Blocks.STONE);
        helper.setBlock(doorPos, ModBlocks.KIDAY_DOOR.get());
        helper.setBlock(doorPos.above(), ModBlocks.KIDAY_DOOR.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        helper.setBlock(trapdoorPos, ModBlocks.KIDAY_TRAPDOOR.get());

        helper.useBlock(doorPos);
        helper.useBlock(trapdoorPos);
        helper.assertFalse(helper.getBlockState(doorPos).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),
                "The Kiday door opened by hand");
        helper.assertFalse(helper.getBlockState(trapdoorPos).getValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN),
                "The Kiday trapdoor opened by hand");

        helper.setBlock(doorPower, Blocks.REDSTONE_BLOCK);
        helper.setBlock(trapdoorPower, Blocks.REDSTONE_BLOCK);

        helper.assertTrue(helper.getBlockState(doorPos).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),
                "The Kiday door did not open when powered");
        helper.assertTrue(helper.getBlockState(trapdoorPos).getValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN),
                "The Kiday trapdoor did not open when powered");

        helper.setBlock(doorPower, Blocks.AIR);
        helper.setBlock(trapdoorPower, Blocks.AIR);
        helper.assertFalse(helper.getBlockState(doorPos).getValue(net.minecraft.world.level.block.DoorBlock.OPEN),
                "The Kiday door did not close when power was removed");
        helper.assertFalse(helper.getBlockState(trapdoorPos).getValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN),
                "The Kiday trapdoor did not close when power was removed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void breakingDoorDropsExactlyOneDoor(GameTestHelper helper) {
        BlockPos lowerBreak = new BlockPos(1, 1, 1);
        BlockPos upperBreak = new BlockPos(4, 1, 1);
        var lower = ModBlocks.KIDAY_DOOR.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
        var upper = lower.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
        for (BlockPos pos : List.of(lowerBreak, upperBreak)) {
            helper.setBlock(pos.below(), Blocks.STONE);
            helper.setBlock(pos, lower);
            helper.setBlock(pos.above(), upper);
        }
        helper.getLevel().destroyBlock(helper.absolutePos(lowerBreak), true);
        helper.getLevel().destroyBlock(helper.absolutePos(upperBreak.above()), true);
        helper.runAtTickTime(2, () -> {
            helper.assertItemEntityCountIs(ModBlocks.KIDAY_DOOR_ITEM.get(), lowerBreak, 1.25, 1);
            helper.assertItemEntityCountIs(ModBlocks.KIDAY_DOOR_ITEM.get(), upperBreak, 1.25, 1);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void lightningEngineUsesCreateRpmControlAndFrontShaft(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        var state = ModBlocks.LIGHTNING_ENGINE.get().defaultBlockState()
                .setValue(io.github.createkiday.block.LightningEngineBlock.HORIZONTAL_FACING, net.minecraft.core.Direction.EAST);
        helper.setBlock(pos, state);
        var engine = (io.github.createkiday.block.entity.LightningEngineBlockEntity) helper.getBlockEntity(pos);
        helper.assertTrue(engine.targetSpeed != null, "The Lightning Engine did not create a Create ScrollValue RPM control");
        helper.assertTrue(engine.targetSpeed.getClass()
                        == com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour.class,
                "The Lightning Engine RPM menu does not use the reference standard ScrollValueBehaviour");
        int[] rmpValues = {16, 32, 64, 128};
        for (int index = 0; index < rmpValues.length; index++) {
            engine.targetSpeed.setValue(index);
            helper.assertTrue(engine.selectedRpm() == rmpValues[index], "The Create RPM control did not apply tier " + rmpValues[index]);
            helper.assertTrue(io.github.createkiday.KidayBalance.stressCapacity(rmpValues[index]) > 0,
                    "RPM tier " + rmpValues[index] + " has no configured capacity");
        }
        helper.assertTrue(io.github.createkiday.block.entity.LightningEngineBlockEntity.isRpmControlSide(
                        state, net.minecraft.core.Direction.WEST), "The RPM control is not on the engine's back face");
        helper.assertFalse(io.github.createkiday.block.entity.LightningEngineBlockEntity.isRpmControlSide(
                        state, net.minecraft.core.Direction.EAST), "The RPM control is also active on the shaft's front face");
        var controlCenter = io.github.createkiday.block.entity.LightningEngineBlockEntity.RPM_CONTROL_BACK_CENTER;
        helper.assertTrue(controlCenter.x == 0.5 && controlCenter.y == 0.5,
                "The RPM control is not centered on the back face");
        helper.assertTrue(ModBlocks.LIGHTNING_ENGINE.get().hasShaftTowards(helper.getLevel(), helper.absolutePos(pos), state,
                        net.minecraft.core.Direction.EAST), "The Lightning Engine does not expose a shaft on its front face");
        helper.assertFalse(ModBlocks.LIGHTNING_ENGINE.get().hasShaftTowards(helper.getLevel(), helper.absolutePos(pos), state,
                        net.minecraft.core.Direction.UP), "The Lightning Engine incorrectly exposes a shaft on top");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 30)
    public static void biomassGeneratorHasTaggedAutomatedFuelAndWholeRpmControl(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, ModBlocks.BIOMASS_GENERATOR.get());
        var generator = (io.github.createkiday.block.entity.BiomassGeneratorBlockEntity) helper.getBlockEntity(pos);
        helper.assertTrue(generator.selectedRpm() == 32, "The Biomass Generator should start at 32 RPM, got " + generator.selectedRpm());
        for (int rpm : new int[]{8, 32, 64}) {
            generator.targetSpeed.setValue(rpm);
            helper.assertTrue(generator.selectedRpm() == rpm, "The Biomass Generator rejected whole RPM value " + rpm);
            helper.assertTrue(generator.calculateAddedStressCapacity() == 4096.0f / rpm,
                    "The Biomass Generator did not calculate 4096 / RPM SU at " + rpm);
        }
        var input = generator.itemHandler();
        ItemStack coal = new ItemStack(Items.COAL, 40);
        ItemStack remainder = input.insertItem(0, coal, false);
        helper.assertTrue(remainder.getCount() == 8, "The first Biomass slot should cap at 32 items");
        input.insertItem(1, new ItemStack(Items.CHARCOAL, 32), false);
        helper.assertTrue(input.extractItem(0, 1, false).isEmpty(), "Fuel must not be extractable through the input capability");
        helper.runAtTickTime(3, () -> {
            helper.assertTrue(input.getStackInSlot(0).getCount() == 31, "Fuel in the first slot was not consumed first");
            helper.assertTrue(input.getStackInSlot(1).getCount() == 32, "Fuel from the second slot was consumed before the first slot");
            helper.assertTrue(generator.fuelTicks() > 0, "The Biomass Generator did not retain its burning time");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void lightningEngineRodAttractsAndConsumesRealLightning(GameTestHelper helper) {
        BlockPos relative = new BlockPos(3, 1, 3);
        helper.setBlock(relative, ModBlocks.LIGHTNING_ENGINE.get());
        var engine = (io.github.createkiday.block.entity.LightningEngineBlockEntity) helper.getBlockEntity(relative);
        helper.assertTrue(engine.getGeneratedSpeed() == 0, "The engine is running before a lightning strike");
        var bolt = EntityType.LIGHTNING_BOLT.create(helper.getLevel());
        helper.assertTrue(bolt != null, "The test lightning bolt could not be created");
        bolt.setVisualOnly(true);
        BlockPos enginePosition = helper.absolutePos(relative);
        var engineChunk = new net.minecraft.world.level.ChunkPos(enginePosition);
        bolt.moveTo(engineChunk.getMinBlockX() + 15.5, enginePosition.getY() + 4.0,
                engineChunk.getMinBlockZ() + 15.5);
        helper.assertTrue(bolt.chunkPosition().equals(engineChunk),
                "The test lightning bolt is not in the same chunk as the Lightning Engine");
        helper.getLevel().addFreshEntity(bolt);
        long[] chargedAtTickFive = new long[1];
        helper.runAtTickTime(5, () -> {
            helper.assertTrue(engine.getGeneratedSpeed() > 0, "The lightning rod did not attract real lightning and charge the engine");
            chargedAtTickFive[0] = engine.remainingChargeTicks();
            Vec3 expected = Vec3.atBottomCenterOf(enginePosition).add(0, 2.0, 0);
            helper.assertTrue(bolt.position().distanceToSqr(expected) < 0.25,
                    "The lightning bolt was not redirected to the rod above the engine");
        });
        helper.runAtTickTime(30, () -> {
            helper.assertTrue(engine.remainingChargeTicks() <= chargedAtTickFive[0] - 20,
                    "The Lightning Engine's remaining charge time did not decrease as game time advanced");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 140, batch = "lei_gong_combat")
    public static void leiGongUsesTimedChainingElectricityWithoutLightningEntities(GameTestHelper helper) {
        ServerPlayer target = new DamageableFakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "lei-gong-target"));
        helper.getLevel().addNewPlayer(target);
        target.setGameMode(GameType.SURVIVAL);
        target.getAbilities().invulnerable = false;
        disableSpawnProtection(target);
        target.setPos(helper.absoluteVec(new Vec3(1.5, 30.1, 2.5)));
        target.setHealth(20.0F);
        var firstChain = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(3.5, 1.1, 2.5));
        var secondChain = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(5.5, 1.1, 2.5));
        var untouched = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(8.0, 1.1, 2.5));
        LeiGongEntity leiGong = helper.spawn(ModEntities.LEI_GONG.get(), new Vec3(1.5, 1.1, 7.5));
        firstChain.setNoGravity(true);
        secondChain.setNoGravity(true);
        untouched.setNoGravity(true);
        leiGong.setNoGravity(true);
        helper.assertTrue(leiGong.getMaxHealth() == 250.0F, "Lei Gong does not have 250 maximum health");
        helper.assertTrue(leiGong.hurt(helper.getLevel().damageSources().generic(), 1.0F)
                        && leiGong.getHealth() == 249.0F,
                "Lei Gong's 250 HP health attribute prevented normal damage");
        leiGong.setHealth(leiGong.getMaxHealth());
        helper.assertTrue(LeiGongEntity.LIGHTNING_STRIKES == 2 && LeiGongEntity.PRIMARY_DAMAGE == 6.0F
                        && LeiGongEntity.LIGHTNING_GAP_TICKS == 20,
                "Lei Gong lightning count, damage or the one-second inter-strike interval is wrong");
        helper.assertTrue(LeiGongEntity.STUN_CHANCE == 0.05F && LeiGongEntity.STUN_TICKS == 10,
                "Lei Gong's lightning stun chance or duration is wrong");
        helper.runAtTickTime(65, () -> {
            target.setPos(helper.absoluteVec(new Vec3(1.5, 1.1, 2.5)));
            target.setHealth(20.0F);
            target.invulnerableTime = 0;
            leiGong.setTarget(target);
        });

        helper.runAtTickTime(110, () -> {
                helper.assertTrue(target.getHealth() <= 8.0F,
                    "Lei Gong did not land its two 6 HP lightning strikes: health="
                            + target.getHealth() + " cycle=" + leiGong.attackCycleTick()
                            + " cooldown=" + leiGong.specialCooldown() + " target=" + leiGong.getTarget()
                            + " distance=" + leiGong.distanceTo(target));
            helper.assertTrue(firstChain.getHealth() < firstChain.getMaxHealth()
                            && secondChain.getHealth() < secondChain.getMaxHealth(),
                    "Lei Gong electricity did not chain through two secondary targets");
            helper.assertTrue(untouched.getHealth() == untouched.getMaxHealth(),
                    "Lei Gong electricity exceeded its three-target limit: first=" + firstChain.getHealth()
                            + " second=" + secondChain.getHealth() + " untouched=" + untouched.getHealth());
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class,
                    new AABB(leiGong.position(), target.position()).inflate(16.0)).isEmpty(),
                    "Lei Gong's electrical attack created a vanilla lightning entity");
            helper.getLevel().removePlayerImmediately(target, Entity.RemovalReason.DISCARDED);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void stunPreventsMovementAndExpiresNormally(GameTestHelper helper) {
        ServerPlayer player = new DamageableFakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "stunned-player"));
        helper.getLevel().addNewPlayer(player);
        player.setNoGravity(true);
        player.setPos(helper.absoluteVec(new Vec3(4.5, 1.1, 4.5)));
        player.addEffect(new MobEffectInstance(ModMobEffects.STUN, LeiGongEntity.STUN_TICKS,
                0, false, true, true));
        double startX = player.getX();
        player.setDeltaMovement(0.35, 0, 0);
        player.travel(new Vec3(1, 0, 0));
        player.jumpFromGround();
        helper.assertTrue(player.hasEffect(ModMobEffects.STUN), "The stun effect was not applied to the player");
        helper.assertTrue(Math.abs(player.getX() - startX) < 0.001,
                "Stunned player moved horizontally despite movement input");
        helper.assertTrue(Math.abs(player.getDeltaMovement().x) < 0.001,
                "Stunned player retained horizontal momentum");

        helper.runAtTickTime(12, () -> {
            try {
                var tickEffects = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("tickEffects");
                tickEffects.setAccessible(true);
                for (int tick = 0; tick < LeiGongEntity.STUN_TICKS + 1; tick++) tickEffects.invoke(player);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not advance the stun effect timer", exception);
            }
            helper.assertFalse(player.hasEffect(ModMobEffects.STUN),
                    "The 10-tick stun effect did not expire; remaining="
                            + (player.getEffect(ModMobEffects.STUN) == null ? 0
                                    : player.getEffect(ModMobEffects.STUN).getDuration()));
            double beforeMove = player.getX();
            player.travel(new Vec3(1, 0, 0));
            helper.assertTrue(player.getX() > beforeMove,
                    "Player movement remained locked after the stun expired");
            helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void stormAmuletReducesNaturalAndLeiGongElectricity(GameTestHelper helper) {
        ServerPlayer player = new DamageableFakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "storm-amulet-wearer"));
        helper.getLevel().addNewPlayer(player);
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable = false;
        disableSpawnProtection(player);
        player.setPos(helper.absoluteVec(new Vec3(4.5, 1.1, 4.5)));
        CuriosApi.getCuriosInventory(player).orElseThrow()
                .setEquippedCurio("necklace", 0, ModItems.STORM_AMULET.toStack());
        helper.assertTrue(StormAmuletEvents.isEquipped(player),
                "The Storm Amulet is not recognized in the Curios necklace slot");

        helper.runAtTickTime(65, () -> {
            player.setHealth(20.0F);
            player.invulnerableTime = 0;
            player.hurt(helper.getLevel().damageSources().lightningBolt(), 8.0F);
            helper.assertTrue(Math.abs(player.getHealth() - 16.0F) < 0.01F,
                    "The Storm Amulet did not reduce natural lightning damage by 50 percent: health="
                            + player.getHealth());

            player.invulnerableTime = 0;
            player.setHealth(20.0F);
            player.hurt(helper.getLevel().damageSources().source(ModDamageTypes.LEI_GONG_ELECTRICITY), 8.0F);
            helper.assertTrue(Math.abs(player.getHealth() - 14.0F) < 0.01F,
                    "The Storm Amulet did not reduce Lei Gong electricity by 25 percent: health="
                            + player.getHealth());
            helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void stormGroveHasBoundedBossAndSpiritSpawnRules(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        var stormGrove = biomes.getHolderOrThrow(ModBiomes.STORM_GROVE);
        helper.assertTrue(ModBiomes.STORM_GROVE_WIDTH == 512,
                "Storm Grove region width is not set to 512 blocks");
        var treeFeatureKey = ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath("create_kiday", "waqwaq_trees"));
        var treeFeature = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                .getHolderOrThrow(treeFeatureKey).value();
        helper.assertTrue(stormGrove.value().getGenerationSettings().hasFeature(treeFeature),
                "Waqwaq tree placed feature is missing from Storm Grove generation settings");
        helper.assertTrue(stormGrove.value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                        .anyMatch(spawn -> spawn.type == ModEntities.STORM_SPIRIT.get()),
                "Storm Grove does not naturally spawn Storm Spirits");
        helper.assertTrue(stormGrove.value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                        .noneMatch(spawn -> spawn.type == ModEntities.LEI_GONG.get()),
                "LeiGong still uses natural mob spawning instead of fixed region anchors");
        long otherSpiritBiomes = biomes.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(ModBiomes.STORM_GROVE))
                .filter(entry -> entry.getValue().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                        .anyMatch(spawn -> spawn.type == ModEntities.STORM_SPIRIT.get()))
                .count();
        helper.assertTrue(otherSpiritBiomes == 0, "Storm Spirit is configured to spawn outside Storm Grove");
        helper.assertTrue(io.github.createkiday.entity.LeiGongBalance.bossCount(
                        (ModBiomes.STORM_GROVE_WIDTH / 16) * (ModBiomes.STORM_GROVE_WIDTH / 16)) == 2,
                "A 512 block Storm Grove does not resolve to two LeiGong bosses");
        helper.assertTrue(io.github.createkiday.gameplay.StormGroveBiomeResolver.get() != null,
                "Storm Grove is not bound to the Overworld generator");
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(
                        ResourceLocation.fromNamespaceAndPath("create_kiday", "storm_amulet")).isEmpty(),
                "Storm Amulet unexpectedly has a crafting recipe");
        var egg = ModItems.LEI_GONG_SPAWN_EGG.get();
        helper.assertTrue(egg.getType(ModItems.LEI_GONG_SPAWN_EGG.toStack()) == ModEntities.LEI_GONG.get(),
                "Lei Gong spawn egg does not resolve to Lei Gong");
        var spiritEgg = ModItems.STORM_SPIRIT_SPAWN_EGG.get();
        helper.assertTrue(spiritEgg.getType(ModItems.STORM_SPIRIT_SPAWN_EGG.toStack()) == ModEntities.STORM_SPIRIT.get(),
                "Storm Spirit spawn egg does not resolve to Storm Spirit");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void leiGongDropsCoreAtNinetyNinePercentRoll(GameTestHelper helper) {
        LeiGongEntity leiGong = helper.spawn(ModEntities.LEI_GONG.get(), new Vec3(4.5, 1.1, 4.5));
        long successfulSeed = 0;
        while (RandomSource.create(successfulSeed).nextFloat() >= 0.99F) successfulSeed++;
        leiGong.getRandom().setSeed(successfulSeed);
        leiGong.kill();
        helper.runAtTickTime(2, () -> {
            helper.assertItemEntityPresent(ModItems.LEI_GONG_CORE.get(), new BlockPos(4, 1, 4), 2.0);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "storm_spirit")
    public static void stormSpiritIsNeutralAndHasExpectedHealth(GameTestHelper helper) {
        var spirit = helper.spawn(ModEntities.STORM_SPIRIT.get(), new Vec3(4.5, 4.0, 4.5));
        helper.assertTrue(spirit.getMaxHealth() == 40.0F, "Storm Spirit max health is not 40 HP");
        helper.assertTrue(spirit.getTarget() == null, "Storm Spirit targets players without being provoked");
        helper.assertTrue(spirit.isNoGravity(), "Storm Spirit does not have gravity disabled for flight");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void everyGameplayProductHasAnAcquisitionRecipe(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.getOptional(
                        ResourceLocation.fromNamespaceAndPath("create_kiday", "kiday_paxel")).isEmpty(),
                "The removed Kiday Paxel remains registered");
        helper.assertTrue(BuiltInRegistries.ITEM.getOptional(
                        ResourceLocation.fromNamespaceAndPath("create_kiday", "kiday_paxel_head")).isEmpty(),
                "The removed Kiday Paxel Head remains registered");
        List<String> required = new java.util.ArrayList<>(List.of(
                "blank_mold", "kiday_block", "kiday_ingot_from_block", "kiday_door", "casting_table",
                "lightning_engine", "kiday_chronicle", "disk_mold", "tnt_disc",
                "kiday_sword", "kiday_pickaxe", "kiday_axe", "kiday_shovel", "kiday_hoe",
                "melting_raw_kiday", "pressing_kiday_sheet"));
        required.addAll(List.of("waqwaq_planks", "waqwaq_planks_from_wood", "waqwaq_planks_from_stripped_log",
                "waqwaq_planks_from_stripped_wood", "waqwaq_sticks", "waqwaq_stairs", "waqwaq_slab",
                "waqwaq_fence", "waqwaq_fence_gate", "waqwaq_door", "waqwaq_trapdoor",
                "waqwaq_pressure_plate", "waqwaq_button", "waqwaq_boat", "waqwaq_chest_boat"));
        for (String mold : List.of("ingot", "sword_head", "pickaxe_head", "axe_head", "shovel_head", "hoe_head",
                "helmet", "chestplate", "leggings", "boots", "trapdoor")) {
            required.add("casting_" + mold);
            required.add("mold_shaping_" + mold);
        }
        for (String part : List.of("ingot", "sword_head", "pickaxe_head", "axe_head", "shovel_head", "hoe_head",
                "helmet", "chestplate", "leggings", "boots")) {
            required.add("casting_salur_" + part);
            required.add("washing_kiday_" + part);
            required.add("washing_salur_" + part);
        }
        required.addAll(List.of("salur_block", "salur_ingot_from_block", "smelting_raw_salur", "blasting_raw_salur",
                "pressing_salur_sheet", "casting_salur_disk", "washing_salur_disk", "tnt_disc"));
        for (String id : required)
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(
                            ResourceLocation.fromNamespaceAndPath("create_kiday", id)).isPresent(),
                    "Missing crafting or processing recipe: " + id);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void generatedWaqwaqTreeHasTwoToFiveFruits(GameTestHelper helper) {
        BlockPos base = new BlockPos(8, 20, 8);
        for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++)
            helper.setBlock(base.offset(x, -1, z), Blocks.DIRT);
        var key = ResourceKey.create(Registries.CONFIGURED_FEATURE,
                ResourceLocation.fromNamespaceAndPath("create_kiday", "waqwaq_tree"));
        var configured = helper.getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolderOrThrow(key).value();
        BlockPos worldBase = helper.absolutePos(base);
        helper.assertTrue(configured.place(helper.getLevel(), helper.getLevel().getChunkSource().getGenerator(),
                helper.getLevel().random, worldBase), "Configured Waqwaq tree feature did not place");
        int fruitCount = 0;
        for (int x = -6; x <= 6; x++) for (int y = 0; y <= 18; y++) for (int z = -6; z <= 6; z++)
            if (helper.getLevel().getBlockState(worldBase.offset(x, y, z)).is(ModBlocks.WAQWAQ_FRUIT.get())) fruitCount++;
        helper.assertTrue(fruitCount >= 2 && fruitCount <= 5,
                "A generated Waqwaq tree should have 2–5 fruits, found " + fruitCount);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void waqwaqSaplingGrowsVanillaSpruceStyleTreeWithBonemeal(GameTestHelper helper) {
        BlockPos base = new BlockPos(8, 20, 8);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) helper.setBlock(base.offset(x, 0, z), Blocks.DIRT);
        }
        helper.setBlock(base.above(), ModBlocks.WAQWAQ_SAPLING.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.SaplingBlock.STAGE, 1));
        var sapling = ModBlocks.WAQWAQ_SAPLING.get();
        BlockPos worldBase = helper.absolutePos(base);
        var state = helper.getLevel().getBlockState(worldBase.above());
        helper.assertTrue(sapling.isValidBonemealTarget(helper.getLevel(), worldBase.above(), state),
                "Waqwaq sapling cannot receive bonemeal");
        sapling.performBonemeal(helper.getLevel(), helper.getLevel().random, worldBase.above(), state);
        int logs = 0;
        int leaves = 0;
        for (int x = -5; x <= 5; x++) {
            for (int y = 1; y <= 16; y++) {
                for (int z = -5; z <= 5; z++) {
                    var grown = helper.getLevel().getBlockState(worldBase.offset(x, y, z));
                    if (grown.is(ModBlocks.WAQWAQ_LOG.get())) logs++;
                    if (grown.is(ModBlocks.WAQWAQ_LEAVES.get())) leaves++;
                }
            }
        }
        helper.assertTrue(logs >= 8, "Bonemeal did not grow a full Waqwaq tree: logs=" + logs);
        helper.assertTrue(leaves >= 10, "Bonemeal did not generate spruce-style Waqwaq foliage: leaves=" + leaves);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void waqwaqFruitGrantsSixtySecondLightningProtectionOnly(GameTestHelper helper) {
        ServerPlayer player = new DamageableFakePlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "waqwaq-fruit-eater"));
        helper.getLevel().addNewPlayer(player);
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().invulnerable = false;
        disableSpawnProtection(player);
        player.setPos(helper.absoluteVec(new Vec3(4.5, 1.1, 4.5)));

        ItemStack fruit = ModBlocks.WAQWAQ_FRUIT_ITEM.get().getDefaultInstance();
        var food = fruit.get(DataComponents.FOOD);
        helper.assertTrue(food != null && food.nutrition() == 4 && food.canAlwaysEat(),
                "Waqwaq fruit must restore four hunger points and be edible when full");
        fruit.finishUsingItem(helper.getLevel(), player);
        var blessing = player.getEffect(ModMobEffects.WAQWAQ_BLESSING);
        helper.assertTrue(blessing != null && blessing.getDuration() == 1200,
                "Eating Waqwaq fruit did not apply its 1200 tick blessing");

        float health = player.getHealth();
        player.hurt(helper.getLevel().damageSources().lightningBolt(), 6.0F);
        helper.assertTrue(player.getHealth() == health, "Waqwaq's Blessing did not block vanilla lightning");
        player.invulnerableTime = 0;
        player.hurt(helper.getLevel().damageSources().source(ModDamageTypes.LEI_GONG_ELECTRICITY), 6.0F);
        helper.assertTrue(player.getHealth() == health, "Waqwaq's Blessing did not block Lei Gong's electricity");
        player.invulnerableTime = 0;
        player.hurt(helper.getLevel().damageSources().inFire(), 2.0F);
        helper.assertTrue(player.getHealth() < health, "Waqwaq's Blessing incorrectly blocked non-lightning damage");
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    private static void disableSpawnProtection(ServerPlayer player) {
        try {
            var field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            field.setAccessible(true);
            field.setInt(player, 0);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not disable fake-player spawn protection", exception);
        }
    }

    private static final class DamageableFakePlayer extends FakePlayer {
        private DamageableFakePlayer(ServerLevel level, GameProfile profile) {
            super(level, profile);
        }

        @Override
        public boolean isInvulnerableTo(DamageSource source) {
            return false;
        }
    }
}

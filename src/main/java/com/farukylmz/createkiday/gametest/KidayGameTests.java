package com.farukylmz.createkiday.gametest;

import com.farukylmz.createkiday.block.entity.CastingTableBlockEntity;
import com.farukylmz.createkiday.registry.ModBlocks;
import com.farukylmz.createkiday.registry.ModItems;
import com.farukylmz.createkiday.registry.ModCreativeTabs;
import com.farukylmz.createkiday.registry.ModFluids;
import com.farukylmz.createkiday.entity.HunterDiskEntity;
import com.farukylmz.createkiday.entity.LeiGongEntity;
import com.farukylmz.createkiday.bracelet.BraceletFlightService;
import com.farukylmz.createkiday.gameplay.StormAmuletEvents;
import com.farukylmz.createkiday.item.KidayChronicleItem;
import com.farukylmz.createkiday.item.TagFilterData;
import com.farukylmz.createkiday.registry.ModBiomes;
import com.farukylmz.createkiday.registry.ModDataComponents;
import com.farukylmz.createkiday.registry.ModDamageTypes;
import com.farukylmz.createkiday.registry.ModMobEffects;
import com.farukylmz.createkiday.registry.ModEntities;
import com.farukylmz.createkiday.recipe.PatternShapingDeployerRecipe;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
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
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import top.theillusivec4.curios.api.CuriosApi;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void tagFilterCombinesDynamicTagsAndIgnoresStackComponents(GameTestHelper helper) {
        ItemStack filter = ModItems.TAG_FILTER.get().getDefaultInstance();
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        planks.set(DataComponents.CUSTOM_NAME, Component.literal("Renamed planks"));

        filter.set(ModDataComponents.TAG_FILTER_RULES.get(), new TagFilterData(
                List.of("minecraft:planks", "minecraft:logs"), TagFilterData.MatchMode.ANY,
                TagFilterData.FilterMode.WHITELIST));
        helper.assertTrue(FilterItemStack.of(filter).test(helper.getLevel(), planks),
                "OR whitelist failed to match a tagged item with custom components");

        filter.set(ModDataComponents.TAG_FILTER_RULES.get(), new TagFilterData(
                List.of("minecraft:logs", "minecraft:logs_that_burn"), TagFilterData.MatchMode.ALL,
                TagFilterData.FilterMode.WHITELIST));
        helper.assertTrue(FilterItemStack.of(filter).test(helper.getLevel(), new ItemStack(Items.OAK_LOG)),
                "AND whitelist did not require and match all selected tags");
        helper.assertFalse(FilterItemStack.of(filter).test(helper.getLevel(), planks),
                "AND whitelist matched an item missing one or more selected tags");

        filter.set(ModDataComponents.TAG_FILTER_RULES.get(), new TagFilterData(
                List.of("minecraft:planks"), TagFilterData.MatchMode.ANY,
                TagFilterData.FilterMode.BLACKLIST));
        helper.assertFalse(FilterItemStack.of(filter).test(helper.getLevel(), planks),
                "Blacklist accepted an item in its selected tag");
        helper.assertTrue(FilterItemStack.of(filter).test(helper.getLevel(), new ItemStack(Items.STONE)),
                "Blacklist rejected an item outside its selected tags");
        helper.succeed();
    }

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
        ResourceLocation expected = ResourceLocation.fromNamespaceAndPath("create_kiday", "kiday");
        helper.assertTrue(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(ModCreativeTabs.KIDAY.get()).equals(expected),
                "The Create: Kiday creative tab is not registered");
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

    @GameTest(template = "empty", timeoutTicks = 40, batch = "hunter_tracking")
    public static void hunterDiskTracksCreeperDirectlyAhead(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "hunter-owner"));
        helper.getLevel().addNewPlayer(owner);
        owner.setPos(helper.absoluteVec(new Vec3(1.5, 1.1, 1.5)));
        owner.setYRot(0);
        owner.setXRot(0);
        var creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(1.5, 1.1, 5.5));
        HunterDiskEntity disk = new HunterDiskEntity(com.farukylmz.createkiday.registry.ModEntities.HUNTER_DISK.get(), helper.getLevel());
        disk.launch(owner, UUID.randomUUID(), UUID.randomUUID());
        disk.tick();
        helper.assertTrue(creeper.getUUID().equals(disk.navigationTargetId()),
                "The Creeper directly ahead was not selected as the hunt target: removed=" + disk.isRemoved()
                        + " fallback=" + disk.isHunterFallback() + " pos=" + disk.position());
        float healthBefore = creeper.getHealth();
        for (int tick = 0; tick < 80 && creeper.getHealth() == healthBefore; tick++) disk.tick();
        helper.assertTrue(creeper.getHealth() < healthBefore, "The Hunter Disk hit the Creeper without dealing damage");
        disk.discard();
        helper.getLevel().removePlayerImmediately(owner, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "hunter_tracking")
    public static void hunterDiskHitsLeiGongEightBlocksAhead(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "lei-gong-hunter"));
        helper.getLevel().addNewPlayer(owner);
        owner.setPos(helper.absoluteVec(new Vec3(1.5, 1.1, 1.5)));
        owner.setYRot(0);
        owner.setXRot(0);
        var leiGong = helper.spawnWithNoFreeWill(
                com.farukylmz.createkiday.registry.ModEntities.LEI_GONG.get(), new Vec3(1.5, 1.1, 9.5));
        HunterDiskEntity disk = new HunterDiskEntity(
                com.farukylmz.createkiday.registry.ModEntities.HUNTER_DISK.get(), helper.getLevel());
        disk.launch(owner, UUID.randomUUID(), UUID.randomUUID());
        float healthBefore = leiGong.getHealth();
        for (int tick = 0; tick < 100 && leiGong.getHealth() == healthBefore && !disk.isRemoved(); tick++) {
            disk.tick();
        }
        helper.assertTrue(leiGong.getHealth() < healthBefore,
                "The Hunter Disk failed to reach Lei Gong eight blocks ahead: target=" + disk.navigationTargetId()
                        + " fallback=" + disk.isHunterFallback() + " pos=" + disk.position()
                        + " removed=" + disk.isRemoved());
        disk.discard();
        helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 180, batch = "hunter_vertical")
    public static void hunterDiskBanksTowardTargetBelow(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "hunter-above-target"));
        helper.getLevel().addNewPlayer(owner);
        owner.setPos(helper.absoluteVec(new Vec3(3.5, 8.1, 3.5)));
        owner.setYRot(0);
        owner.setXRot(90);
        var creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(3.5, 1.1, 3.5));
        HunterDiskEntity disk = new HunterDiskEntity(
                com.farukylmz.createkiday.registry.ModEntities.HUNTER_DISK.get(), helper.getLevel());
        disk.launch(owner, UUID.randomUUID(), UUID.randomUUID());
        Vec3 launch = disk.position();
        disk.tick();
        double horizontalStep = new Vec3(disk.getX() - launch.x, 0, disk.getZ() - launch.z).length();
        helper.assertTrue(horizontalStep > 0.15,
                "The Hunter Disk dove vertically toward the target below without banking: " + horizontalStep);
        float healthBefore = creeper.getHealth();
        double closestDistance = disk.distanceTo(creeper);
        for (int tick = 0; tick < 160 && creeper.getHealth() == healthBefore && !disk.isRemoved(); tick++) {
            disk.tick();
            closestDistance = Math.min(closestDistance, disk.distanceTo(creeper));
        }
        helper.assertTrue(creeper.getHealth() < healthBefore,
                "The Hunter Disk banked toward the target below but did not hit it: closest="
                        + closestDistance + " pos=" + disk.position() + " target=" + disk.navigationTargetId()
                        + " fallback=" + disk.isHunterFallback() + " removed=" + disk.isRemoved());
        disk.discard();
        helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "hunter_fallback")
    public static void targetlessHunterFallbackUsesEllipse(GameTestHelper helper) {
        Vec3 origin = new Vec3(0, 0, 0);
        Vec3 forward = new Vec3(0, 0, 1);
        Vec3 right = new Vec3(-1, 0, 0);
        Vec3 outward = HunterDiskEntity.ellipsePoint(origin, forward, right, origin, 0.25);
        Vec3 farthest = HunterDiskEntity.ellipsePoint(origin, forward, right, origin, 0.5);
        Vec3 returned = HunterDiskEntity.ellipsePoint(origin, forward, right, origin, 1.0);
        helper.assertTrue(Math.abs(outward.x) > 1.9 && farthest.z > 15.9,
                "The targetless Hunter flight does not follow the reference elliptical outbound path");
        helper.assertTrue(returned.distanceToSqr(origin) < 1.0E-6,
                "The targetless Hunter ellipse does not return to its owner");
        int interpolationSteps = HunterDiskEntity.clientInterpolationSteps(3);
        Vec3 displayed = HunterDiskEntity.interpolationStep(Vec3.ZERO, new Vec3(6, 3, -3), interpolationSteps);
        helper.assertTrue(interpolationSteps == 1 && displayed.distanceToSqr(new Vec3(6, 3, -3)) < 1.0E-8,
                "The Hunter Disk does not consume each network sample before the next one arrives");
        Vec3 smoothed = HunterDiskEntity.smoothMovement(Vec3.ZERO, new Vec3(0, 0, 4),
                new Vec3(1, 0, 0), HunterDiskEntity.MAX_TURN_RADIANS);
        double turnAngle = Math.acos(Mth.clamp(smoothed.normalize().dot(new Vec3(1, 0, 0)), -1.0, 1.0));
        helper.assertTrue(turnAngle <= HunterDiskEntity.MAX_TURN_RADIANS + 0.01 && smoothed.z > 0,
                "The Hunter Disk does not smooth its turn angle when changing targets");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80, batch = "hunter_fallback")
    public static void targetlessHunterRemainsVisibleDuringOutboundArc(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fallback-owner"));
        helper.getLevel().addNewPlayer(owner);
        owner.setPos(helper.absoluteVec(new Vec3(2.5, 30.1, 2.5)));
        owner.setYRot(0);
        owner.setXRot(0);
        HunterDiskEntity disk = new HunterDiskEntity(
                com.farukylmz.createkiday.registry.ModEntities.HUNTER_DISK.get(), helper.getLevel());
        disk.launch(owner, UUID.randomUUID(), UUID.randomUUID());
        Vec3 launch = disk.position();
        double farthestDistance = 0;
        double smallestStep = Double.MAX_VALUE;
        Vec3 previous = launch;
        for (int tick = 0; tick < HunterDiskEntity.FALLBACK_FLIGHT_TICKS - 1; tick++) {
            disk.tick();
            helper.assertFalse(disk.isRemoved(),
                    "The targetless Hunter Disk disappeared before completing its full tour: tick=" + tick
                            + " pos=" + disk.position() + " flightTick=" + disk.flightTick()
                            + " stagnant=" + disk.stagnantTicks()
                            + " returning=" + disk.isReturningToOwner());
            smallestStep = Math.min(smallestStep, disk.position().distanceTo(previous));
            farthestDistance = Math.max(farthestDistance, disk.position().distanceTo(launch));
            previous = disk.position();
        }
        helper.assertTrue(farthestDistance > 7.5,
                "The targetless Hunter Disk returned without completing a visible tour: " + farthestDistance);
        helper.assertTrue(smallestStep > 0.05,
                "The targetless Hunter Disk stalled during its tour: " + smallestStep);
        for (int tick = 0; tick < 40 && !disk.isRemoved(); tick++) disk.tick();
        helper.assertTrue(disk.isRemoved(), "The targetless Hunter Disk did not return to its owner after the tour");
        helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "hunter_creative")
    public static void creativePlayerCanLaunchMultipleDisks(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "creative-hunter"));
        helper.getLevel().addNewPlayer(owner);
        owner.setPos(helper.absoluteVec(new Vec3(2.5, 1.1, 2.5)));
        owner.setGameMode(GameType.CREATIVE);
        ItemStack bracelet = ModItems.HUNTER_BRACELET.toStack();
        helper.assertTrue(BraceletFlightService.launchWithBracelet(owner, bracelet), "The first disk could not launch in Creative mode");
        helper.assertTrue(BraceletFlightService.launchWithBracelet(owner, bracelet), "The second Creative-mode disk waited for the first disk");
        int disks = helper.getLevel().getEntitiesOfClass(HunterDiskEntity.class, owner.getBoundingBox().inflate(4)).size();
        helper.assertTrue(disks == 2, "Creative mode did not create two simultaneous disks: " + disks);
        helper.getLevel().getEntitiesOfClass(HunterDiskEntity.class, owner.getBoundingBox().inflate(4)).forEach(Entity::discard);
        helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80, batch = "hunter_range")
    public static void hunterUsesReferenceRangeAndIgnoresPassiveMobs(GameTestHelper helper) {
        ServerPlayer owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "range-hunter"));
        helper.getLevel().addNewPlayer(owner);
        owner.setPos(helper.absoluteVec(new Vec3(1.5, 1.1, 1.5)));
        owner.setYRot(0);
        helper.spawnWithNoFreeWill(EntityType.COW, new Vec3(1.5, 1.1, 3.5));
        var creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new Vec3(1.5, 1.1, 13.5));
        HunterDiskEntity disk = new HunterDiskEntity(com.farukylmz.createkiday.registry.ModEntities.HUNTER_DISK.get(), helper.getLevel());
        disk.launch(owner, UUID.randomUUID(), UUID.randomUUID());
        disk.tick();
        helper.assertTrue(HunterDiskEntity.HUNTER_RANGE == 16.0,
                "The Hunter Bracelet target radius does not match the reference value of 16 blocks");
        helper.assertTrue(creeper.getUUID().equals(disk.navigationTargetId()),
                "The Hunter Disk did not find the enemy beyond 8 blocks or selected a nearby passive mob: removed="
                        + disk.isRemoved() + " fallback=" + disk.isHunterFallback() + " pos=" + disk.position());
        Vec3 firstStep = disk.position().subtract(owner.getEyePosition().add(0, -0.25, 0));
        helper.assertTrue(firstStep.normalize().dot(owner.getLookAngle().normalize()) > 0.99,
                "The Hunter Disk did not launch in the player's look direction before seeking a target");
        disk.discard();
        helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
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
                .setValue(com.farukylmz.createkiday.block.LightningEngineBlock.HORIZONTAL_FACING, net.minecraft.core.Direction.EAST);
        helper.setBlock(pos, state);
        var engine = (com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity) helper.getBlockEntity(pos);
        helper.assertTrue(engine.targetSpeed != null, "The Lightning Engine did not create a Create ScrollValue RPM control");
        helper.assertTrue(engine.targetSpeed.getClass()
                        == com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour.class,
                "The Lightning Engine RPM menu does not use the reference standard ScrollValueBehaviour");
        engine.targetSpeed.setValue(48);
        helper.assertTrue(engine.selectedRpm() == 48, "The Create RPM control did not apply the selected 48 RPM to the engine");
        helper.assertTrue(com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity.isRpmControlSide(
                        state, net.minecraft.core.Direction.WEST), "The RPM control is not on the engine's back face");
        helper.assertFalse(com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity.isRpmControlSide(
                        state, net.minecraft.core.Direction.EAST), "The RPM control is also active on the shaft's front face");
        var controlCenter = com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity.RPM_CONTROL_BACK_CENTER;
        helper.assertTrue(controlCenter.x == 0.5 && controlCenter.y == 0.5,
                "The RPM control is not centered on the back face");
        helper.assertTrue(ModBlocks.LIGHTNING_ENGINE.get().hasShaftTowards(helper.getLevel(), helper.absolutePos(pos), state,
                        net.minecraft.core.Direction.EAST), "The Lightning Engine does not expose a shaft on its front face");
        helper.assertFalse(ModBlocks.LIGHTNING_ENGINE.get().hasShaftTowards(helper.getLevel(), helper.absolutePos(pos), state,
                        net.minecraft.core.Direction.UP), "The Lightning Engine incorrectly exposes a shaft on top");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void lightningEngineRodAttractsAndConsumesRealLightning(GameTestHelper helper) {
        BlockPos relative = new BlockPos(3, 1, 3);
        helper.setBlock(relative, ModBlocks.LIGHTNING_ENGINE.get());
        var engine = (com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity) helper.getBlockEntity(relative);
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
        helper.assertTrue(leiGong.getMaxHealth() == 120.0F, "Lei Gong does not have 120 maximum health");
        helper.assertTrue(leiGong.hurt(helper.getLevel().damageSources().generic(), 1.0F)
                        && leiGong.getHealth() == 119.0F,
                "Lei Gong's 120 HP health attribute prevented normal damage");
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
    public static void stormGroveIsWideAndExclusivelySpawnsLeiGong(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        var stormGrove = biomes.getHolderOrThrow(ModBiomes.STORM_GROVE);
        helper.assertTrue(ModBiomes.STORM_GROVE_WIDTH >= 500,
                "Storm Grove regions are narrower than 500 blocks");
        helper.assertTrue(stormGrove.value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                        .anyMatch(spawn -> spawn.type == ModEntities.LEI_GONG.get()),
                "Storm Grove does not naturally spawn Lei Gong");
        long otherLeiGongBiomes = biomes.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(ModBiomes.STORM_GROVE))
                .filter(entry -> entry.getValue().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream()
                        .anyMatch(spawn -> spawn.type == ModEntities.LEI_GONG.get()))
                .count();
        helper.assertTrue(otherLeiGongBiomes == 0, "Lei Gong is configured to spawn outside Storm Grove");
        helper.assertTrue(com.farukylmz.createkiday.gameplay.StormGroveBiomeResolver.get() != null,
                "Storm Grove is not bound to the Overworld generator");
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(
                        ResourceLocation.fromNamespaceAndPath("create_kiday", "storm_amulet")).isEmpty(),
                "Storm Amulet unexpectedly has a crafting recipe");
        var egg = ModItems.LEI_GONG_SPAWN_EGG.get();
        helper.assertTrue(egg.getType(ModItems.LEI_GONG_SPAWN_EGG.toStack()) == ModEntities.LEI_GONG.get(),
                "Lei Gong spawn egg does not resolve to Lei Gong");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void leiGongCanDropStormAmuletAtFivePercentRoll(GameTestHelper helper) {
        LeiGongEntity leiGong = helper.spawn(ModEntities.LEI_GONG.get(), new Vec3(4.5, 1.1, 4.5));
        long successfulSeed = 0;
        while (RandomSource.create(successfulSeed).nextFloat() >= 0.05F) successfulSeed++;
        leiGong.getRandom().setSeed(successfulSeed);
        leiGong.kill();
        helper.runAtTickTime(2, () -> {
            helper.assertItemEntityPresent(ModItems.STORM_AMULET.get(), new BlockPos(4, 1, 4), 2.0);
            helper.succeed();
        });
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
                "lightning_engine", "kiday_chronicle", "hunter_bracelet",
                "kiday_sword", "kiday_pickaxe", "kiday_axe", "kiday_shovel", "kiday_hoe",
                "melting_raw_kiday", "pressing_kiday_sheet"));
        for (String mold : List.of("ingot", "sword_head", "pickaxe_head", "axe_head", "shovel_head", "hoe_head",
                "helmet", "chestplate", "leggings", "boots", "trapdoor", "bracelet_frame")) {
            required.add("casting_" + mold);
            required.add("mold_shaping_" + mold);
        }
        for (String hot : List.of("hot_kiday_ingot", "hot_kiday_sword_head", "hot_kiday_pickaxe_head", "hot_kiday_axe_head",
                "hot_kiday_shovel_head", "hot_kiday_hoe_head", "hot_kiday_helmet", "hot_kiday_chestplate",
                "hot_kiday_leggings", "hot_kiday_boots", "hot_kiday_trapdoor", "hot_bracelet_frame"))
            required.add("washing_" + hot);
        for (String id : required)
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(
                            ResourceLocation.fromNamespaceAndPath("create_kiday", id)).isPresent(),
                    "Missing crafting or processing recipe: " + id);
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

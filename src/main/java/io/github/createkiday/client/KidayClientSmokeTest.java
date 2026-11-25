package io.github.createkiday.client;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.block.entity.CastingTableBlockEntity;
import io.github.createkiday.registry.ModCreativeTabs;
import io.github.createkiday.registry.ModItems;
import io.github.createkiday.compat.jei.KidayJeiPlugin;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Opt-in rendered acceptance test: ./gradlew runClient -PkidayClientSmokeTest. */
@EventBusSubscriber(modid = CreateKidayMod.MOD_ID, value = Dist.CLIENT)
public final class KidayClientSmokeTest {
    private static boolean openingWorld;
    private static boolean verified;
    private static boolean capturePending;
    private static boolean bookCapturePending;
    private static boolean lightningCaptureScheduled;
    private static boolean lightningCapturePending;
    private static boolean leiGongCapturePending;
    private static boolean armorCapturePending;
    private static boolean moltenBucketHandCapturePending;
    private static boolean moltenBucketInventoryCapturePending;
    private static boolean kidayOreHandCapturePending;
    private static boolean biomassGeneratorCapturePending;
    private static int renderTicks;
    private static int stage;
    private static int bookWaitTicks;
    private static volatile boolean worldgenVerified;
    private static volatile String worldgenFailure;
    private static BlockPos countdownEnginePos;
    private static long initialClientCharge;
    private static float initialShaftAngle;

    private KidayClientSmokeTest() {
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("create_kiday.clientSmokeTest") || verified) return;
        Minecraft minecraft = Minecraft.getInstance();
        // The opt-in smoke test can run while the game window is not focused; keep
        // its integrated server ticking so scripted client/server render stages finish.
        minecraft.options.pauseOnLostFocus = false;
        if (!minecraft.isGameLoadFinished() || minecraft.getOverlay() != null) return;
        if (minecraft.level == null) {
            if (!openingWorld) {
                openingWorld = true;
                minecraft.createWorldOpenFlows().createFreshLevel("create-kiday-client-smoke-" + System.currentTimeMillis(),
                        new LevelSettings("Create Kiday Client QA", GameType.CREATIVE, false, Difficulty.NORMAL,
                                true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(1, false, false),
                        registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                                .getHolderOrThrow(WorldPresets.NORMAL).value().createWorldDimensions(), minecraft.screen);
            }
            return;
        }
        if (minecraft.player == null) return;

        try {
            if (stage == 0) {
                var server = minecraft.getSingleplayerServer();
                var playerId = minecraft.player.getUUID();
                if (server == null) return;
                server.execute(() -> {
                    var serverPlayer = server.getPlayerList().getPlayer(playerId);
                    if (serverPlayer == null) return;
                    serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ModItems.KIDAY_CHRONICLE.get().getDefaultInstance());
                    ModItems.KIDAY_CHRONICLE.get().use(serverPlayer.serverLevel(), serverPlayer, InteractionHand.MAIN_HAND);
                });
                stage = 1;
                return;
            }
            if (stage == 1) {
                if (!(minecraft.screen instanceof KidayChronicleScreen)) {
                    if (++bookWaitTicks > 100) throw new IllegalStateException("Chronicle network use did not open KidayChronicleScreen");
                    return;
                }
                if (!bookCapturePending) {
                    LogUtils.getLogger().info("KIDAY_CHRONICLE_SCREEN_PASS");
                    bookCapturePending = true;
                }
                return;
            }
            if (stage == 2) {
                var server = minecraft.getSingleplayerServer();
                var playerId = minecraft.player.getUUID();
                if (server == null) return;
                server.execute(() -> {
                    var serverPlayer = server.getPlayerList().getPlayer(playerId);
                    if (serverPlayer == null) return;
                    var registryAccess = serverPlayer.serverLevel().registryAccess();
                    var placedFeatures = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
                    var configuredFeatures = registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE);
                    var kidayOre = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "kiday_ore");
                    var salurOre = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "salur_ore");
                    boolean allFeaturesRegistered = placedFeatures.containsKey(kidayOre)
                            && placedFeatures.containsKey(salurOre)
                            && configuredFeatures.containsKey(kidayOre)
                            && configuredFeatures.containsKey(salurOre);
                    if (!allFeaturesRegistered) worldgenFailure = "Kiday or Salur ore worldgen feature is missing from registries";
                    else LogUtils.getLogger().info("KIDAY_SALUR_ORE_FEATURE_REGISTRIES_PASS");
                    worldgenVerified = true;
                    serverPlayer.setItemSlot(EquipmentSlot.HEAD, ModItems.KIDAY_HELMET.toStack());
                    serverPlayer.setItemSlot(EquipmentSlot.CHEST, ModItems.KIDAY_CHESTPLATE.toStack());
                    serverPlayer.setItemSlot(EquipmentSlot.LEGS, ModItems.KIDAY_LEGGINGS.toStack());
                    serverPlayer.setItemSlot(EquipmentSlot.FEET, ModItems.KIDAY_BOOTS.toStack());
                });
                stage = 3;
                bookWaitTicks = 0;
                return;
            }
            if (stage == 3) {
                if (!worldgenVerified) return;
                if (worldgenFailure != null) throw new IllegalStateException(worldgenFailure);
                boolean equipped = minecraft.player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.KIDAY_HELMET.get())
                        && minecraft.player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.KIDAY_CHESTPLATE.get())
                        && minecraft.player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.KIDAY_LEGGINGS.get())
                        && minecraft.player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.KIDAY_BOOTS.get());
                if (!equipped) {
                    if (++bookWaitTicks > 100) throw new IllegalStateException("Kiday armor did not synchronize to client player");
                    return;
                }
                minecraft.setScreen(new KidayArmorPreviewScreen());
                stage = 4;
                bookWaitTicks = 0;
                return;
            }
            if (stage == 4) {
                if (!(minecraft.screen instanceof KidayArmorPreviewScreen))
                    throw new IllegalStateException("Armor preview inventory screen closed unexpectedly");
                if (++bookWaitTicks >= 30) armorCapturePending = true;
                return;
            }
            if (stage == 5) {
                var server = minecraft.getSingleplayerServer();
                var playerId = minecraft.player.getUUID();
                if (server == null) return;
                countdownEnginePos = new BlockPos(4, 66, -2);
                server.execute(() -> {
                    var serverPlayer = server.getPlayerList().getPlayer(playerId);
                    if (serverPlayer == null) return;
                    for (int x = -5; x <= 6; x++) for (int z = -9; z <= 5; z++)
                        serverPlayer.serverLevel().setBlock(new BlockPos(x, 64, z),
                                net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                    serverPlayer.teleportTo(0.5, 65.0, -5.5);
                    serverPlayer.setYRot(0.0F);
                    serverPlayer.setXRot(0.0F);
                    var leiGong = new io.github.createkiday.entity.LeiGongEntity(
                            io.github.createkiday.registry.ModEntities.LEI_GONG.get(),
                            serverPlayer.serverLevel());
                    leiGong.setPos(-1.5, 65.0, 0.5);
                    leiGong.setYRot(180.0F);
                    leiGong.setYBodyRot(180.0F);
                    leiGong.setYHeadRot(180.0F);
                    leiGong.setNoAi(true);
                    leiGong.setNoGravity(true);
                    serverPlayer.serverLevel().addFreshEntity(leiGong);
                    var spirit = new io.github.createkiday.entity.StormSpiritEntity(
                            io.github.createkiday.registry.ModEntities.STORM_SPIRIT.get(),
                            serverPlayer.serverLevel());
                    spirit.setPos(1.5, 66.0, 0.5);
                    spirit.setYRot(180.0F);
                    spirit.setYBodyRot(180.0F);
                    spirit.setYHeadRot(180.0F);
                    spirit.setNoAi(true);
                    spirit.setNoGravity(true);
                    serverPlayer.serverLevel().addFreshEntity(spirit);
                    serverPlayer.serverLevel().setBlock(countdownEnginePos,
                            io.github.createkiday.registry.ModBlocks.LIGHTNING_ENGINE.get().defaultBlockState(), 3);
                    if (serverPlayer.serverLevel().getBlockEntity(countdownEnginePos)
                            instanceof io.github.createkiday.block.entity.LightningEngineBlockEntity engine)
                        engine.chargeFromLightning();
                });
                stage = 6;
                bookWaitTicks = 0;
                return;
            }
            if (stage == 6) {
                var blockEntity = minecraft.level.getBlockEntity(countdownEnginePos);
                if (!(blockEntity instanceof io.github.createkiday.block.entity.LightningEngineBlockEntity engine)
                        || engine.remainingChargeTicks() <= 0) {
                    if (++bookWaitTicks > 200) {
                        String observedBlockEntity = blockEntity == null ? "missing" : blockEntity.getClass().getName();
                        long observedCharge = blockEntity instanceof io.github.createkiday.block.entity.LightningEngineBlockEntity current
                                ? current.remainingChargeTicks() : -1;
                        throw new IllegalStateException("Charged Lightning Engine did not synchronize to client after "
                                + bookWaitTicks + " ticks; block=" + minecraft.level.getBlockState(countdownEnginePos)
                                + ", blockEntity=" + observedBlockEntity + ", charge=" + observedCharge);
                    }
                    return;
                }
                if (initialClientCharge == 0) {
                    if (!(minecraft.getBlockEntityRenderDispatcher().getRenderer(engine) instanceof LightningEngineRenderer))
                        throw new IllegalStateException("Lightning Engine has no stationary shaft renderer");
                    initialClientCharge = engine.remainingChargeTicks();
                    initialShaftAngle = com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe(
                            engine, engine.getBlockPos(),
                            engine.getBlockState().getValue(
                                    io.github.createkiday.block.LightningEngineBlock.HORIZONTAL_FACING).getAxis());
                    bookWaitTicks = 0;
                    return;
                }
                bookWaitTicks++;
                float currentShaftAngle = com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe(
                        engine, engine.getBlockPos(),
                        engine.getBlockState().getValue(
                                io.github.createkiday.block.LightningEngineBlock.HORIZONTAL_FACING).getAxis());
                boolean countdownAdvanced = engine.remainingChargeTicks() <= initialClientCharge - 20;
                boolean shaftRotatesWhilePowered = engine.getSpeed() != 0
                        && Math.abs(currentShaftAngle - initialShaftAngle) >= 0.01F;
                if (!countdownAdvanced || !shaftRotatesWhilePowered) {
                    if (bookWaitTicks > 120)
                        throw new IllegalStateException("Lightning Engine shaft did not rotate while powered: charge="
                                + engine.remainingChargeTicks() + "/" + initialClientCharge + ", speed="
                                + engine.getSpeed() + ", angle=" + currentShaftAngle + "/" + initialShaftAngle);
                    return;
                }
                LogUtils.getLogger().info("KIDAY_LIGHTNING_COUNTDOWN_PASS initial={} current={}",
                        initialClientCharge, engine.remainingChargeTicks());
                LogUtils.getLogger().info("KIDAY_LIGHTNING_ROTATING_SHAFT_RENDER_PASS speed={} angle={}",
                        engine.getSpeed(), currentShaftAngle);
                stage = 7;
                bookWaitTicks = 0;
                return;
            }
            if (stage == 7) {
                var previewArea = minecraft.player.getBoundingBox().inflate(12.0);
                var leiGong = minecraft.level.getEntitiesOfClass(
                        io.github.createkiday.entity.LeiGongEntity.class, previewArea).stream().findFirst();
                var spirit = minecraft.level.getEntitiesOfClass(
                        io.github.createkiday.entity.StormSpiritEntity.class, previewArea).stream().findFirst();
                if (leiGong.isEmpty() || spirit.isEmpty()) {
                    if (++bookWaitTicks > 100)
                        throw new IllegalStateException("Entity preview did not synchronize to the client");
                    return;
                }
                minecraft.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                        new net.minecraft.world.phys.Vec3(0.0, 66.0, 0.5));
                leiGongCapturePending = true;
                stage = 8;
                return;
            }
            if (stage == 8) {
                var server = minecraft.getSingleplayerServer();
                var playerId = minecraft.player.getUUID();
                if (server == null) return;
                BlockPos biomassPreviewPos = new BlockPos(0, 65, 0);
                server.execute(() -> {
                    var serverPlayer = server.getPlayerList().getPlayer(playerId);
                    if (serverPlayer == null) return;
                    var level = serverPlayer.serverLevel();
                    level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,
                            new net.minecraft.world.phys.AABB(-4, 63, -4, 5, 72, 5), entity ->
                                    entity instanceof io.github.createkiday.entity.LeiGongEntity
                                            || entity instanceof io.github.createkiday.entity.StormSpiritEntity)
                            .forEach(net.minecraft.world.entity.Entity::discard);
                    var state = io.github.createkiday.registry.ModBlocks.BIOMASS_GENERATOR.get()
                            .defaultBlockState().setValue(
                                    com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING,
                                    Direction.NORTH);
                    serverPlayer.serverLevel().setBlock(biomassPreviewPos, state, 3);
                    serverPlayer.teleportTo(0.5, 65.0, -3.5);
                    serverPlayer.setYRot(0.0F);
                    serverPlayer.setXRot(0.0F);
                    serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                });
                stage = 9;
                bookWaitTicks = 0;
                return;
            }
            if (stage == 9) {
                minecraft.options.hideGui = true;
                BlockPos biomassPreviewPos = new BlockPos(0, 65, 0);
                if (!minecraft.level.getBlockState(biomassPreviewPos).is(
                        io.github.createkiday.registry.ModBlocks.BIOMASS_GENERATOR.get())) {
                    if (++bookWaitTicks > 100)
                        throw new IllegalStateException("Biomass Generator preview block did not synchronize to the client");
                    return;
                }
                minecraft.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                        new net.minecraft.world.phys.Vec3(0.5, 65.7, 0.5));
                if (++bookWaitTicks >= 20) biomassGeneratorCapturePending = true;
                return;
            }
            if (!ModCreativeTabs.KIDAY.get().contains(ModItems.KIDAY_INGOT.toStack()))
                throw new IllegalStateException("The combined Kiday tab does not contain the cooled Kiday ingot");
            for (var hot : ModItems.HOT_PRODUCTS)
                if (!ModCreativeTabs.KIDAY.get().contains(hot.toStack()))
                    throw new IllegalStateException("The combined Kiday tab is missing hot product " + hot.getId());
            if (!KidayJeiPlugin.allRecipesRegistered())
                throw new IllegalStateException("JEI did not register every Casting and Pattern recipe");

            int loadedItemModels = 0;
            var modelManager = minecraft.getItemRenderer().getItemModelShaper().getModelManager();
            for (var item : BuiltInRegistries.ITEM) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (!itemId.getNamespace().equals(CreateKidayMod.MOD_ID)) continue;
                var model = minecraft.getItemRenderer().getModel(
                        item.getDefaultInstance(), minecraft.level, minecraft.player, loadedItemModels);
                if (model == modelManager.getMissingModel())
                    throw new IllegalStateException("Missing baked item model/texture for " + itemId);
                loadedItemModels++;
            }
            if (loadedItemModels < 50)
                throw new IllegalStateException("Expected all Kiday item models, loaded only " + loadedItemModels);
            LogUtils.getLogger().info("KIDAY_TEXTURE_MODELS_PASS {} item models", loadedItemModels);
            var biomass = io.github.createkiday.registry.ModBlocks.BIOMASS_GENERATOR.get();
            for (Direction facing : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
                var state = biomass.defaultBlockState().setValue(
                        com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING, facing);
                if (minecraft.getBlockRenderer().getBlockModel(state) == modelManager.getMissingModel())
                    throw new IllegalStateException("Missing Biomass Generator block model for facing " + facing);
            }
            LogUtils.getLogger().info("KIDAY_BIOMASS_BLOCK_MODEL_PASS all facings");
            var previewDisk = new io.github.createkiday.entity.SalurDiskEntity(
                    io.github.createkiday.registry.ModEntities.SALUR_DISK.get(), minecraft.level);
            if (!(minecraft.getEntityRenderDispatcher().getRenderer(previewDisk) instanceof SalurDiskRenderer))
                throw new IllegalStateException("Salur Disc renderer is not registered");
            var previewLeiGong = new io.github.createkiday.entity.LeiGongEntity(
                    io.github.createkiday.registry.ModEntities.LEI_GONG.get(), minecraft.level);
            if (!(minecraft.getEntityRenderDispatcher().getRenderer(previewLeiGong) instanceof LeiGongRenderer renderer)
                    || !renderer.getTextureLocation(previewLeiGong).getPath().equals("textures/entity/lei_gong.png"))
                throw new IllegalStateException("Lei Gong does not use the classic wide-arm player renderer and placeholder skin");
            var previewSpirit = new io.github.createkiday.entity.StormSpiritEntity(
                    io.github.createkiday.registry.ModEntities.STORM_SPIRIT.get(), minecraft.level);
            if (!(minecraft.getEntityRenderDispatcher().getRenderer(previewSpirit) instanceof StormSpiritRenderer spiritRenderer)
                    || !spiritRenderer.getTextureLocation(previewSpirit).getPath().equals("textures/entity/storm_spirit.png"))
                throw new IllegalStateException("Storm Spirit is missing its native Blaze renderer or texture");

            for (String item : new String[]{"casting_table", "blank_mold", "lightning_engine", "biomass_generator"}) {
                var scenes = PonderIndex.getSceneAccess().compile(id(item));
                if (scenes.size() != 1) throw new IllegalStateException("Expected one Ponder scene for " + item);
                var scene = scenes.getFirst();
                for (int replay = 0; replay < 2; replay++) {
                    scene.begin();
                    for (int tick = 0; tick < 650; tick++) scene.tick();
                    if (!scene.isFinished()) throw new IllegalStateException("Ponder scene did not finish: " + item);
                }
                if (item.equals("casting_table")) {
                    var fan = scene.getWorld().getBlockEntity(new BlockPos(8, 1, 5));
                    if (!(fan instanceof com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity encasedFan)
                            || encasedFan.getAirFlowDirection() != Direction.WEST)
                        throw new IllegalStateException("Bulk Washing Ponder fan is not blowing forward through water toward the casting");
                }
                LogUtils.getLogger().info("KIDAY_PONDER_PLAYBACK_PASS {}", item);
            }

            PonderUI ui = PonderUI.of(id("casting_table"));
            minecraft.setScreen(ui);
            ui.seekToTime(175);
            verified = true;
        } catch (Throwable failure) {
            LogUtils.getLogger().error("KIDAY_CLIENT_SMOKE_FAILED", failure);
            minecraft.stop();
            throw new IllegalStateException("Create: Kiday client smoke test failed", failure);
        }
    }

    @SubscribeEvent
    public static void waitForRenderedPonder(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("create_kiday.clientSmokeTest") || !verified || capturePending || lightningCapturePending) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (lightningCaptureScheduled) {
            if (!(minecraft.screen instanceof PonderUI) || ++renderTicks < 40) return;
            lightningCaptureScheduled = false;
            lightningCapturePending = true;
            return;
        }
        if (!(minecraft.screen instanceof PonderUI ui) || ++renderTicks < 40) return;
        var scene = ui.getActiveScene();
        var table = scene.getWorld().getBlockEntity(new BlockPos(4, 2, 5));
        if (!(table instanceof CastingTableBlockEntity castingTable)) return;
        if (castingTable.patternStack().isEmpty()) throw new IllegalStateException("Rendered Casting Table has no visible Pattern state");
        if (CastingTableRenderer.PATTERN_SIZE_PIXELS != 10
                || CastingTableRenderer.PATTERN_CENTER != 0.5
                || CastingTableRenderer.PATTERN_SCALE != 10.0F / 16.0F)
            throw new IllegalStateException("Casting Table Pattern is not an exact centered 10x10-pixel render");
        if (!(minecraft.getBlockEntityRenderDispatcher().getRenderer(castingTable) instanceof CastingTableRenderer))
            throw new IllegalStateException("Casting Table Pattern renderer is not registered");
        capturePending = true;
    }

    @SubscribeEvent
    public static void capture(RenderFrameEvent.Post event) {
        if (!capturePending && !bookCapturePending && !armorCapturePending && !lightningCapturePending
                && !leiGongCapturePending && !moltenBucketHandCapturePending
                && !moltenBucketInventoryCapturePending && !kidayOreHandCapturePending
                && !biomassGeneratorCapturePending) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (biomassGeneratorCapturePending) {
            biomassGeneratorCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-biomass-generator.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_BIOMASS_GENERATOR_RENDER_PASS screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save Biomass Generator render evidence", exception);
            }
            minecraft.options.hideGui = false;
            stage = 10;
            bookWaitTicks = 0;
            return;
        }
        if (leiGongCapturePending) {
            leiGongCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-lei-gong.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_LEI_GONG_RENDER_PASS screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save Lei Gong evidence", exception);
            }
            stage = 8;
            return;
        }
        if (moltenBucketHandCapturePending) {
            moltenBucketHandCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-molten-bucket-hand.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_MOLTEN_BUCKET_HAND_RENDER_PASS screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save molten bucket hand evidence", exception);
            }
            minecraft.setScreen(new InventoryScreen(minecraft.player));
            moltenBucketInventoryCapturePending = true;
            return;
        }
        if (moltenBucketInventoryCapturePending) {
            moltenBucketInventoryCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-molten-bucket-inventory.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_MOLTEN_BUCKET_INVENTORY_RENDER_PASS screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save molten bucket inventory evidence", exception);
            }
            minecraft.setScreen(null);
            minecraft.player.setItemInHand(InteractionHand.MAIN_HAND,
                    io.github.createkiday.registry.ModBlocks.DEEPSLATE_KIDAY_ORE_ITEM.get().getDefaultInstance());
            kidayOreHandCapturePending = true;
            return;
        }
        if (kidayOreHandCapturePending) {
            kidayOreHandCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-ore-hand.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_ORE_HAND_RENDER_PASS screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save Kiday ore hand evidence", exception);
            }
            minecraft.stop();
            return;
        }
        if (bookCapturePending) {
            bookCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-chronicle.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_CHRONICLE_RENDER_PASS screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save Kiday Chronicle evidence", exception);
            }
            minecraft.setScreen(null);
            stage = 2;
            return;
        }
        if (armorCapturePending) {
            armorCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-armor-preview.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_ARMOR_RENDER_PASS screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save Kiday armor evidence", exception);
            }
            minecraft.setScreen(null);
            stage = 5;
            return;
        }
        if (lightningCapturePending) {
            lightningCapturePending = false;
            try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                Path path = Path.of("screenshots", "kiday-lightning-engine.png");
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                LogUtils.getLogger().info("KIDAY_CLIENT_SMOKE_PASS book creative_tab textures ponder pattern_renderer engine_model countdown screenshot={}", path);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Could not save Lightning Engine evidence", exception);
            }
            minecraft.setScreen(null);
            minecraft.player.setItemInHand(InteractionHand.MAIN_HAND,
                    ModItems.MOLTEN_KIDAY_BUCKET.get().getDefaultInstance());
            moltenBucketHandCapturePending = true;
            return;
        }
        capturePending = false;
        try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
            Path path = Path.of("screenshots", "kiday-ponder-production-line.png");
            Files.createDirectories(path.getParent());
            image.writeToFile(path);
            LogUtils.getLogger().info("KIDAY_CASTING_RENDER_PASS screenshot={}", path);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not save Kiday client evidence", exception);
        }
        PonderUI engineUi = PonderUI.of(id("lightning_engine"));
        minecraft.setScreen(engineUi);
        engineUi.seekToTime(150);
        renderTicks = 0;
        lightningCaptureScheduled = true;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, path);
    }
}

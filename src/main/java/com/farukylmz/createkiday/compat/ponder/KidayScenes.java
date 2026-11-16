package com.farukylmz.createkiday.compat.ponder;

import com.farukylmz.createkiday.block.entity.CastingTableBlockEntity;
import com.farukylmz.createkiday.registry.ModBlocks;
import com.farukylmz.createkiday.registry.ModFluids;
import com.farukylmz.createkiday.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Ponder scenes laid out as one left-to-right production line. */
public final class KidayScenes {
    private KidayScenes() {
    }

    public static void productionLine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("production_line", "Kiday: Melt, Cast, Cool");
        scene.configureBasePlate(0, 0, 11);
        scene.scaleSceneView(0.65F);
        scene.showBasePlate();

        BlockPos basin = util.grid().at(1, 2, 5);
        BlockPos mixer = basin.above(2);
        BlockPos pump = util.grid().at(2, 2, 5);
        BlockPos pipe = util.grid().at(3, 2, 5);
        BlockPos table = util.grid().at(4, 2, 5);
        BlockPos hotDepot = util.grid().at(6, 1, 5);
        BlockPos water = util.grid().at(7, 1, 5);
        BlockPos fan = util.grid().at(8, 1, 5);

        scene.world().setBlock(basin.below(), AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL,
                        com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.world().setBlock(basin, AllBlocks.BASIN.getDefaultState(), false);
        scene.world().setBlock(mixer, AllBlocks.MECHANICAL_MIXER.getDefaultState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 5, 1, 4, 5), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(mixer), 32);
        scene.world().modifyBlockEntity(basin, com.simibubi.create.content.processing.basin.BasinBlockEntity.class,
                be -> be.getInputInventory().insertItem(0, ModItems.RAW_KIDAY.toStack(), false));
        scene.overlay().showControls(util.vector().topOf(basin), Pointing.DOWN, 60).withItem(ModItems.RAW_KIDAY.toStack());
        scene.overlay().showText(90).text("1. A heated Basin and a Mixer running at 32 RPM or more turn Raw Kiday into 144 mB of Molten Kiday. Superheat is not required.")
                .pointAt(util.vector().centerOf(basin)).placeNearTarget();
        scene.idle(100);
        scene.addKeyframe();

        scene.world().modifyBlockEntity(basin, com.simibubi.create.content.processing.basin.BasinBlockEntity.class,
                be -> be.inputTank.getPrimaryHandler().fill(new FluidStack(ModFluids.MOLTEN_KIDAY.get(), 144), IFluidHandler.FluidAction.EXECUTE));
        scene.world().setBlock(pump, AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.EAST), false);
        scene.world().setBlock(pipe, AllBlocks.FLUID_PIPE.getDefaultState()
                .setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.WEST, true), false);
        scene.world().setBlock(table, ModBlocks.CASTING_TABLE.get().defaultBlockState()
                .setValue(com.farukylmz.createkiday.block.CastingTableBlock.FACING, Direction.WEST), false);
        scene.world().showSection(util.select().fromTo(2, 2, 5, 4, 2, 5), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(pump), 32);
        scene.world().modifyBlockEntity(table, CastingTableBlockEntity.class,
                be -> be.insertMold(ModItems.SHAPED_MOLDS.getFirst().toStack()));
        scene.overlay().showControls(util.vector().topOf(table), Pointing.DOWN, 60)
                .rightClick().withItem(ModItems.SHAPED_MOLDS.getFirst().toStack());
        scene.overlay().showText(100).text("2. The Pattern appears on top of the table. The table is not a storage tank; it accepts only the amount required by the Pattern.")
                .pointAt(util.vector().topOf(table)).placeNearTarget();
        for (int portion = 0; portion < 4; portion++) {
            scene.world().modifyBlockEntity(table, CastingTableBlockEntity.class,
                    be -> be.fluidHandler().fill(new FluidStack(ModFluids.MOLTEN_KIDAY.get(), 36), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(15);
        }
        scene.idle(35);
        scene.addKeyframe();

        for (int tick = 0; tick < 20; tick++) {
            scene.world().modifyBlockEntity(table, CastingTableBlockEntity.class,
                    be -> CastingTableBlockEntity.serverTick(be.getLevel(), table, be.getBlockState(), be));
            scene.idle(1);
        }
        scene.world().setBlock(hotDepot, AllBlocks.DEPOT.getDefaultState(), false);
        scene.world().showSection(util.select().position(hotDepot), Direction.DOWN);
        scene.world().createItemOnBeltLike(hotDepot, Direction.NORTH, ModItems.HOT_PRODUCTS.getFirst().toStack());
        scene.overlay().showText(90).text("3. The cast output is still an uncooled Molten item; every Molten item has its own shape and texture.")
                .pointAt(util.vector().topOf(hotDepot)).placeNearTarget();
        scene.idle(100);
        scene.addKeyframe();

        scene.world().setBlock(water, Blocks.WATER.defaultBlockState(), false);
        scene.world().setBlock(fan, AllBlocks.ENCASED_FAN.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.WEST), false);
        scene.world().showSection(util.select().fromTo(7, 1, 5, 8, 1, 5), Direction.DOWN);
        // WEST is the visible front of this fan. A negative speed makes Create's
        // AirCurrent travel WEST: fan -> water -> hot casting. Positive speed
        // would pull EAST and must not be presented as Bulk Washing.
        scene.world().setKineticSpeed(util.select().position(fan), -32);
        scene.overlay().showText(90).text("4. The final left-to-right step is Bulk Washing: the fan blows through water and turns the Molten casting into a usable part.")
                .pointAt(util.vector().centerOf(water)).placeNearTarget();
        scene.idle(70);
        scene.world().removeItemsFromBelt(hotDepot);
        scene.world().createItemOnBeltLike(hotDepot, Direction.NORTH, ModItems.KIDAY_INGOT.toStack());
        scene.idle(45);
        scene.markAsFinished();
    }

    public static void patterns(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("patterns", "Preparing Patterns");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        BlockPos depot = util.grid().at(3, 1, 3);
        BlockPos deployer = util.grid().at(3, 3, 3);
        scene.world().setBlock(depot, AllBlocks.DEPOT.getDefaultState(), false);
        scene.world().setBlock(deployer, AllBlocks.DEPLOYER.getDefaultState().setValue(DirectionalKineticBlock.FACING, Direction.DOWN), false);
        scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 3, 3), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(deployer), 32);
        scene.world().createItemOnBeltLike(depot, Direction.NORTH, ModItems.BLANK_MOLD.toStack());
        scene.overlay().showControls(util.vector().centerOf(deployer), Pointing.RIGHT, 70)
                .withItem(ModItems.PICKAXE_HEAD.toStack());
        scene.overlay().showText(90).text("Give a Blank Pattern and a sample part to the Deployer. The sample is consumed and a uniquely marked Pattern is created.")
                .pointAt(util.vector().centerOf(deployer)).placeNearTarget();
        scene.idle(100);
        scene.world().moveDeployer(deployer, 1, 20);
        scene.idle(20);
        scene.world().removeItemsFromBelt(depot);
        scene.world().createItemOnBeltLike(depot, Direction.NORTH, ModItems.SHAPED_MOLDS.get(2).toStack());
        scene.world().moveDeployer(deployer, -1, 20);
        scene.idle(80);
        scene.markAsFinished();
    }

    public static void lightningEngine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("lightning_engine", "Lightning Engine");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();

        BlockPos engine = util.grid().at(3, 1, 3);
        BlockPos shaft = engine.east();
        scene.world().setBlock(engine, ModBlocks.LIGHTNING_ENGINE.get().defaultBlockState()
                .setValue(com.farukylmz.createkiday.block.LightningEngineBlock.HORIZONTAL_FACING, Direction.EAST), false);
        scene.world().setBlock(shaft, AllBlocks.SHAFT.getDefaultState()
                .setValue(com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS, Direction.Axis.X), false);
        scene.world().showSection(util.select().fromTo(engine, shaft), Direction.DOWN);
        scene.overlay().showText(90)
                .text("1. The lightning rod on top catches lightning and charges the engine for a limited time.")
                .pointAt(util.vector().topOf(engine)).placeNearTarget();
        scene.idle(100);
        scene.addKeyframe();

        scene.world().modifyBlockEntity(engine, com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity.class,
                com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity::chargeFromLightning);
        scene.world().setKineticSpeed(util.select().fromTo(engine, shaft), 16);
        scene.overlay().showText(90)
                .text("2. Kinetic power is taken from the visible shaft output on the block's front face.")
                .pointAt(util.vector().centerOf(shaft)).placeNearTarget();
        scene.idle(100);
        scene.addKeyframe();

        scene.overlay().showControls(util.vector().of(engine.getX() + 0.05, engine.getY() + 0.5, engine.getZ() + 0.5),
                Pointing.RIGHT, 70).scroll();
        scene.overlay().showText(100)
                .text("3. Look at the Create value box on the back face and scroll to select 1–64 RPM.")
                .pointAt(util.vector().of(engine.getX() + 0.05, engine.getY() + 0.5, engine.getZ() + 0.5)).placeNearTarget();
        scene.idle(110);
        scene.markAsFinished();
    }
}

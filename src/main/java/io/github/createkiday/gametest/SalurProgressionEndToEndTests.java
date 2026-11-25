package io.github.createkiday.gametest;

import io.github.createkiday.block.entity.CastingTableBlockEntity;
import io.github.createkiday.registry.ModBlocks;
import io.github.createkiday.registry.ModDataComponents;
import io.github.createkiday.registry.ModFluids;
import io.github.createkiday.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;

/** A real server-side path through Salur worldgen registration, melting and shared casting. */
@GameTestHolder("create_kiday_e2e")
public final class SalurProgressionEndToEndTests {
    private SalurProgressionEndToEndTests() { }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void rawSalurToMoltenSalurToTypedHotIngot(GameTestHelper helper) {
        var level = helper.getLevel();
        ResourceLocation salurFeature = ResourceLocation.fromNamespaceAndPath("create_kiday", "salur_ore");
        helper.assertTrue(level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).containsKey(salurFeature),
                "Salur ore is not available in world generation");
        helper.assertTrue(level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).containsKey(salurFeature),
                "Salur ore configured feature is missing");

        MixingRecipe melting = (MixingRecipe) level.getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath("create_kiday", "melting_raw_salur"))
                .orElseThrow(() -> new AssertionError("Raw Salur melting recipe is missing")).value();
        BlockPos basinPos = new BlockPos(2, 2, 3);
        helper.setBlock(basinPos, AllBlocks.BASIN.get());
        BasinBlockEntity basin = helper.getBlockEntity(basinPos);
        basin.getInputInventory().insertItem(0, ModItems.RAW_SALUR.toStack(), false);
        helper.setBlock(basinPos.below(), AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED));
        basin.tick();
        helper.assertTrue(BasinRecipe.apply(basin, melting), "Raw Salur did not melt in a heated Create Basin");
        var basinFluid = level.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(basinPos), null);
        helper.assertTrue(basinFluid != null, "The heated Basin has no fluid output");
        FluidStack molten = basinFluid.drain(144, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(molten.is(ModFluids.MOLTEN_SALUR.get()) && molten.getAmount() == 144,
                "Raw Salur did not produce exactly one ingot volume of Molten Salur");

        BlockPos tablePos = new BlockPos(5, 1, 3);
        helper.setBlock(tablePos, ModBlocks.CASTING_TABLE.get());
        CastingTableBlockEntity table = helper.getBlockEntity(tablePos);
        helper.assertTrue(table.insertMold(new net.minecraft.world.item.ItemStack(ModItems.SHAPED_MOLDS.getFirst().get())),
                "The shared Ingot Mold could not be installed");
        helper.assertTrue(table.fluidHandler().fill(molten, IFluidHandler.FluidAction.EXECUTE) == 144,
                "The casting table rejected the matching Salur fluid");
        helper.runAtTickTime(25, () -> {
            var hotIngot = table.outputHandler().getStackInSlot(0);
            helper.assertTrue(hotIngot.is(ModItems.HOT_PRODUCTS.getFirst().get()),
                    "The casting table did not produce the shared hot ingot");
            helper.assertTrue("salur".equals(hotIngot.get(ModDataComponents.METAL_TYPE.get())),
                    "The Salur identity was lost during melting and casting");
            helper.assertTrue(table.tank().isEmpty() && table.patternStack().getDamageValue() == 1,
                    "The casting did not consume its exact fluid amount or wear the mold exactly once");
            helper.succeed();
        });
    }
}

package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.fluid.MoltenKidayFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreateKidayMod.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, CreateKidayMod.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_KIDAY_TYPE = TYPES.register("molten_kiday", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid_type.create_kiday.molten_kiday").density(7_800).viscosity(6_000).temperature(1_300)
            .lightLevel(15).canSwim(false).canDrown(false).supportsBoating(false)));

    public static final DeferredHolder<Fluid, MoltenKidayFluid.Source> MOLTEN_KIDAY = FLUIDS.register("molten_kiday", MoltenKidayFluid.Source::new);
    public static final DeferredHolder<Fluid, MoltenKidayFluid.Flowing> FLOWING_MOLTEN_KIDAY = FLUIDS.register("flowing_molten_kiday", MoltenKidayFluid.Flowing::new);

    private ModFluids() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
    }
}

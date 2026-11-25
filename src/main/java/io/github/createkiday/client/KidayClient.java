package io.github.createkiday.client;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.registry.ModFluids;
import io.github.createkiday.registry.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import io.github.createkiday.registry.ModEntities;
import io.github.createkiday.registry.ModBlockEntities;
import io.github.createkiday.compat.ponder.KidayPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KidayClient {
    private KidayClient() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "block/molten_kiday_still");
            private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "block/molten_kiday_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF;
            }
        }, ModFluids.MOLTEN_KIDAY_TYPE.get());
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "block/molten_salur_still");
            private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "block/molten_salur_flow");
            @Override public ResourceLocation getStillTexture() { return STILL; }
            @Override public ResourceLocation getFlowingTexture() { return FLOW; }
            @Override public int getTintColor() { return 0xFFFFFFFF; }
        }, ModFluids.MOLTEN_SALUR_TYPE.get());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SALUR_DISK.get(), SalurDiskRenderer::new);
        event.registerEntityRenderer(ModEntities.LEI_GONG.get(), LeiGongRenderer::new);
        event.registerEntityRenderer(ModEntities.STORM_SPIRIT.get(), StormSpiritRenderer::new);
        event.registerEntityRenderer(ModEntities.WAQWAQ_BOAT.get(), context -> new WaqwaqBoatRenderer(context, false));
        event.registerEntityRenderer(ModEntities.WAQWAQ_CHEST_BOAT.get(), context -> new WaqwaqBoatRenderer(context, true));
        event.registerBlockEntityRenderer(ModBlockEntities.CASTING_TABLE.get(), CastingTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIGHTNING_ENGINE.get(), LightningEngineRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new WaqwaqBoatTextures());
    }


    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new KidayPonderPlugin());
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.KIDAY_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.KIDAY_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WAQWAQ_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WAQWAQ_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WAQWAQ_FRUIT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.MOLTEN_KIDAY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_MOLTEN_KIDAY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.MOLTEN_SALUR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_MOLTEN_SALUR.get(), RenderType.translucent());
        });
    }
}

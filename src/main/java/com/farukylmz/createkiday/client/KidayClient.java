package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.registry.ModFluids;
import com.farukylmz.createkiday.registry.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import com.farukylmz.createkiday.registry.ModEntities;
import com.farukylmz.createkiday.registry.ModBlockEntities;
import com.farukylmz.createkiday.compat.ponder.KidayPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KidayClient {
    public static final KeyMapping LAUNCH_DISK = new KeyMapping("key.create_kiday.launch_disk", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.create_kiday");
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
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(LAUNCH_DISK);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HUNTER_DISK.get(), HunterDiskRenderer::new);
        event.registerEntityRenderer(ModEntities.LEI_GONG.get(), LeiGongRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CASTING_TABLE.get(), CastingTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIGHTNING_ENGINE.get(), LightningEngineRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new KidayPonderPlugin());
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.KIDAY_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.KIDAY_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.MOLTEN_KIDAY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_MOLTEN_KIDAY.get(), RenderType.translucent());
        });
    }
}

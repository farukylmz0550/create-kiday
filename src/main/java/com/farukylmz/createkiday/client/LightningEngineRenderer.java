package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.block.LightningEngineBlock;
import com.farukylmz.createkiday.block.entity.LightningEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/** Renders Create's half-shaft, extended one pixel past the engine face, while the engine is powered. */
public final class LightningEngineRenderer extends KineticBlockEntityRenderer<LightningEngineBlockEntity> {
    public LightningEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(LightningEngineBlockEntity engine, float partialTicks, PoseStack pose,
                              MultiBufferSource buffers, int light, int overlay) {
        Direction output = engine.getBlockState().getValue(LightningEngineBlock.HORIZONTAL_FACING);
        var shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, engine.getBlockState(), output);
        shaft.translate(output.getStepX() / 16.0, output.getStepY() / 16.0, output.getStepZ() / 16.0);
        renderRotatingBuffer(engine, shaft, pose, buffers.getBuffer(RenderType.solid()), light);
    }
}

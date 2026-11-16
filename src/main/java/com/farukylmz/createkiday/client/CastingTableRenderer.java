package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.block.entity.CastingTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the exact installed Pattern flat on the Casting Table's top socket. */
public final class CastingTableRenderer implements BlockEntityRenderer<CastingTableBlockEntity> {
    public static final int PATTERN_SIZE_PIXELS = 10;
    public static final float PATTERN_SCALE = PATTERN_SIZE_PIXELS / 16.0F;
    public static final double PATTERN_CENTER = 0.5;
    public static final double PATTERN_SURFACE_Y = 1.025;
    private final net.minecraft.client.renderer.entity.ItemRenderer itemRenderer;

    public CastingTableRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CastingTableBlockEntity table, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack pattern = table.patternStack();
        if (pattern.isEmpty() || table.getLevel() == null) return;
        poseStack.pushPose();
        poseStack.translate(PATTERN_CENTER, PATTERN_SURFACE_Y, PATTERN_CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(PATTERN_SCALE, PATTERN_SCALE, PATTERN_SCALE);
        itemRenderer.renderStatic(pattern, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffers, table.getLevel(), 0);
        poseStack.popPose();
    }
}

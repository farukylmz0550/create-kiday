package com.farukylmz.createkiday.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.farukylmz.createkiday.item.WaqwaqBoatItem;

public final class WaqwaqBoatItemRenderer extends BlockEntityWithoutLevelRenderer {
    public WaqwaqBoatItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        boolean chest = stack.getItem() instanceof WaqwaqBoatItem item && item.hasChest();
        var texture = chest ? WaqwaqBoatTextures.CHEST_BOAT_ITEM_ID : WaqwaqBoatTextures.BOAT_ITEM_ID;
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutout(texture));
        var matrix = pose.last().pose();
        float size = 0.5F;
        quad(out, matrix, -size, -size, size, size, light, overlay);
        quad(out, matrix, size, -size, -size, size, light, overlay);
    }

    private static void quad(VertexConsumer out, org.joml.Matrix4f m, float l, float b, float r, float t,
            int light, int overlay) {
        out.addVertex(m,l,b,0).setColor(255,255,255,255).setUv(0,1).setOverlay(overlay).setLight(light).setNormal(0,0,1);
        out.addVertex(m,r,b,0).setColor(255,255,255,255).setUv(1,1).setOverlay(overlay).setLight(light).setNormal(0,0,1);
        out.addVertex(m,r,t,0).setColor(255,255,255,255).setUv(1,0).setOverlay(overlay).setLight(light).setNormal(0,0,1);
        out.addVertex(m,l,t,0).setColor(255,255,255,255).setUv(0,0).setOverlay(overlay).setLight(light).setNormal(0,0,1);
    }
}

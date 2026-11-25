package io.github.createkiday.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.createkiday.item.WaqwaqBoatItem;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class WaqwaqBoatItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final BoatModel boatModel;
    private final ChestBoatModel chestBoatModel;

    public WaqwaqBoatItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
        boatModel = new BoatModel(models.bakeLayer(ModelLayers.createBoatModelName(Boat.Type.OAK)));
        chestBoatModel = new ChestBoatModel(models.bakeLayer(ModelLayers.createChestBoatModelName(Boat.Type.OAK)));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        boolean chest = stack.getItem() instanceof WaqwaqBoatItem item && item.hasChest();
        var texture = chest ? WaqwaqBoatTextures.CHEST_BOAT_ITEM_ID : WaqwaqBoatTextures.BOAT_ITEM_ID;
        boolean inHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        if (!inHand) {
            VertexConsumer out = buffers.getBuffer(RenderType.entityCutout(texture));
            quad(out, pose, 0, 0, 1, 1, light, overlay);
            quad(out, pose, 1, 0, 0, 1, light, overlay);
            return;
        }

        ListModel<Boat> model = chest ? chestBoatModel : boatModel;
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(38.0F));
        pose.mulPose(Axis.XP.rotationDegrees(22.0F));
        pose.scale(-0.36F, -0.36F, 0.36F);
        model.renderToBuffer(pose, buffers.getBuffer(model.renderType(texture)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    private static void quad(VertexConsumer out, PoseStack pose, float left, float bottom, float right, float top,
            int light, int overlay) {
        var matrix = pose.last().pose();
        out.addVertex(matrix, left, bottom, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        out.addVertex(matrix, right, bottom, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        out.addVertex(matrix, right, top, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        out.addVertex(matrix, left, top, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }
}

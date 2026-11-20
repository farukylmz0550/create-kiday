package com.farukylmz.createkiday.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

public final class WaqwaqBoatRenderer extends EntityRenderer<Boat> {
    private final ListModel<Boat> model;
    private final ResourceLocation texture;

    public WaqwaqBoatRenderer(EntityRendererProvider.Context context, boolean hasChest) {
        super(context);
        var layer = hasChest ? ModelLayers.createChestBoatModelName(Boat.Type.OAK)
                : ModelLayers.createBoatModelName(Boat.Type.OAK);
        this.model = hasChest ? new ChestBoatModel(context.bakeLayer(layer)) : new BoatModel(context.bakeLayer(layer));
        this.texture = hasChest ? WaqwaqBoatTextures.CHEST_BOAT_ID : WaqwaqBoatTextures.BOAT_ID;
        this.shadowRadius = 0.8F;
    }

    @Override
    public void render(Boat boat, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        float hurtTime = boat.getHurtTime() - partialTick;
        float damage = boat.getDamage() - partialTick;
        if (damage < 0.0F) damage = 0.0F;
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * boat.getHurtDir()));
        }

        float bubbleAngle = boat.getBubbleAngle(partialTick);
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            poseStack.mulPose(new Quaternionf().setAngleAxis(bubbleAngle * ((float)Math.PI / 180.0F), 1.0F, 0.0F, 1.0F));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        model.setupAnim(boat, partialTick, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertices = bufferSource.getBuffer(model.renderType(texture));
        model.renderToBuffer(poseStack, vertices, packedLight, OverlayTexture.NO_OVERLAY);
        if (!boat.isUnderWater() && model instanceof WaterPatchModel waterPatchModel) {
            waterPatchModel.waterPatch().render(poseStack, bufferSource.getBuffer(RenderType.waterMask()),
                    packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(boat, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Boat entity) {
        return texture;
    }
}

package io.github.createkiday.client;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.entity.SalurDiskEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class SalurDiskRenderer extends EntityRenderer<SalurDiskEntity> {
    private static final ResourceLocation SALUR = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "textures/item/salur_disc.png");
    private static final ResourceLocation TNT = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "textures/item/tnt_disc.png");

    public SalurDiskRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.15F; }

    @Override protected int getBlockLightLevel(SalurDiskEntity entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }

    @Override public void render(SalurDiskEntity entity, float yaw, float partial,
            PoseStack poseStack, MultiBufferSource buffers, int light) {
        ResourceLocation texture = entity.isExplosive() ? TNT : SALUR;
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        poseStack.pushPose();
        poseStack.scale(0.75F, 0.75F, 0.75F);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partial) * 35.0F));
        PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, light, -0.5F, 0.04F, -0.5F, 0, 0, 0, 1, 0);
        vertex(consumer, pose, light, -0.5F, 0.04F, 0.5F, 0, 1, 0, 1, 0);
        vertex(consumer, pose, light, 0.5F, 0.04F, 0.5F, 1, 1, 0, 1, 0);
        vertex(consumer, pose, light, 0.5F, 0.04F, -0.5F, 1, 0, 0, 1, 0);
        vertex(consumer, pose, light, 0.5F, -0.04F, -0.5F, 1, 0, 0, -1, 0);
        vertex(consumer, pose, light, 0.5F, -0.04F, 0.5F, 1, 1, 0, -1, 0);
        vertex(consumer, pose, light, -0.5F, -0.04F, 0.5F, 0, 1, 0, -1, 0);
        vertex(consumer, pose, light, -0.5F, -0.04F, -0.5F, 0, 0, 0, -1, 0);
        poseStack.popPose();
        super.render(entity, yaw, partial, poseStack, buffers, light);
    }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, int light,
            float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        out.addVertex(pose, x, y, z).setColor(-1).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    @Override public ResourceLocation getTextureLocation(SalurDiskEntity entity) { return entity.isExplosive() ? TNT : SALUR; }
}

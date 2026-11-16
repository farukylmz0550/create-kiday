package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.entity.HunterDiskEntity;
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

/** Renders the entity texture directly, without registering a collectible disk item. */
public final class HunterDiskRenderer extends EntityRenderer<HunterDiskEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CreateKidayMod.MOD_ID, "textures/item/hunter_disk.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    public HunterDiskRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.2F;
    }

    @Override
    protected int getBlockLightLevel(HunterDiskEntity entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public void render(HunterDiskEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.75F, 0.75F, 0.75F);
        // The disc exists in the world on a horizontal plane. A first-person
        // camera therefore sees its thin edge, while an elevated third-person
        // camera sees the complete, naturally tilted top face.
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTicks) * 32.0F));
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertices = buffer.getBuffer(RENDER_TYPE);
        // Top and bottom faces.
        vertex(vertices, pose, packedLight, -0.5F, 0.04F, -0.5F, 0, 0, 0, 1, 0);
        vertex(vertices, pose, packedLight, -0.5F, 0.04F, 0.5F, 0, 1, 0, 1, 0);
        vertex(vertices, pose, packedLight, 0.5F, 0.04F, 0.5F, 1, 1, 0, 1, 0);
        vertex(vertices, pose, packedLight, 0.5F, 0.04F, -0.5F, 1, 0, 0, 1, 0);
        vertex(vertices, pose, packedLight, 0.5F, -0.04F, -0.5F, 1, 0, 0, -1, 0);
        vertex(vertices, pose, packedLight, 0.5F, -0.04F, 0.5F, 1, 1, 0, -1, 0);
        vertex(vertices, pose, packedLight, -0.5F, -0.04F, 0.5F, 0, 1, 0, -1, 0);
        vertex(vertices, pose, packedLight, -0.5F, -0.04F, -0.5F, 0, 0, 0, -1, 0);
        // A narrow rim remains visible when the top face is edge-on in first person.
        rim(vertices, pose, packedLight, -0.5F, -0.5F, 0.5F, -0.5F, 0, 0, -1);
        rim(vertices, pose, packedLight, 0.5F, 0.5F, -0.5F, 0.5F, 0, 0, 1);
        rim(vertices, pose, packedLight, -0.5F, 0.5F, -0.5F, -0.5F, -1, 0, 0);
        rim(vertices, pose, packedLight, 0.5F, -0.5F, 0.5F, 0.5F, 1, 0, 0);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void rim(VertexConsumer consumer, PoseStack.Pose pose, int light,
                            float x0, float z0, float x1, float z1, float nx, float ny, float nz) {
        vertex(consumer, pose, light, x0, -0.04F, z0, 0, 0.45F, nx, ny, nz);
        vertex(consumer, pose, light, x1, -0.04F, z1, 1, 0.45F, nx, ny, nz);
        vertex(consumer, pose, light, x1, 0.04F, z1, 1, 0.55F, nx, ny, nz);
        vertex(consumer, pose, light, x0, 0.04F, z0, 0, 0.55F, nx, ny, nz);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(HunterDiskEntity entity) {
        return TEXTURE;
    }
}

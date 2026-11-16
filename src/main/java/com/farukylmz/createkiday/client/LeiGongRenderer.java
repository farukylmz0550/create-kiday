package com.farukylmz.createkiday.client;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.entity.LeiGongEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LeiGongRenderer extends MobRenderer<LeiGongEntity, PlayerModel<LeiGongEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CreateKidayMod.MOD_ID, "textures/entity/lei_gong.png");

    public LeiGongRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(LeiGongEntity entity) {
        return TEXTURE;
    }
}

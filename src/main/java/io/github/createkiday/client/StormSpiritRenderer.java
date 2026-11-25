package io.github.createkiday.client;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.entity.StormSpiritEntity;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Uses the vanilla Blaze geometry and animation with the mod's LeiGong-palette texture. */
public final class StormSpiritRenderer extends MobRenderer<StormSpiritEntity, BlazeModel<StormSpiritEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CreateKidayMod.MOD_ID, "textures/entity/storm_spirit.png");

    public StormSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel<>(context.bakeLayer(ModelLayers.BLAZE)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(StormSpiritEntity entity) {
        return TEXTURE;
    }
}

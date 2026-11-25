package io.github.createkiday.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class WaqwaqBoatItemClientExtension implements IClientItemExtensions {
    public static final WaqwaqBoatItemClientExtension INSTANCE = new WaqwaqBoatItemClientExtension();
    private static WaqwaqBoatItemRenderer renderer;

    private WaqwaqBoatItemClientExtension() {}

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (renderer == null) {
            Minecraft minecraft = Minecraft.getInstance();
            renderer = new WaqwaqBoatItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        }
        return renderer;
    }
}

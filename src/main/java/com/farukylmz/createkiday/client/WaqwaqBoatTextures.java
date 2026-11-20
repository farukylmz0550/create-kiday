package com.farukylmz.createkiday.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.farukylmz.createkiday.CreateKidayMod;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Loads the current vanilla boat textures and tints them without bundling Mojang assets. */
public final class WaqwaqBoatTextures extends SimplePreparableReloadListener<WaqwaqBoatTextures.Pair> {
    // A light, muted mauve-gray glaze based on waqwaq_planks.png.
    public static final int TINT = 0xE2BFC0;
    public static final ResourceLocation BOAT_ID = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "waqwaq_boat_tinted");
    public static final ResourceLocation CHEST_BOAT_ID = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "waqwaq_chest_boat_tinted");
    public static final ResourceLocation BOAT_ITEM_ID = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "waqwaq_boat_item_tinted");
    public static final ResourceLocation CHEST_BOAT_ITEM_ID = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "waqwaq_chest_boat_item_tinted");
    private static final ResourceLocation BOAT = ResourceLocation.withDefaultNamespace("textures/entity/boat/oak.png");
    private static final ResourceLocation CHEST_BOAT = ResourceLocation.withDefaultNamespace("textures/entity/chest_boat/oak.png");
    private static final ResourceLocation BOAT_ITEM = ResourceLocation.withDefaultNamespace("textures/item/oak_boat.png");
    private static final ResourceLocation CHEST_BOAT_ITEM = ResourceLocation.withDefaultNamespace("textures/item/oak_chest_boat.png");

    record Pair(NativeImage boat, NativeImage chestBoat, NativeImage boatItem, NativeImage chestBoatItem) {}

    @Override protected Pair prepare(ResourceManager manager, ProfilerFiller profiler) {
        try (var boatStream = manager.getResourceOrThrow(BOAT).open();
             var chestStream = manager.getResourceOrThrow(CHEST_BOAT).open();
             var boatItemStream = manager.getResourceOrThrow(BOAT_ITEM).open();
             var chestItemStream = manager.getResourceOrThrow(CHEST_BOAT_ITEM).open()) {
            return new Pair(NativeImage.read(boatStream), NativeImage.read(chestStream),
                    NativeImage.read(boatItemStream), NativeImage.read(chestItemStream));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load vanilla boat textures", exception);
        }
    }

    @Override protected void apply(Pair textures, ResourceManager manager, ProfilerFiller profiler) {
        Minecraft minecraft = Minecraft.getInstance();
        try {
            tintBoatParts(textures.boat());
            tintBoatParts(textures.chestBoat());
            tintItem(textures.boatItem(), false);
            tintItem(textures.chestBoatItem(), true);
            minecraft.getTextureManager().release(BOAT_ID);
            minecraft.getTextureManager().release(CHEST_BOAT_ID);
            minecraft.getTextureManager().release(BOAT_ITEM_ID);
            minecraft.getTextureManager().release(CHEST_BOAT_ITEM_ID);
            minecraft.getTextureManager().register(BOAT_ID, new DynamicTexture(textures.boat()));
            minecraft.getTextureManager().register(CHEST_BOAT_ID, new DynamicTexture(textures.chestBoat()));
            minecraft.getTextureManager().register(BOAT_ITEM_ID, new DynamicTexture(textures.boatItem()));
            minecraft.getTextureManager().register(CHEST_BOAT_ITEM_ID, new DynamicTexture(textures.chestBoatItem()));
        } catch (RuntimeException exception) {
            textures.boat().close();
            textures.chestBoat().close();
            textures.boatItem().close();
            textures.chestBoatItem().close();
            minecraft.getTextureManager().release(BOAT_ID);
            minecraft.getTextureManager().release(CHEST_BOAT_ID);
            minecraft.getTextureManager().release(BOAT_ITEM_ID);
            minecraft.getTextureManager().release(CHEST_BOAT_ITEM_ID);
            throw new IllegalStateException("Unable to create tinted Waqwaq boat textures", exception);
        }
    }

    private static void tintBoatParts(NativeImage image) {
        if (image.getWidth() % 128 != 0 || image.getHeight() < image.getWidth() / 2)
            throw new IllegalStateException("Unexpected vanilla boat texture dimensions");
        int scale = image.getWidth() / 128;
        if (scale < 1 || image.getHeight() % (64 * scale) != 0)
            throw new IllegalStateException("Vanilla boat texture has a nonuniform scale");
        tint(image, 0, 0, 62, 19, scale);  // bottom
        tint(image, 0, 19, 40, 8, scale);  // back
        tint(image, 0, 27, 36, 8, scale);  // front
        tint(image, 0, 35, 60, 8, scale);  // sides
        tint(image, 0, 43, 60, 8, scale);  // opposite side
        tint(image, 62, 0, 40, 20, scale);  // left paddle and blade
        tint(image, 62, 20, 40, 20, scale); // right paddle and blade
    }

    private static void tintItem(NativeImage image, boolean chestBoat) {
        if (image.getWidth() != image.getHeight() || image.getWidth() % 16 != 0)
            throw new IllegalStateException("Unexpected vanilla boat item texture dimensions");
        int scale = image.getWidth() / 16;
        int startY = chestBoat ? 9 : 0;
        int endY = chestBoat ? 16 : 16;
        int redTint = TINT >>> 16 & 255;
        int greenTint = TINT >>> 8 & 255;
        int blueTint = TINT & 255;
        for (int y = startY * scale; y < endY * scale; y++) {
            for (int x = 0; x < 16 * scale; x++) {
                int pixel = image.getPixelRGBA(x, y);
                int alpha = pixel >>> 24;
                if (alpha == 0) continue;

                int sourceRed = pixel & 255;
                int sourceGreen = pixel >>> 8 & 255;
                int sourceBlue = pixel >>> 16 & 255;
                int luminance = (sourceRed * 54 + sourceGreen * 183 + sourceBlue * 19) >> 8;
                float shade = luminance / 128.0F;
                int red = Math.min(255, Math.round(redTint * shade));
                int green = Math.min(255, Math.round(greenTint * shade));
                int blue = Math.min(255, Math.round(blueTint * shade));
                image.setPixelRGBA(x, y, alpha << 24 | blue << 16 | green << 8 | red);
            }
        }
    }

    private static void tint(NativeImage image, int u, int v, int width, int height, int scale) {
        // Re-map the source luminance into the muted rose-gray waqwaq-planks palette.
        // This keeps the oak texture's shading while avoiding a barely visible pink multiply.
        for (int y = v * scale; y < (v + height) * scale; y++) for (int x = u * scale; x < (u + width) * scale; x++) {
            int pixel = image.getPixelRGBA(x, y);
            int alpha = pixel >>> 24;
            if (alpha == 0) continue;
            int sourceRed = pixel & 255;
            int sourceGreen = (pixel >>> 8) & 255;
            int sourceBlue = (pixel >>> 16) & 255;
            int luminance = (sourceRed * 54 + sourceGreen * 183 + sourceBlue * 19) >> 8;
            int red = Math.min(255, luminance * 1.07F > 255 ? 255 : Math.round(luminance * 1.07F));
            int green = Math.min(255, Math.round(luminance * 0.91F));
            int blue = Math.min(255, Math.round(luminance * 0.92F));
            image.setPixelRGBA(x, y, alpha << 24 | blue << 16 | green << 8 | red);
        }
    }
}

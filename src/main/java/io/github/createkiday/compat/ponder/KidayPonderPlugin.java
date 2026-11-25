package io.github.createkiday.compat.ponder;

import io.github.createkiday.CreateKidayMod;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public final class KidayPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateKidayMod.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(id("casting_table"), id("raw_kiday"), id("molten_kiday_bucket"),
                        id("ingot_mold"), id("hot_ingot"), id("hot_helmet"),
                        id("hot_chestplate"), id("hot_leggings"), id("hot_boots"))
                .addStoryBoard("workshop", KidayScenes::productionLine);
        helper.forComponents(id("blank_mold"), id("pickaxe_head_mold"), id("kiday_pickaxe_head"))
                .addStoryBoard("workshop", KidayScenes::patterns);
        helper.forComponents(id("lightning_engine"))
                .addStoryBoard("workshop", KidayScenes::lightningEngine);
        helper.forComponents(id("biomass_generator"))
                .addStoryBoard("workshop", KidayScenes::biomassGenerator);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, path);
    }
}

package com.farukylmz.createkiday.compat.ponder;

import com.farukylmz.createkiday.CreateKidayMod;
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
                        id("ingot_mold"), id("hot_kiday_ingot"), id("hot_kiday_helmet"),
                        id("hot_kiday_chestplate"), id("hot_kiday_leggings"), id("hot_kiday_boots"))
                .addStoryBoard("workshop", KidayScenes::productionLine);
        helper.forComponents(id("blank_mold"), id("pickaxe_head_mold"), id("kiday_pickaxe_head"))
                .addStoryBoard("workshop", KidayScenes::patterns);
        helper.forComponents(id("lightning_engine"))
                .addStoryBoard("workshop", KidayScenes::lightningEngine);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, path);
    }
}

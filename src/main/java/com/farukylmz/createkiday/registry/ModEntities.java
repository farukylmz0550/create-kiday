package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.entity.HunterDiskEntity;
import com.farukylmz.createkiday.entity.LeiGongEntity;
import com.farukylmz.createkiday.entity.WaqwaqBoatEntity;
import com.farukylmz.createkiday.entity.WaqwaqChestBoatEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<HunterDiskEntity>> HUNTER_DISK = TYPES.register("hunter_disk", () -> EntityType.Builder.<HunterDiskEntity>of(HunterDiskEntity::new, MobCategory.MISC).sized(0.45F, 0.16F).clientTrackingRange(10).updateInterval(1).build("hunter_disk"));
    public static final DeferredHolder<EntityType<?>, EntityType<LeiGongEntity>> LEI_GONG = TYPES.register("lei_gong",
            () -> EntityType.Builder.of(LeiGongEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("lei_gong"));
    public static final DeferredHolder<EntityType<?>, EntityType<WaqwaqBoatEntity>> WAQWAQ_BOAT = TYPES.register("waqwaq_boat",
            () -> EntityType.Builder.of(WaqwaqBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F).clientTrackingRange(10).updateInterval(3).build("waqwaq_boat"));
    public static final DeferredHolder<EntityType<?>, EntityType<WaqwaqChestBoatEntity>> WAQWAQ_CHEST_BOAT = TYPES.register("waqwaq_chest_boat",
            () -> EntityType.Builder.of(WaqwaqChestBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F).clientTrackingRange(10).updateInterval(3).build("waqwaq_chest_boat"));

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}

package io.github.createkiday.registry;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.entity.LeiGongEntity;
import io.github.createkiday.entity.WaqwaqBoatEntity;
import io.github.createkiday.entity.WaqwaqChestBoatEntity;
import io.github.createkiday.entity.SalurDiskEntity;
import io.github.createkiday.entity.StormSpiritEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<LeiGongEntity>> LEI_GONG = TYPES.register("lei_gong",
            () -> EntityType.Builder.of(LeiGongEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 3.6F)
                    .clientTrackingRange(10)
                    .build("lei_gong"));
    public static final DeferredHolder<EntityType<?>, EntityType<SalurDiskEntity>> SALUR_DISK = TYPES.register("salur_disk",
            () -> EntityType.Builder.<SalurDiskEntity>of(SalurDiskEntity::new, MobCategory.MISC)
                    .sized(0.55F, 0.12F).clientTrackingRange(8).updateInterval(1).build("salur_disk"));
    public static final DeferredHolder<EntityType<?>, EntityType<StormSpiritEntity>> STORM_SPIRIT = TYPES.register("storm_spirit",
            () -> EntityType.Builder.of(StormSpiritEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(8).updateInterval(2).build("storm_spirit"));
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

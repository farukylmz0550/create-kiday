package com.farukylmz.createkiday.gameplay;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.registry.ModItems;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class ShipwreckLootEvents {
    private static final Set<ResourceLocation> SHIPWRECK_CHESTS = Set.of(
            BuiltInLootTables.SHIPWRECK_MAP.location(), BuiltInLootTables.SHIPWRECK_SUPPLY.location(), BuiltInLootTables.SHIPWRECK_TREASURE.location());

    private ShipwreckLootEvents() {
    }

    @SubscribeEvent
    public static void addBraceletCore(LootTableLoadEvent event) {
        if (!SHIPWRECK_CHESTS.contains(event.getName())) return;
        event.getTable().addPool(LootPool.lootPool()
                .name("create_kiday_bracelet_core")
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.20F))
                .add(LootItem.lootTableItem(ModItems.BRACELET_CORE.get()))
                .build());
    }
}

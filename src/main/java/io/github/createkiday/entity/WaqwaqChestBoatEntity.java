package io.github.createkiday.entity;

import io.github.createkiday.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class WaqwaqChestBoatEntity extends ChestBoat {
    public WaqwaqChestBoatEntity(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        setVariant(Type.OAK);
    }

    @Override
    public Item getDropItem() {
        return ModItems.WAQWAQ_CHEST_BOAT.get();
    }
}

package com.farukylmz.createkiday.entity;

import com.farukylmz.createkiday.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class VakvakBoatEntity extends Boat {
    public VakvakBoatEntity(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        setVariant(Type.OAK);
    }

    @Override
    public Item getDropItem() {
        return ModItems.VAKVAK_BOAT.get();
    }
}

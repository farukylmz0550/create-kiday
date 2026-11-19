package com.farukylmz.createkiday.item;

import com.farukylmz.createkiday.entity.VakvakBoatEntity;
import com.farukylmz.createkiday.entity.VakvakChestBoatEntity;
import com.farukylmz.createkiday.registry.ModEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class VakvakBoatItem extends BoatItem {
    private final boolean chest;

    public VakvakBoatItem(boolean chest, Item.Properties properties) {
        super(chest, Boat.Type.OAK, properties);
        this.chest = chest;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() == HitResult.Type.MISS) return InteractionResultHolder.pass(stack);

        Vec3 view = player.getViewVector(1.0F);
        AABB search = player.getBoundingBox().expandTowards(view.scale(5.0D)).inflate(1.0D);
        List<Entity> entities = level.getEntities(player, search, entity -> !entity.isSpectator() && entity.isPickable());
        Vec3 eye = player.getEyePosition();
        for (Entity entity : entities) {
            if (entity.getBoundingBox().inflate(entity.getPickRadius()).contains(eye)) {
                return InteractionResultHolder.pass(stack);
            }
        }
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);

        Vec3 location = hit.getLocation();
        Boat boat = chest
                ? new VakvakChestBoatEntity(ModEntities.VAKVAK_CHEST_BOAT.get(), level)
                : new VakvakBoatEntity(ModEntities.VAKVAK_BOAT.get(), level);
        boat.setPos(location.x, location.y, location.z);
        boat.setYRot(player.getYRot());
        if (!level.noCollision(boat, boat.getBoundingBox())) return InteractionResultHolder.fail(stack);

        if (!level.isClientSide) {
            level.addFreshEntity(boat);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, location);
            stack.consume(1, player);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}

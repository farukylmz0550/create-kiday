package io.github.createkiday.item;

import io.github.createkiday.entity.LeiGongEntity;
import io.github.createkiday.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Right-click melee and the left-click lightning ability share one three-second cooldown. */
public final class LightningStaffItem extends SwordItem {
    public static final int COOLDOWN_TICKS = 60;
    public static final int DURABILITY_PER_USE = 1;
    public static final double LIGHTNING_RANGE = 24.0;
    private static final double MELEE_RANGE = 3.0;

    public LightningStaffItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(stack, true);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.pass(stack);
        EntityHitResult hit = raycastLiving(player, MELEE_RANGE);
        if (hit == null || !(hit.getEntity() instanceof LivingEntity target))
            return InteractionResultHolder.pass(stack);

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        target.hurt(player.damageSources().playerAttack(player), 6.0F);
        wear(serverPlayer, hand, stack);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static void castLightning(ServerPlayer player, Vec3 targetPoint) {
        LightningStaffItem staff = ModItems.LIGHTNING_STAFF.get();
        if (player.getCooldowns().isOnCooldown(staff)) return;
        player.getCooldowns().addCooldown(staff, COOLDOWN_TICKS);
        LeiGongEntity.staffLightning(player.serverLevel(), player, targetPoint);
        wear(player, InteractionHand.MAIN_HAND, player.getMainHandItem());
    }

    public static void castAtTarget(ServerPlayer player, LivingEntity target) {
        castLightning(player, target.getBoundingBox().getCenter());
    }

    public static EntityHitResult raycastLiving(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(range));
        AABB bounds = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        return ProjectileUtil.getEntityHitResult(player, start, end, bounds,
                entity -> entity instanceof LivingEntity && entity != player && !entity.isSpectator(), range * range);
    }

    private static void wear(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(DURABILITY_PER_USE, player, slot);
    }
}

package io.github.createkiday.item;

import io.github.createkiday.entity.SalurDiskEntity;
import io.github.createkiday.registry.ModEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import io.github.createkiday.advancement.KidayAdvancements;

public final class SalurDiskItem extends Item {
    public static final String ACTIVE_SALUR_DISK = "create_kiday.active_salur_disk";
    private final boolean explosive;

    public SalurDiskItem(boolean explosive, Properties properties) {
        super(properties);
        this.explosive = explosive;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean creative = player.getAbilities().instabuild;
        if (!creative && (player.getCooldowns().isOnCooldown(this)
                || (!explosive && player.getPersistentData().getBoolean(ACTIVE_SALUR_DISK))))
            return InteractionResultHolder.fail(stack);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            SalurDiskEntity projectile = new SalurDiskEntity(ModEntities.SALUR_DISK.get(), level);
            projectile.launch(serverPlayer, explosive);
            level.addFreshEntity(projectile);
            if (!creative && !explosive) {
                player.getPersistentData().putBoolean(ACTIVE_SALUR_DISK, true);
                player.getCooldowns().addCooldown(this, 200);
            }
            if (explosive) KidayAdvancements.award(serverPlayer, "tnt_disc", "used");
            if (!explosive && !player.getAbilities().instabuild) {
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}

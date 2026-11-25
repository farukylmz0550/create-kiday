package io.github.createkiday.gameplay;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.SalurBalance;
import io.github.createkiday.registry.ModItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class SalurArmorEvents {
    private static final List<EquipmentSlot> SLOTS = List.of(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    private static final List<ResourceLocation> MODIFIER_IDS = List.of(
            id("salur_helmet_speed"), id("salur_chestplate_speed"), id("salur_leggings_speed"), id("salur_boots_speed"));
    private static final Map<UUID, Integer> APPLIED_ARMOR_MASKS = new HashMap<>();

    private SalurArmorEvents() { }

    @SubscribeEvent
    public static void equipmentChanged(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        refresh(entity);
        if (entity.level() instanceof ServerLevel level) {
            level.getServer().tell(new TickTask(level.getServer().getTickCount() + 1, () -> {
                if (!entity.isRemoved() && entity.level() == level) refresh(entity);
            }));
        }
    }

    @SubscribeEvent
    public static void reconcileAfterLoad(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        UUID playerId = player.getUUID();
        int equippedMask = equippedMask(player);
        Integer previousMask = APPLIED_ARMOR_MASKS.get(playerId);
        if (previousMask == null || previousMask != equippedMask || player.tickCount % 20 == 0)
            applyMask(player, equippedMask);
    }

    @SubscribeEvent
    public static void forgetLoggedOutPlayer(PlayerEvent.PlayerLoggedOutEvent event) {
        APPLIED_ARMOR_MASKS.remove(event.getEntity().getUUID());
    }

    private static void refresh(LivingEntity entity) {
        var speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        applyMask(entity, equippedMask(entity));
    }

    private static void applyMask(LivingEntity entity, int equippedMask) {
        var speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        for (ResourceLocation modifierId : MODIFIER_IDS) speed.removeModifier(modifierId);
        int index = 0;
        for (EquipmentSlot slot : SLOTS) {
            if ((equippedMask & (1 << index)) != 0) {
                speed.addTransientModifier(new AttributeModifier(MODIFIER_IDS.get(index),
                        SalurBalance.movementBonus(1), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            index++;
        }
        if (entity instanceof Player) APPLIED_ARMOR_MASKS.put(entity.getUUID(), equippedMask);
    }

    private static int equippedMask(LivingEntity entity) {
        int mask = 0;
        for (int i = 0; i < SLOTS.size(); i++)
            if (entity.getItemBySlot(SLOTS.get(i)).is(salurArmor(SLOTS.get(i)))) mask |= 1 << i;
        return mask;
    }

    private static net.minecraft.world.item.Item salurArmor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ModItems.SALUR_HELMET.get();
            case CHEST -> ModItems.SALUR_CHESTPLATE.get();
            case LEGS -> ModItems.SALUR_LEGGINGS.get();
            case FEET -> ModItems.SALUR_BOOTS.get();
            default -> net.minecraft.world.item.Items.AIR;
        };
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, path);
    }
}

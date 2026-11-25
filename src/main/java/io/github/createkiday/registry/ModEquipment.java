package io.github.createkiday.registry;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.SalurBalance;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEquipment {
    private static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, CreateKidayMod.MOD_ID);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> KIDAY = MATERIALS.register("kiday", () -> {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.BODY, 11);
        return new ArmorMaterial(defense, 13, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(ModItems.KIDAY_INGOT.get()), List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "kiday"))), 2.5F, 0.05F);
    });
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SALUR = MATERIALS.register("salur", () -> {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, SalurBalance.armorProtection(0));
        defense.put(ArmorItem.Type.LEGGINGS, SalurBalance.armorProtection(2));
        defense.put(ArmorItem.Type.CHESTPLATE, SalurBalance.armorProtection(1));
        defense.put(ArmorItem.Type.HELMET, SalurBalance.armorProtection(0));
        defense.put(ArmorItem.Type.BODY, 7);
        return new ArmorMaterial(defense, SalurBalance.ENCHANTABILITY, SoundEvents.ARMOR_EQUIP_IRON,
                () -> Ingredient.of(ModItems.SALUR_INGOT.get()),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "salur"))),
                SalurBalance.ARMOR_TOUGHNESS, 0.0F);
    });

    private ModEquipment() {
    }

    public static void register(IEventBus bus) {
        MATERIALS.register(bus);
    }
}

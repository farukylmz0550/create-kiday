package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.KidayBalance;
import com.farukylmz.createkiday.item.HotKidayItem;
import com.farukylmz.createkiday.item.HunterBraceletItem;
import com.farukylmz.createkiday.item.KidayChronicleItem;
import com.farukylmz.createkiday.item.KidayMoldItem;
import com.farukylmz.createkiday.item.KidayTier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
import com.farukylmz.createkiday.item.VakvakBoatItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateKidayMod.MOD_ID);
    private static final SupplierList INGREDIENTS = new SupplierList();
    private static final SupplierList EQUIPMENT = new SupplierList();

    public static final DeferredItem<Item> RAW_KIDAY = ingredient("raw_kiday");
    public static final DeferredItem<Item> KIDAY_INGOT = ingredient("kiday_ingot");
    public static final DeferredItem<Item> KIDAY_SHEET = ingredient("kiday_sheet");
    public static final DeferredItem<BucketItem> MOLTEN_KIDAY_BUCKET = INGREDIENTS.addAndReturn(ITEMS.register("molten_kiday_bucket", () -> new BucketItem(ModFluids.MOLTEN_KIDAY.get(), new Item.Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET))));
    public static final DeferredItem<Item> BLANK_MOLD = ingredient("blank_mold");
    public static final DeferredItem<Item> BRACELET_CORE = ingredient("bracelet_core");
    public static final DeferredItem<Item> SWORD_HEAD = ingredient("kiday_sword_head");
    public static final DeferredItem<Item> PICKAXE_HEAD = ingredient("kiday_pickaxe_head");
    public static final DeferredItem<Item> AXE_HEAD = ingredient("kiday_axe_head");
    public static final DeferredItem<Item> SHOVEL_HEAD = ingredient("kiday_shovel_head");
    public static final DeferredItem<Item> HOE_HEAD = ingredient("kiday_hoe_head");
    public static final DeferredItem<Item> BRACELET_FRAME = ingredient("bracelet_frame");

    public static final DeferredItem<SwordItem> KIDAY_SWORD = equipment("kiday_sword", () -> new SwordItem(KidayTier.INSTANCE, new Item.Properties().attributes(SwordItem.createAttributes(KidayTier.INSTANCE, 3.0F, -2.4F))));
    public static final DeferredItem<PickaxeItem> KIDAY_PICKAXE = equipment("kiday_pickaxe", () -> new PickaxeItem(KidayTier.INSTANCE, new Item.Properties().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> KIDAY_AXE = equipment("kiday_axe", () -> new AxeItem(KidayTier.INSTANCE, new Item.Properties().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> KIDAY_SHOVEL = equipment("kiday_shovel", () -> new ShovelItem(KidayTier.INSTANCE, new Item.Properties().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> KIDAY_HOE = equipment("kiday_hoe", () -> new HoeItem(KidayTier.INSTANCE, new Item.Properties().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, -3.5F, 0.0F))));
    public static final DeferredItem<ArmorItem> KIDAY_HELMET = equipment("kiday_helmet", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.HELMET, new Item.Properties().durability(385)));
    public static final DeferredItem<ArmorItem> KIDAY_CHESTPLATE = equipment("kiday_chestplate", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(560)));
    public static final DeferredItem<ArmorItem> KIDAY_LEGGINGS = equipment("kiday_leggings", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(525)));
    public static final DeferredItem<ArmorItem> KIDAY_BOOTS = equipment("kiday_boots", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.BOOTS, new Item.Properties().durability(455)));
    public static final DeferredItem<HunterBraceletItem> HUNTER_BRACELET = EQUIPMENT.addAndReturn(ITEMS.register("hunter_bracelet", () -> new HunterBraceletItem(new Item.Properties())));
    public static final DeferredItem<KidayChronicleItem> KIDAY_CHRONICLE = EQUIPMENT.addAndReturn(ITEMS.register("kiday_chronicle", () -> new KidayChronicleItem(new Item.Properties())));
    public static final DeferredItem<Item> STORM_AMULET = EQUIPMENT.addAndReturn(
            ITEMS.registerSimpleItem("storm_amulet", new Item.Properties().stacksTo(1)));
    public static final DeferredItem<DeferredSpawnEggItem> LEI_GONG_SPAWN_EGG = EQUIPMENT.addAndReturn(
            ITEMS.register("lei_gong_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.LEI_GONG::get, 0xD9B54A, 0x3B334A, new Item.Properties())));
    public static final DeferredItem<VakvakBoatItem> VAKVAK_BOAT = EQUIPMENT.addAndReturn(
            ITEMS.register("vakvak_boat", () -> new VakvakBoatItem(false, new Item.Properties().stacksTo(1))));
    public static final DeferredItem<VakvakBoatItem> VAKVAK_CHEST_BOAT = EQUIPMENT.addAndReturn(
            ITEMS.register("vakvak_chest_boat", () -> new VakvakBoatItem(true, new Item.Properties().stacksTo(1))));

    public static final List<DeferredItem<KidayMoldItem>> SHAPED_MOLDS = List.of(
            mold("ingot_mold", 1), mold("sword_head_mold", 2), mold("pickaxe_head_mold", 3), mold("axe_head_mold", 3),
            mold("shovel_head_mold", 1), mold("hoe_head_mold", 2), mold("helmet_mold", 5), mold("chestplate_mold", 8),
            mold("leggings_mold", 7), mold("boots_mold", 4), mold("trapdoor_mold", 4), mold("bracelet_frame_mold", 2));

    public static final List<DeferredItem<HotKidayItem>> HOT_PRODUCTS = List.of(
            hot("hot_kiday_ingot"), hot("hot_kiday_sword_head"), hot("hot_kiday_pickaxe_head"), hot("hot_kiday_axe_head"),
            hot("hot_kiday_shovel_head"), hot("hot_kiday_hoe_head"), hot("hot_kiday_helmet"), hot("hot_kiday_chestplate"),
            hot("hot_kiday_leggings"), hot("hot_kiday_boots"), hot("hot_kiday_trapdoor"), hot("hot_bracelet_frame"));

    private ModItems() {
    }

    private static DeferredItem<Item> ingredient(String name) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name);
        INGREDIENTS.add(item);
        return item;
    }

    private static <T extends Item> DeferredItem<T> equipment(String name, Supplier<T> factory) {
        DeferredItem<T> item = ITEMS.register(name, factory);
        EQUIPMENT.add(item);
        return item;
    }

    private static DeferredItem<KidayMoldItem> mold(String name, int units) {
        DeferredItem<KidayMoldItem> item = ITEMS.register(name, () -> new KidayMoldItem(units));
        INGREDIENTS.add(item);
        return item;
    }

    private static DeferredItem<HotKidayItem> hot(String name) {
        DeferredItem<HotKidayItem> item = ITEMS.register(name, () -> new HotKidayItem(new Item.Properties().stacksTo(1)));
        INGREDIENTS.add(item);
        return item;
    }

    public static List<Supplier<? extends Item>> visibleIngredients() {
        return List.copyOf(INGREDIENTS);
    }

    public static List<Supplier<? extends Item>> visibleEquipment() {
        return List.copyOf(EQUIPMENT);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private static final class SupplierList extends ArrayList<Supplier<? extends Item>> {
        private <T extends Supplier<? extends Item>> T addAndReturn(T value) {
            add(value);
            return value;
        }
    }
}

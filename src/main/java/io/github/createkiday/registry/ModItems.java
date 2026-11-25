package io.github.createkiday.registry;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.KidayBalance;
import io.github.createkiday.item.HotKidayItem;
import io.github.createkiday.item.KidayChronicleItem;
import io.github.createkiday.item.KidayMoldItem;
import io.github.createkiday.item.KidayTier;
import io.github.createkiday.item.SalurTier;
import io.github.createkiday.item.SalurDiskItem;
import io.github.createkiday.item.LightningStaffItem;
import io.github.createkiday.SalurBalance;
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
import io.github.createkiday.item.WaqwaqBoatItem;
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
    public static final DeferredItem<Item> RAW_SALUR = ingredient("raw_salur");
    public static final DeferredItem<Item> SALUR_INGOT = ingredient("salur_ingot");
    public static final DeferredItem<Item> SALUR_SHEET = ingredient("salur_sheet");
    public static final DeferredItem<Item> KIDAY_PANEL = ingredient("kiday_panel");
    public static final DeferredItem<Item> INCOMPLETE_STORM_MECHANISM = ingredient("incomplete_storm_mechanism");
    public static final DeferredItem<Item> STORM_MECHANISM = ingredient("storm_mechanism");
    public static final DeferredItem<BucketItem> MOLTEN_SALUR_BUCKET = INGREDIENTS.addAndReturn(ITEMS.register("molten_salur_bucket", () -> new BucketItem(ModFluids.MOLTEN_SALUR.get(), new Item.Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET))));
    public static final DeferredItem<Item> BLANK_MOLD = ingredient("blank_mold");
    public static final DeferredItem<Item> SWORD_HEAD = ingredient("kiday_sword_head");
    public static final DeferredItem<Item> PICKAXE_HEAD = ingredient("kiday_pickaxe_head");
    public static final DeferredItem<Item> AXE_HEAD = ingredient("kiday_axe_head");
    public static final DeferredItem<Item> SHOVEL_HEAD = ingredient("kiday_shovel_head");
    public static final DeferredItem<Item> HOE_HEAD = ingredient("kiday_hoe_head");

    public static final DeferredItem<SwordItem> KIDAY_SWORD = equipment("kiday_sword", () -> new SwordItem(KidayTier.INSTANCE, new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(KidayTier.INSTANCE, 3.0F, -2.4F))));
    public static final DeferredItem<PickaxeItem> KIDAY_PICKAXE = equipment("kiday_pickaxe", () -> new PickaxeItem(KidayTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> KIDAY_AXE = equipment("kiday_axe", () -> new AxeItem(KidayTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> KIDAY_SHOVEL = equipment("kiday_shovel", () -> new ShovelItem(KidayTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> KIDAY_HOE = equipment("kiday_hoe", () -> new HoeItem(KidayTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(KidayTier.INSTANCE, -3.5F, 0.0F))));
    public static final DeferredItem<ArmorItem> KIDAY_HELMET = equipment("kiday_helmet", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.HELMET, new Item.Properties().fireResistant().durability(385)));
    public static final DeferredItem<ArmorItem> KIDAY_CHESTPLATE = equipment("kiday_chestplate", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant().durability(560)));
    public static final DeferredItem<ArmorItem> KIDAY_LEGGINGS = equipment("kiday_leggings", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant().durability(525)));
    public static final DeferredItem<ArmorItem> KIDAY_BOOTS = equipment("kiday_boots", () -> new ArmorItem(ModEquipment.KIDAY, ArmorItem.Type.BOOTS, new Item.Properties().fireResistant().durability(455)));
    public static final DeferredItem<SwordItem> SALUR_SWORD = equipment("salur_sword", () -> new SwordItem(SalurTier.INSTANCE, new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(SalurTier.INSTANCE, 3.0F, -2.4F))));
    public static final DeferredItem<PickaxeItem> SALUR_PICKAXE = equipment("salur_pickaxe", () -> new PickaxeItem(SalurTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(SalurTier.INSTANCE, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> SALUR_AXE = equipment("salur_axe", () -> new AxeItem(SalurTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(SalurTier.INSTANCE, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> SALUR_SHOVEL = equipment("salur_shovel", () -> new ShovelItem(SalurTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(SalurTier.INSTANCE, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> SALUR_HOE = equipment("salur_hoe", () -> new HoeItem(SalurTier.INSTANCE, new Item.Properties().fireResistant().attributes(DiggerItem.createAttributes(SalurTier.INSTANCE, -3.5F, 0.0F))));
    public static final DeferredItem<ArmorItem> SALUR_HELMET = equipment("salur_helmet", () -> new ArmorItem(ModEquipment.SALUR, ArmorItem.Type.HELMET, new Item.Properties().fireResistant().durability(SalurBalance.armorDurability(0))));
    public static final DeferredItem<ArmorItem> SALUR_CHESTPLATE = equipment("salur_chestplate", () -> new ArmorItem(ModEquipment.SALUR, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant().durability(SalurBalance.armorDurability(1))));
    public static final DeferredItem<ArmorItem> SALUR_LEGGINGS = equipment("salur_leggings", () -> new ArmorItem(ModEquipment.SALUR, ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant().durability(SalurBalance.armorDurability(2))));
    public static final DeferredItem<ArmorItem> SALUR_BOOTS = equipment("salur_boots", () -> new ArmorItem(ModEquipment.SALUR, ArmorItem.Type.BOOTS, new Item.Properties().fireResistant().durability(SalurBalance.armorDurability(3))));
    public static final DeferredItem<Item> SALUR_SWORD_HEAD = ingredient("salur_sword_head");
    public static final DeferredItem<Item> SALUR_PICKAXE_HEAD = ingredient("salur_pickaxe_head");
    public static final DeferredItem<Item> SALUR_AXE_HEAD = ingredient("salur_axe_head");
    public static final DeferredItem<Item> SALUR_SHOVEL_HEAD = ingredient("salur_shovel_head");
    public static final DeferredItem<Item> SALUR_HOE_HEAD = ingredient("salur_hoe_head");
    public static final DeferredItem<KidayChronicleItem> KIDAY_CHRONICLE = EQUIPMENT.addAndReturn(ITEMS.register("kiday_chronicle", () -> new KidayChronicleItem(new Item.Properties())));
    public static final DeferredItem<Item> STORM_AMULET = EQUIPMENT.addAndReturn(
            ITEMS.registerSimpleItem("storm_amulet", new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LEI_GONG_CORE = equipment("lei_gong_core",
            () -> new Item(new Item.Properties().fireResistant().stacksTo(1)));
    public static final DeferredItem<Item> LIGHTNING_STAFF_HEAD = ingredient("lightning_staff_head");
    public static final DeferredItem<LightningStaffItem> LIGHTNING_STAFF = equipment("lightning_staff",
            () -> new LightningStaffItem(KidayTier.INSTANCE, new Item.Properties().durability(KidayBalance.TOOL_DURABILITY)
                    .fireResistant().attributes(SwordItem.createAttributes(KidayTier.INSTANCE, 5.0F, -2.4F))));
    public static final DeferredItem<SalurDiskItem> SALUR_DISK = equipment("salur_disc",
            () -> new SalurDiskItem(false, new Item.Properties().durability(905)));
    public static final DeferredItem<SalurDiskItem> TNT_DISC = equipment("tnt_disc",
            () -> new SalurDiskItem(true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<DeferredSpawnEggItem> LEI_GONG_SPAWN_EGG = EQUIPMENT.addAndReturn(
            ITEMS.register("lei_gong_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.LEI_GONG::get, 0xD9B54A, 0x3B334A, new Item.Properties())));
    public static final DeferredItem<DeferredSpawnEggItem> STORM_SPIRIT_SPAWN_EGG = EQUIPMENT.addAndReturn(
            ITEMS.register("storm_spirit_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.STORM_SPIRIT::get, 0x102B41, 0x54C7D2, new Item.Properties())));
    public static final DeferredItem<WaqwaqBoatItem> WAQWAQ_BOAT = EQUIPMENT.addAndReturn(
            ITEMS.register("waqwaq_boat", () -> new WaqwaqBoatItem(false, new Item.Properties().stacksTo(1))));
    public static final DeferredItem<WaqwaqBoatItem> WAQWAQ_CHEST_BOAT = EQUIPMENT.addAndReturn(
            ITEMS.register("waqwaq_chest_boat", () -> new WaqwaqBoatItem(true, new Item.Properties().stacksTo(1))));

    public static final List<DeferredItem<KidayMoldItem>> SHAPED_MOLDS = List.of(
            mold("ingot_mold", 1), mold("sword_head_mold", 2), mold("pickaxe_head_mold", 3), mold("axe_head_mold", 3),
            mold("shovel_head_mold", 1), mold("hoe_head_mold", 2), mold("helmet_mold", 5), mold("chestplate_mold", 8),
            mold("leggings_mold", 7), mold("boots_mold", 4), mold("trapdoor_mold", 4), mold("disk_mold", 3));

    public static final List<DeferredItem<HotKidayItem>> HOT_PRODUCTS = List.of(
            hot("hot_ingot"), hot("hot_sword_head"), hot("hot_pickaxe_head"), hot("hot_axe_head"),
            hot("hot_shovel_head"), hot("hot_hoe_head"), hot("hot_helmet"), hot("hot_chestplate"),
            hot("hot_leggings"), hot("hot_boots"), hot("hot_trapdoor"), hot("hot_disk"));

    private ModItems() {
    }

    private static DeferredItem<Item> ingredient(String name) {
        boolean fireproof = name.equals("raw_kiday") || name.startsWith("kiday_");
        DeferredItem<Item> item = ITEMS.register(name, () -> new Item(fireproof ? new Item.Properties().fireResistant() : new Item.Properties()));
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

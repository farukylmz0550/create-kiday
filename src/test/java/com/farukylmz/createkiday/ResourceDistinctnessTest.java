package com.farukylmz.createkiday;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDistinctnessTest {
    @Test
    void toolsArmorPatternsAndMoltenCastingsHaveDistinctTextures() {
        assertAllDistinct(List.of(
                "kiday_sword", "kiday_pickaxe", "kiday_axe", "kiday_shovel", "kiday_hoe",
                "kiday_sword_head", "kiday_pickaxe_head", "kiday_axe_head", "kiday_shovel_head", "kiday_hoe_head",
                "kiday_helmet", "kiday_chestplate", "kiday_leggings", "kiday_boots"));
        assertAllDistinct(List.of(
                "blank_mold", "ingot_mold", "sword_head_mold", "pickaxe_head_mold", "axe_head_mold", "shovel_head_mold",
                "hoe_head_mold", "helmet_mold", "chestplate_mold", "leggings_mold", "boots_mold", "trapdoor_mold", "bracelet_frame_mold"));
        assertAllDistinct(List.of(
                "hot_kiday_ingot", "hot_kiday_sword_head", "hot_kiday_pickaxe_head", "hot_kiday_axe_head",
                "hot_kiday_shovel_head", "hot_kiday_hoe_head", "hot_kiday_helmet", "hot_kiday_chestplate",
                "hot_kiday_leggings", "hot_kiday_boots", "hot_kiday_trapdoor", "hot_bracelet_frame"));
    }

    @Test
    void removedPaxelHasNoRegisteredModelRecipeOrTextures() {
        for (String path : List.of(
                "assets/create_kiday/models/item/kiday_paxel.json",
                "assets/create_kiday/models/item/kiday_paxel_head.json",
                "assets/create_kiday/textures/item/kiday_paxel.png",
                "assets/create_kiday/textures/item/kiday_paxel_head.png",
                "data/create_kiday/recipe/kiday_paxel.json",
                "data/create_kiday/recipe/kiday_paxel_head.json",
                "data/create_kiday/tags/block/mineable/paxel.json")) {
            assertNull(getClass().getClassLoader().getResource(path), "Removed Paxel resource is still packaged: " + path);
        }
    }

    @Test
    void technicalBlockModelsUseSemanticFaceTextureNames() throws IOException {
        for (String machine : List.of("casting_table", "lightning_engine")) {
            String model = text("assets/create_kiday/models/block/" + machine + ".json");
            for (String face : List.of("top", "back", "bottom", "front", "right"))
                assertTrue(model.contains(machine + "_" + face), machine + " model is missing its " + face + " texture");
            if (machine.equals("casting_table")) assertTrue(model.contains("\"left\": \"#right\""));
            else assertTrue(model.contains("lightning_engine_left"));
            for (String direction : List.of("north", "south", "east", "west"))
                assertFalse(model.contains(machine + "_" + direction), machine + " model still refers to compass-named textures");
        }
        String engineStates = text("assets/create_kiday/blockstates/lightning_engine.json");
        assertTrue(engineStates.contains("\"facing=east\""));
        assertTrue(engineStates.contains("\"y\": 90"));
        assertTrue(engineStates.contains("\"facing=south\""));
        assertTrue(engineStates.contains("\"y\": 180"));
        assertTrue(engineStates.contains("\"facing=west\""));
        assertTrue(engineStates.contains("\"y\": 270"));
    }

    @Test
    void thinDoorModelsAndKidayPonderTemplateArePackaged() throws IOException {
        String door = text("assets/create_kiday/models/block/kiday_door_bottom_left.json");
        String trapdoor = text("assets/create_kiday/models/block/kiday_trapdoor_bottom.json");
        assertTrue(door.contains("minecraft:block/door_bottom_left"));
        assertTrue(trapdoor.contains("minecraft:block/template_trapdoor_bottom"));
        assertNotNull(getClass().getClassLoader().getResource("assets/create_kiday/ponder/workshop.nbt"));
    }

    @Test
    void transparentDoorBlocksSeparateItemArmorUvAndRemovedDiskItemArePackagedCorrectly() {
        assertTransparentAndOpaque("assets/create_kiday/textures/block/kiday_door_top.png", 80, 160);
        assertTransparentAndOpaque("assets/create_kiday/textures/block/kiday_door_bottom.png", 80, 160);
        assertTransparentAndOpaque("assets/create_kiday/textures/block/kiday_trapdoor.png", 80, 160);
        assertTransparentAndOpaque("assets/create_kiday/textures/item/kiday_door.png", 20, 40);
        assertNotEquals(hash("assets/create_kiday/textures/item/kiday_door.png"),
                hash("assets/create_kiday/textures/block/kiday_door_bottom.png"),
                "Held door item must use art distinct from the placed door block");
        assertArmorUv("assets/create_kiday/textures/models/armor/kiday_layer_1.png");
        assertArmorUv("assets/create_kiday/textures/models/armor/kiday_layer_2.png");
        assertNull(getClass().getClassLoader().getResource("assets/create_kiday/models/item/hunter_disk.json"),
                "Hunter Disk must remain an entity visual, not an inventory item");
    }

    @Test
    void heldToolsEngineGeometryWorldgenAndCraftingRoutesAreRealResources() throws IOException {
        for (String tool : List.of("sword", "pickaxe", "axe", "shovel", "hoe"))
            assertTrue(text("assets/create_kiday/models/item/kiday_" + tool + ".json").contains("minecraft:item/handheld"));
        String engine = text("assets/create_kiday/models/block/lightning_engine.json");
        String shaft = text("assets/create_kiday/models/block/lightning_engine_shaft.json");
        assertTrue(engine.contains("29") && engine.contains("30"), "Lightning Engine has no raised lightning rod geometry");
        assertTrue(engine.contains("create_kiday:block/lightning_engine_rod"), "Lightning rod has no custom copper texture");
        assertTextureComplexity("assets/create_kiday/textures/block/lightning_engine_rod.png", 256, 8);
        assertFalse(shaft.contains("-5"), "Lightning Engine shaft is longer than Create's gearbox half-shaft");
        assertTrue(shaft.contains("create:block/shaft_half"), "Lightning Engine output does not use Create's gearbox-length half-shaft");
        assertFalse(shaft.contains("create_kiday:block/lightning_engine"), "Lightning Engine shaft still uses pink machine textures");
        String modifier = text("data/create_kiday/neoforge/biome_modifier/add_kiday_ore.json");
        assertTrue(modifier.contains("#minecraft:is_overworld"));
        String orePlacement = text("data/create_kiday/worldgen/placed_feature/kiday_ore.json");
        assertTrue(orePlacement.contains("\"count\": 12"));
        assertTrue(orePlacement.contains("\"absolute\": -64"));
        assertTrue(orePlacement.contains("\"absolute\": 16"));
        for (String recipe : List.of("casting_table", "lightning_engine", "kiday_chronicle", "kiday_ingot_from_block"))
            assertNotNull(getClass().getClassLoader().getResource("data/create_kiday/recipe/" + recipe + ".json"),
                    "Missing acquisition recipe " + recipe);
    }

    @Test
    void chronicleHasCompleteAndDifferentEnglishAndTurkishTranslations() throws IOException {
        String english = text("assets/create_kiday/lang/en_us.json");
        String turkish = text("assets/create_kiday/lang/tr_tr.json");
        for (int page = 1; page <= 22; page++) {
            String key = "book.create_kiday.chronicle.page_" + page;
            assertTrue(english.contains(key), "Missing English chronicle page " + page);
            assertTrue(turkish.contains(key), "Missing Turkish chronicle page " + page);
        }
        assertTrue(english.contains("The Epic of Kitay Bahsi"));
        assertFalse(english.contains("Kıtay Bahsı Destanı"));
        assertTrue(turkish.contains("Kıtay Bahsı Destanı"));
        assertFalse(turkish.contains("**"));
        assertFalse(turkish.contains("# Kıtay"));
    }

    @Test
    void advancementTreeAndBothLocalesAreComplete() throws IOException {
        String[] ids = {"root", "first_cast", "all_tools", "full_armor", "master_smith",
                "all_casts", "engine_running", "defeat_lei_gong", "touch_grass"};
        String english = text("assets/create_kiday/lang/en_us.json");
        String turkish = text("assets/create_kiday/lang/tr_tr.json");
        for (String id : ids) {
            String path = "data/create_kiday/advancement/" + id + ".json";
            String json = text(path);
            assertTrue(json.contains("advancements.create_kiday." + id + ".title"), path + " has no title");
            assertTrue(json.contains("advancements.create_kiday." + id + ".description"), path + " has no description");
            assertTrue(english.contains("advancements.create_kiday." + id + ".title"), "Missing English title: " + id);
            assertTrue(english.contains("advancements.create_kiday." + id + ".description"), "Missing English description: " + id);
            assertTrue(turkish.contains("advancements.create_kiday." + id + ".title"), "Missing Turkish title: " + id);
            assertTrue(turkish.contains("advancements.create_kiday." + id + ".description"), "Missing Turkish description: " + id);
        }
        assertTrue(text("data/create_kiday/advancement/master_smith.json").contains("\"frame\": \"challenge\""));
        assertTrue(text("data/create_kiday/advancement/all_casts.json").contains("\"frame\": \"goal\""));
        assertTrue(text("data/create_kiday/advancement/touch_grass.json").contains("\"hidden\": true"));
        assertTrue(text("data/create_kiday/advancement/touch_grass.json").contains("\"announce_to_chat\": true"));
        assertTrue(text("data/create_kiday/advancement/touch_grass.json").contains("\"frame\": \"challenge\""));
        assertTrue(text("data/create_kiday/advancement/defeat_lei_gong.json").contains("\"frame\": \"challenge\""));
        assertTrue(text("data/create_kiday/advancement/touch_grass.json").contains("\"all_eight\""));
        assertTrue(turkish.contains("Kadayıf Değil, Kiday"));
        assertTrue(turkish.contains("İlk ham Kiday'ını buldun. Şerbet döküp yemeyi de denersin artık."));
        assertTrue(turkish.contains("Bütün başarımları tamamladın. Maalesef bu bilgi CV'nde pek işine yaramayacak."));
        assertTrue(english.contains("Go Touch Some Grass"));
        assertTrue(english.contains("effect.create_kiday.stun"));
        assertTrue(turkish.contains("effect.create_kiday.stun"));
        for (String expected : List.of(
                "\"advancements.create_kiday.root.title\": \"Kadayıf Değil, Kiday\"",
                "\"advancements.create_kiday.root.description\": \"İlk ham Kiday'ını buldun. Şerbet döküp yemeyi de denersin artık.\"",
                "\"advancements.create_kiday.first_cast.title\": \"Kalıbının Eşyası\"",
                "\"advancements.create_kiday.first_cast.description\": \"İlk dökümünü yaptın. Kalıbına bakınca daha fazlasını beklememek gerekirdi.\"",
                "\"advancements.create_kiday.all_tools.title\": \"Alet Çok, İş Yok\"",
                "\"advancements.create_kiday.all_tools.description\": \"Bütün Kiday aletlerini ürettin. İş yapmamak için artık bahanen kalmadı.\"",
                "\"advancements.create_kiday.full_armor.title\": \"Ateşten Gömlek\"",
                "\"advancements.create_kiday.full_armor.description\": \"Tam Kiday zırhını kuşandın. Konserve kutusu olmanın bir üst aşaması.\"",
                "\"advancements.create_kiday.master_smith.title\": \"Baştan Sona Demirbaş\"",
                "\"advancements.create_kiday.master_smith.description\": \"Bütün zırh ve aletleri tamamladın. Yürüyen bir nalbursun, hayırlı işler.\"",
                "\"advancements.create_kiday.all_casts.title\": \"Döküm Döküm Üstüne\"",
                "\"advancements.create_kiday.all_casts.description\": \"Bütün kalıplardan ürün döktün. Bir tek karakterini kalıba sokamadık.\"",
                "\"advancements.create_kiday.engine_running.title\": \"Tesla Görse Kıskanır!\"",
                "\"advancements.create_kiday.engine_running.description\": \"Yıldırım Motoru'nu çalıştırdın. Bir bulutu sömürmediğin kalmıştı.\"",
                "\"advancements.create_kiday.defeat_lei_gong.title\": \"Fırtınayı Dizginlemek\"",
                "\"advancements.create_kiday.defeat_lei_gong.description\": \"LeiGong'un Avatarı'nı yendin. Tanrılık makamına vekâleten atanmış olmalı.\"",
                "\"advancements.create_kiday.touch_grass.title\": \"Git Biraz Çimene Dokun\"",
                "\"advancements.create_kiday.touch_grass.description\": \"Bütün başarımları tamamladın. Maalesef bu bilgi CV'nde pek işine yaramayacak.\""))
            assertTrue(turkish.contains(expected), "Turkish achievement text changed: " + expected);

        assertTrue(text("data/create_kiday/advancement/root.json")
                .contains("\"trigger\": \"minecraft:impossible\""),
                "The root advancement must be awarded only after a real Kiday ore drop event");
        assertFalse(text("data/create_kiday/advancement/root.json").contains("inventory_changed"),
                "Possessing or receiving Raw Kiday without mining ore must not grant the root");
        assertTrue(text("data/create_kiday/advancement/first_cast.json").contains("\"parent\": \"create_kiday:root\""));
        assertTrue(text("data/create_kiday/advancement/all_tools.json").contains("\"parent\": \"create_kiday:first_cast\""));
        assertTrue(text("data/create_kiday/advancement/full_armor.json").contains("\"parent\": \"create_kiday:all_tools\""));
        assertTrue(text("data/create_kiday/advancement/master_smith.json").contains("\"parent\": \"create_kiday:full_armor\""));
        assertTrue(text("data/create_kiday/advancement/all_casts.json").contains("\"parent\": \"create_kiday:first_cast\""));
        assertTrue(text("data/create_kiday/advancement/engine_running.json").contains("\"parent\": \"create_kiday:root\""));
        assertTrue(text("data/create_kiday/advancement/defeat_lei_gong.json").contains("\"parent\": \"create_kiday:root\""));
        String casts = text("data/create_kiday/advancement/all_casts.json");
        for (String mold : List.of("ingot", "sword_head", "pickaxe_head", "axe_head", "shovel_head", "hoe_head",
                "helmet", "chestplate", "leggings", "boots", "trapdoor", "bracelet_frame"))
            assertTrue(casts.contains("\"cast_" + mold + "\""), "Missing cast criterion for mold " + mold);
    }

    @Test
    void leiGongStormGroveAndAmuletResourcesArePackaged() throws IOException {
        assertTextureSize("assets/create_kiday/textures/entity/lei_gong.png", 64, 64);
        assertTextureSize("assets/create_kiday/textures/item/storm_amulet.png", 16, 16);
        assertTextureComplexity("assets/create_kiday/textures/entity/lei_gong.png", 1000, 6);
        assertNotNull(getClass().getClassLoader().getResource("assets/create_kiday/models/item/storm_amulet.json"));
        String stormGrove = text("data/create_kiday/worldgen/biome/storm_grove.json");
        assertTrue(stormGrove.contains("\"type\": \"create_kiday:lei_gong\",\n        \"weight\": 400"));
        assertFalse(stormGrove.contains("cherry_trees_medium"));
        assertTrue(stormGrove.contains("create_kiday:waqwaq_trees"),
                "Waqwaq placed feature must be part of Storm Grove vegetal decoration");
        assertNull(getClass().getClassLoader().getResource(
                "data/create_kiday/neoforge/biome_modifier/waqwaq_trees.json"));
        assertNotNull(getClass().getClassLoader().getResource("assets/create_kiday/models/item/lei_gong_spawn_egg.json"));
        assertTrue(text("assets/create_kiday/lang/en_us.json").contains("Lei Gong Spawn Egg"));
        assertTrue(text("assets/create_kiday/lang/tr_tr.json").contains("Lei Gong Çağırma Yumurtası"));
        assertTrue(text("data/curios/tags/item/necklace.json").contains("create_kiday:storm_amulet"));
        assertNull(getClass().getClassLoader().getResource("data/create_kiday/recipe/storm_amulet.json"));
    }

    private static void assertAllDistinct(List<String> names) {
        Set<String> hashes = new HashSet<>();
        for (String name : names) hashes.add(hash("assets/create_kiday/textures/item/" + name + ".png"));
        assertEquals(names.size(), hashes.size(), "Texture files must be visually distinct: " + names);
    }

    private static String hash(String path) {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()));
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new AssertionError(exception);
        }
    }

    private static String text(String path) throws IOException {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static void assertTextureComplexity(String path, int minimumVisiblePixels, int minimumColors) {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            BufferedImage image = ImageIO.read(input);
            Set<Integer> colors = new HashSet<>();
            int visiblePixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    if ((argb >>> 24) == 0) continue;
                    visiblePixels++;
                    colors.add(argb);
                }
            }
            assertTrue(visiblePixels >= minimumVisiblePixels, path + " is still a sparse placeholder");
            assertTrue(colors.size() >= minimumColors, path + " does not have enough visual detail");
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void assertTextureSize(String path, int width, int height) {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            BufferedImage image = ImageIO.read(input);
            assertEquals(width, image.getWidth(), path + " has the wrong width");
            assertEquals(height, image.getHeight(), path + " has the wrong height");
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private static double[] averageOpaqueColor(String path) {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            BufferedImage image = ImageIO.read(input);
            long red = 0;
            long green = 0;
            long blue = 0;
            long count = 0;
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) continue;
                red += (argb >>> 16) & 0xff;
                green += (argb >>> 8) & 0xff;
                blue += argb & 0xff;
                count++;
            }
            assertTrue(count > 0, path + " is fully transparent");
            return new double[] {(double) red / count, (double) green / count, (double) blue / count};
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void assertTransparentAndOpaque(String path, int minimumTransparent, int minimumOpaque) {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            BufferedImage image = ImageIO.read(input);
            int transparent = 0;
            int opaque = 0;
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) transparent++; else opaque++;
            }
            assertTrue(transparent >= minimumTransparent, path + " has no real transparent cutouts");
            assertTrue(opaque >= minimumOpaque, path + " lost its solid frame");
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void assertFullyOpaque(String path) {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            BufferedImage image = ImageIO.read(input);
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                assertEquals(255, image.getRGB(x, y) >>> 24,
                        path + " contains a transparent pixel at " + x + "," + y);
            }
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void assertArmorUv(String path) {
        try (InputStream input = ResourceDistinctnessTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing resource " + path);
            BufferedImage image = ImageIO.read(input);
            assertEquals(64, image.getWidth());
            assertEquals(32, image.getHeight());
            int transparent = 0;
            Set<Integer> colors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) transparent++; else colors.add(argb);
            }
            assertTrue(transparent > 900, path + " does not preserve a real armor UV alpha mask");
            assertTrue(colors.size() >= 6, path + " lacks articulated armor shading");
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}

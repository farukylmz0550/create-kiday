package io.github.createkiday;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Cross-checks item registration against the recipe and model data that consumes those IDs. */
class ResourceIntegrationTest {
    @Test
    void kidayAndSalurCastingShareHotProductsWhileKeepingMetalIdentity() throws IOException {
        String registry = source("src/main/java/io/github/createkiday/registry/ModItems.java");
        assertTrue(registry.contains("hot(\"hot_ingot\")"));
        assertTrue(registry.contains("hot(\"hot_helmet\")"));

        for (String metal : new String[]{"kiday", "salur"}) {
            String cast = resource("data/create_kiday/recipe/casting_" + (metal.equals("kiday") ? "" : "salur_") + "ingot.json");
            String wash = resource("data/create_kiday/recipe/washing_" + metal + "_ingot.json");
            assertTrue(cast.contains("\"result\": \"create_kiday:hot_ingot\""));
            assertTrue(cast.contains("\"fluid\": \"create_kiday:molten_" + metal + "\""));
            assertTrue(cast.contains("\"metal\": \"" + metal + "\""));
            assertTrue(wash.contains("\"item\": \"create_kiday:hot_ingot\""));
            assertTrue(wash.contains("\"create_kiday:metal_type\": \"" + metal + "\""));
            assertTrue(wash.contains("\"id\": \"create_kiday:" + metal + "_ingot\""));
        }
    }

    @Test
    void diskAndLightningStaffRecipesUseOnlyRequestedMaterials() throws IOException {
        String disk = resource("data/create_kiday/recipe/casting_salur_disk.json");
        assertTrue(disk.contains("\"amount\": 432"));
        assertTrue(disk.contains("\"result\": \"create_kiday:hot_disk\""));
        assertTrue(resource("data/create_kiday/recipe/disk_mold.json").contains("\"result\": {"));

        String head = resource("data/create_kiday/recipe/lightning_staff_head.json");
        assertTrue(head.contains("\"SSS\""));
        assertTrue(head.contains("create_kiday:lei_gong_core"));
        String staff = resource("data/create_kiday/recipe/lightning_staff.json");
        assertTrue(staff.contains("\" A \""));
        assertTrue(staff.contains("create_kiday:kiday_sheet"));
    }

    @Test
    void bossAndSpiritLootUseTheConfiguredItemsAndChances() throws IOException {
        String boss = source("src/main/java/io/github/createkiday/entity/LeiGongEntity.java");
        String bossBalance = source("src/main/java/io/github/createkiday/entity/LeiGongBalance.java");
        assertTrue(boss.contains("LeiGongBalance.coreDrops(random.nextFloat())"));
        assertTrue(boss.contains("ModItems.LEI_GONG_CORE.get()"));
        assertTrue(bossBalance.contains("roll < 0.99F"));
        String spirit = resource("data/create_kiday/loot_table/entities/storm_spirit.json");
        assertTrue(spirit.contains("create_kiday:storm_amulet"));
        assertTrue(spirit.contains("\"chance\": 0.05"));
    }

    private static String resource(String path) throws IOException {
        try (InputStream input = ResourceIntegrationTest.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new AssertionError("Missing resource " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String source(String path) throws IOException {
        return java.nio.file.Files.readString(java.nio.file.Path.of(path));
    }
}

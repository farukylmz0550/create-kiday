package io.github.createkiday.bracelet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Retains the former bracelet regression suite as checks that removed content stays absent. */
class BraceletFlightRegistryTest {
    @Test
    void hunterBraceletCodeAndAssetsRemainRemoved() {
        assertFalse(Files.exists(Path.of("src/main/java/io/github/createkiday/bracelet")));
        assertFalse(Files.exists(Path.of("src/main/java/io/github/createkiday/item/HunterBraceletItem.java")));
        assertFalse(Files.exists(Path.of("src/main/java/io/github/createkiday/entity/HunterDiskEntity.java")));
        assertFalse(Files.exists(Path.of("src/main/resources/assets/create_kiday/textures/item/hunter_bracelet.png")));
        assertFalse(Files.exists(Path.of("src/main/resources/data/create_kiday/recipe/hunter_bracelet.json")));
    }

    @Test
    void registriesAndResourceValidationDoNotRestoreRemovedItemIds() throws Exception {
        String items = Files.readString(Path.of("src/main/java/io/github/createkiday/registry/ModItems.java"));
        String entities = Files.readString(Path.of("src/main/java/io/github/createkiday/registry/ModEntities.java"));
        String validator = Files.readString(Path.of("tools/validate_resources.py"));
        assertFalse(items.contains("hunter_bracelet"));
        assertFalse(items.contains("hunter_disk"));
        assertFalse(entities.contains("HUNTER_DISK"));
        assertTrue(validator.contains("hunter_bracelet"));
        assertTrue(validator.contains("hunter_disk"));
    }
}

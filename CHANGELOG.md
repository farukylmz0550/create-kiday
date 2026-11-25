# Changelog

## 1.4.0

### Added

- Added Salur ore, Raw Salur, ingots, sheets, blocks, molten fluid, tools, armor, and the shared metal-aware casting flow.
- Added the Biomass Generator and configurable biomass fuel tag, the returning Salur Disc, TNT Disc, LeiGong's Core, Lightning Staff, and Storm Spirit.
- Added the Storm Mechanism Create assembly recipe and the two new achievements, bringing the tree to 21 achievements (20 visible and one hidden).
- Added LeiGong's three boss phases, boss bar colors, lightning storm, and persistent spawning based on the connected Storm Grove chunk footprint.
- Added 21 localized advancements, deterministic pixel-art generation, a texture preview atlas, unit/resource/integration checks, GameTests, Jacoco coverage, and CI test artifacts.
- Added per-piece Salur armor movement speed and a Storm Grove undead daylight-burn fix.
- Added a translated Storm Spirit spawn egg to the Kiday creative tab.

### Changed

- LeiGong is now named LeiGong's Replica / LeiGong'un Kopyası and has 250 HP. Its guaranteed common drop is LeiGong's Core (99%); it no longer drops the Lightning Necklace.
- Storm Spirit uses Minecraft's built-in Blaze model and animation with a fully redrawn 64×32 LeiGong-palette texture; unused Blaze UVs are transparent.
- LeiGong spawn checks now flood-fill the bounded Storm Grove chunk footprint through biome lookups, count the connected biome area, and avoid loading or generating chunks during boundary detection.
- Storm Grove biome coordinates now use chunk-scaled region bounds, matching the LeiGong anchors to the generated biome patch.
- Lightning Staff's shared melee/lightning cooldown is now three seconds.
- Lightning Engine output now uses the four supported speeds: 16, 32, 64, and 128 RPM. Added the Biomass Generator as a second Create power source.
- Advancement parent fields now use Minecraft's top-level schema so all 21 achievements appear in one tree.
- Hot casting items now use shared `hot_*` IDs and textures; their metal type travels as an item data component.
- Waqwaq fruit ambient calls are scheduled at random 40–60 tick intervals through one client scheduler.
- Updated the version to 1.4.0. Previous worlds and items are not supported.

### Removed

- Removed Hunter Bracelet, its core/frame/mold and recipes, the hunter disk, and automatic targeting code.
- Removed the unused wrist Curios slot and obsolete hot-item models, textures, and recipes.

### Fixed

- Zombies and skeletons no longer burn from sunlight in Storm Grove; vanilla behavior elsewhere is preserved.
- Creative players retain Salur Disc durability and TNT Disc stacks. Survival Salur Discs break correctly on their last use.
- Create cooling recipes now filter the shared hot item by its metal component; unknown metal values cannot turn into Kiday equipment.
- Storm Spirit spawning no longer requires hostile-mob darkness, and its egg is available in the creative tab.

### Validation

- `./gradlew compileJava` passed.
- `./gradlew build allTests smokeTest regressionTest` passed.
- The JUnit task passed all 37 tests; the unit, integration, and resource-validation tasks report 22, 3, and 10 tests when run separately.
- The headless gameplay suite passed all 30 GameTests. The isolated Salur metallurgy E2E suite passed its 1 GameTest.
- Texture determinism check passed for 114 PNGs; resource validation parsed 417 JSON files.
- `./gradlew runClient -PkidayClientSmokeTest` passed, loading 102 item models and producing rendered screenshots, including front views of LeiGong and Storm Spirit.
- `./gradlew runServer` reached the dedicated server `Done` state with the mod, recipes, and Curios data loaded.
- JaCoCo overall line and branch coverage remain low because most Minecraft-bound production code is not covered by JVM-only tests; the four pure balance/ledger classes are at 95–100% line and 75–100% branch coverage.

<div align="center">
  <img src="docs/assets/logo.png" alt="Create: Kiday" width="360">
  <h1>Create: Kiday</h1>
  <p>A Create-powered metallurgy and adventure addon for Minecraft 1.21.1.</p>
</div>

Create: Kiday adds a new metal and its complete production chain to Create: find Kiday underground, melt it in a heated Basin, cast it with Patterns, and cool the castings with Bulk Washing. Explore new equipment, the Hunter Bracelet, the Lightning Engine, and the stormbound Lei Gong encounter.

**Release:** [1.1.0](https://github.com/farukylmz0550/create-kiday/releases/tag/1.1.0) · **License:** MIT

## Screenshots

<p align="center">
  <img src="docs/screenshots/production-line.png" alt="Kiday production line Ponder scene" width="49%">
  <img src="docs/screenshots/lightning-engine.png" alt="Lightning Engine Ponder scene" width="49%">
</p>
<p align="center">
  <img src="docs/screenshots/chronicle.png" alt="Kiday Chronicle in game" width="49%">
  <img src="docs/screenshots/molten-kiday.png" alt="Molten Kiday bucket in game" width="49%">
</p>

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.266 or later in the 21.1 line
- Create 6.0.x
- Curios 9.2.2 or later
- JEI 19.x (optional)

Install Create: Kiday and its required dependencies on both the client and server. Download the compiled `create_kiday_1.1.0.jar` from the [1.1.0 release](https://github.com/farukylmz0550/create-kiday/releases/tag/1.1.0).

## Getting started

1. Mine Kiday Ore in underground Overworld biomes. Veins generate between Y -48 and Y 48.
2. Process Raw Kiday in a Basin with a Mechanical Mixer at 32 RPM or faster and a normally heated Blaze Burner. Superheating is not needed. Each Raw Kiday yields 144 mB of Molten Kiday.
3. Move Molten Kiday through Create pipes or use its bucket. The placed fluid is luminous and damages entities, like lava.
4. Shape a Blank Pattern with a Deployer, then place the Pattern on a Casting Table. The table accepts exactly the fluid amount required by that Pattern; without a Pattern it accepts no fluid.
5. Remove the hot casting and cool it with Bulk Washing. Point a fan through water toward the item; suction and reverse airflow do not cool castings.

The Casting Table recipe uses three Iron Sheets across the top row, two Create Fluid Pipes around a Create Basin in the middle row, and three Andesite Alloys along the bottom row.

## Features

- **Kiday metallurgy:** underground ore, ingots, sheets, placeable molten fluid, twelve reusable Patterns, exact-volume casting, and individually textured hot castings.
- **Tools and armor:** a full Kiday tool set and armor set, with situational abilities and distinct in-game textures.
- **Hunter Bracelet:** launches a returning disk four blocks forward, targets hostiles within 16 blocks, and chains through up to five targets in one flight.
- **Lightning Engine:** redirects lightning in its loaded chunk to its custom rod, then generates Create rotation for a limited charge. Set output from 1 to 64 RPM with the Create value box on its back; power exits through the horizontal shaft on the front.
- **Storm Grove:** discover a stormbound biome and face Lei Gong's Avatar.
- **Kiday Chronicle:** read the story behind the metal and its smith.
- **Tag Filter:** filter items by dynamically loaded vanilla or modded item tags, with multiple tag selection, AND/OR matching, whitelist/blacklist modes, sample-item tag discovery, and matching previews.
- **Create integration:** Ponder scenes and JEI recipe displays explain the production and machine workflows.

## Documentation and development

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup, texture conventions, and contribution checks.

Use Java 21 and the checked-in Gradle wrapper:

```bash
./gradlew build
./gradlew test
./gradlew runGameTestServer
./gradlew runClient -PkidayClientSmokeTest
./gradlew runData
```

`./gradlew build` creates `build/libs/create_kiday_1.0.0.jar` and runs the local `installFragmanMod` copy task for the Modrinth `fragman` profile. To build without installing into that local game profile, run `./gradlew build -x installFragmanMod`.

## License

Create: Kiday source code is available under the [MIT License](LICENSE). Minecraft, NeoForge, Create, Curios, and JEI are trademarks or property of their respective owners.

# Third-Party Technology and Attribution Notices

## Minecraft attribution

**Create: Kiday is a community-made mod developed for Minecraft: Java Edition 1.21.1.**
Minecraft is developed and published by Mojang Studios/Microsoft. Minecraft names, game
code, and artwork belong to their respective owners and remain subject to the
[Minecraft End User License Agreement](https://www.minecraft.net/eula). Create: Kiday is
not an official Minecraft product and is not approved, endorsed, or sponsored by Mojang
Studios or Microsoft. For the official mod and name-use guidance, see [Minecraft Usage
Guidelines](https://www.minecraft.net/en-us/usage-guidelines).

The mod is intended to run with the Minecraft version and mod loader listed below. It is
distributed as an add-on; this repository does not include Minecraft's game files.

## Technologies used

The versions below are the versions configured or used for Create: Kiday 1.4.0. “API”
means the project is compiled against that library's interface; it does not mean its
source or artwork has been copied into this repository.

| Technology | Version | Use in this project | License | Official license / project source |
| --- | --- | --- | --- | --- |
| Minecraft: Java Edition | 1.21.1 | Target game; mod content and runtime | Minecraft EULA | [Minecraft EULA](https://www.minecraft.net/eula) |
| Java / Eclipse Temurin | Java 21 toolchain; local build used Temurin 21.0.13 | Compile and run the mod and tests | GPL-2.0-only with Classpath Exception (Temurin distribution may include additional notices) | [Eclipse Temurin licenses](https://projects.eclipse.org/projects/adoptium.temurin) |
| NeoForge | 21.1.266 | Minecraft mod loader and game APIs | LGPL-2.1-only | [NeoForge license notes](https://github.com/neoforged/NeoForge/blob/26.3.x/README-LICENSE.md) |
| Create | 6.0.10-281 | Kinetic systems, recipes, transport, and in-game integration | MIT for code; Create assets are All Rights Reserved | [Create license](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md) |
| Registrate | MC1.21-1.3.0+67 | Registry and content registration API | MPL-2.0 | [Registrate license](https://github.com/tterrag1098/Registrate/blob/1.21/dev/LICENSE) |
| Flywheel API | 1.0.6 | Create rendering API integration | MIT | [Flywheel source and license](https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/LICENSE.md) |
| Ponder | 1.0.82+mc1.21.1 | In-game guidance scenes | MIT | [Ponder license](https://github.com/Creators-of-Create/Ponder/blob/mc1.21.1/dev/LICENSE) |
| Curios API | 9.2.2+1.21.1 | Optional accessory-slot integration | LGPL-3.0-or-later | [Curios license](https://github.com/TheIllusiveC4/Curios/blob/1.21.x/LICENSE) |
| Just Enough Items (JEI) | 19.21.0.247 | Optional recipe-viewer integration | MIT | [JEI license](https://github.com/mezz/JustEnoughItems/blob/1.21.1/LICENSE.txt) |
| Parchment mappings | 2024.11.17 for Minecraft 1.21.1 | Readable parameter names and documentation during development | CC0-1.0 | [Parchment license](https://github.com/ParchmentMC/Parchment/blob/versions/1.21.x/LICENSE.txt) |
| ModDevGradle | 2.0.148 | NeoForge development and run configuration | LGPL-2.1-only | [ModDevGradle license](https://github.com/neoforged/ModDevGradle/blob/main/LICENSE) |
| Gradle | 9.2.1 wrapper | Build and verification tasks | Apache-2.0 | [Gradle license](https://github.com/gradle/gradle/blob/master/LICENSE) |
| JUnit Jupiter | 5.11.4 | Unit and integration tests (development only) | EPL-2.0 | [JUnit license](https://github.com/junit-team/junit-framework) |
| JaCoCo | 0.8.12 | Test coverage report (development only) | EPL-2.0 | [JaCoCo license](https://github.com/jacoco/jacoco/blob/v0.8.12/LICENSE.md) |
| Python | 3.15.2 in the local asset-generation environment | Deterministic resource and texture scripts | PSF-2.0 | [Python license information](https://docs.python.org/3/license.html) |
| Pillow | 12.3.0 in the local asset-generation environment | Pixel-art texture generation and validation (development only) | MIT-CMU | [Pillow license](https://github.com/python-pillow/Pillow/blob/main/LICENSE) |

## Original work and asset notes

Create: Kiday's original source code and original artwork are licensed under the MIT
License in `LICENSE`. Third-party licenses above apply to their respective technologies;
they do not change the license for original Kiday work. No Create or Luvi source code or
artwork is included in the new Kiday textures. New Salur and machine textures are
hand-authored 16×16 pixel art. The shaded Salur Sheet keeps the Kiday Sheet's silhouette
and light direction, translated into the Salur palette.

Create's asset license is separate from its code license. This project references Create
through its APIs and does not redistribute Create artwork. See the [Create license
details](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md) for
the code and asset terms.

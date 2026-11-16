# Contributing

## Setup

Install a Java 21 JDK, clone the repository, and run `./gradlew build`. Use `./gradlew runClient` for interactive development, `./gradlew runClient -PkidayClientSmokeTest` for automated client verification, `./gradlew runServer` for dedicated-server checks, `./gradlew runData` after changing generated data, and `./gradlew runGameTestServer` for GameTests.

## Code style

- Use four spaces in Java and UTF-8 in every text file.
- Prefer descriptive names and small, deterministic domain helpers.
- Keep client-only code out of common/server class loading paths.
- Format Java with the IDE's standard Java formatter; avoid unrelated rewrites.

## Textures

Runtime textures live under `src/main/resources/assets/create_kiday/textures/`. Use lowercase snake case and keep each tool, armor piece, Pattern, and hot casting visually distinct. The Casting Table and Lightning Engine each use six separate face textures: `top`, `back`, `bottom`, `front`, `right`, and `left`. Curios slot icons belong in `textures/slot/`; the wrist icon resource is `slot/wrist.png`. Lei Gong's entity texture belongs in `textures/entity/`.

After changing textures, load them in Minecraft and inspect the affected models and screens. Include current in-game screenshots in `docs/screenshots/` when a visual change affects player-facing content. Keep unrelated run output and development screenshots out of version control.

## Tests and commits

Add unit tests or GameTests for behavioral changes. Machine behavior must be verified in Minecraft, not only by compilation. Run `./gradlew test`, `./gradlew runGameTestServer`, the client smoke test when client code changed, and `./gradlew build` before submitting. Release JARs are named `create_kiday_<version>.jar`; keep generated JARs and local run data out of version control. Use short imperative commit subjects such as `Add casting table automation`; keep commits focused.

## Pull request checklist

- Explain what changed and why.
- List local validation performed.
- Include before/after screenshots for visible changes.
- Confirm no credentials, run directories, generated build output, or incompatible third-party assets are included.
- Link relevant issues.

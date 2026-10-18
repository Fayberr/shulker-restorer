# Shulker Restorer

Fabric mod for **Minecraft Java Edition 26.3**. Restores selected **Minecraft 1.20.5 Shulker and Shulker Bullet mechanics** to help older technical farms behave more like they did originally.

## What it restores

| Mechanic | 1.20.5 | 26.3 vanilla | With Shulker Restorer |
| --- | --- | --- | --- |
| Shulker Bullet redirects another projectile | `AIM_DEFLECT` | Shulker-specific `MOMENTUM_DEFLECT` | 1.20.5 redirection via base `Projectile` |
| Bullet collision processing | Before bullet movement | After movement and block effects | Hit applied before bullet movement |
| Shulker Bullet raycast margin | Constant `0.3F` | Builds up from `0.0F` to `0.3F` over early ticks | Constant `0.3F` |
| Shulker teleport destination tag | No `#shulker_does_not_teleport_to` restriction | New tag blocks some teleport targets | Tag restriction skipped for Shulkers only |

No boats become invulnerable; no universal collision or damage rules are replaced.

## Install

- **Minecraft Java 26.3**
- **Fabric Loader 0.19.5+**
- **Java 25**
- No Fabric API required
- **Install on the server** (or in singleplayer). Connecting clients do not require it.

Download the `shulker-restorer-<version>.jar` from the latest successful [GitHub Actions Build](https://github.com/Fayberr/shulker-restorer/actions/workflows/build.yml) artifacts, unzip the artifact package, and place the jar in the server's `mods` folder.

## Build from source

Run `./gradlew clean build` on Linux/macOS or `gradlew.bat clean build` on Windows, with JDK 25. The distributable jar is in `build/libs/` (select the jar without `-sources`).

GitHub Actions verifies the mod compiles **and** attempts a headless dedicated-server startup smoke test. This is still not a proof of long-term farm operation.

## Important limitations

This is a **targeted restoration of identified 1.20.5 mechanics**, not a full recreation of all Minecraft 1.20.5 internals. Vehicle / passenger physics, entity collision systems, and unrelated differences may still affect older farms.

The old 1.28k/h Shulker Farm was already known to have intermittent reliability issues before 26.3. **No guarantee of unattended multi-day reliability.** Long-term controlled testing remains necessary.

See [Mechanics and test protocol](docs/MECHANICS.md).

## References

- [Minecraft Java Edition 1.20.5 release decompiled Mojang mappings](https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/tree/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity)
- [Minecraft 26.3 ShulkerBullet](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/shulkerbullet/)
- [Minecraft 26.3 ProjectileUtil](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectileutil/)
- [Minecraft 26.3 Shulker](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/monster/shulker/)

MIT licensed; not affiliated with Mojang or Microsoft.

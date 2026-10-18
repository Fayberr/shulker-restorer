# Shulker Restorer

Fabric mod for **Minecraft Java Edition 26.3**. Its purpose is to restore relevant **1.20.5 Shulker Bullet mechanics** so older technical farms can be tested without changing the boats themselves.

## What it restores

| Mechanic | 1.20.5 | 26.3 vanilla | With Shulker Restorer |
| --- | --- | --- | --- |
| Shulker Bullet redirects another projectile | `AIM_DEFLECT` | Shulker-specific `MOMENTUM_DEFLECT` | 1.20.5 redirection via base `Projectile` |
| Bullet collision processing | Before bullet movement | After movement and block effects | Hit applied before bullet movement |
| Projectile raycast collision margin for Shulker Bullets | Constant `0.3F` | Grows from `0.0F` to `0.3F` over early ticks | Constant `0.3F` |

All three modifications are **scoped to Shulker Bullets**. No boats become invulnerable; no universal collision or damage rules are replaced.

## Install

- **Minecraft Java 26.3**
- **Fabric Loader 0.19.5+**
- **Java 25**
- No Fabric API required
- **Install on the server** (or into the singleplayer instance). Player clients on a dedicated server do not need the mod.

Download the `shulker-restorer-<version>.jar` from the latest successful [GitHub Actions Build](https://github.com/Fayberr/shulker-restorer/actions/workflows/build.yml) artifacts, unzip the artifact package, and place the jar in the server's `mods` folder.

## Build from source

Run `./gradlew clean build` on Linux/macOS or `gradlew.bat clean build` on Windows, with JDK 25. The distributable jar will be in `build/libs/` (use the jar **without** `-sources`).

## What this DOES NOT claim

This is a **targeted compatibility restoration**, not a full port of Minecraft 1.20.5 into 26.3. Shulker targeting, teleport destination rules, passengers, armor stand behavior, projectile/boat entity physics and other changed systems still follow 26.3 unless expressly patched.

The 1.28k Shulker Farm was reportedly unreliable even before 26.3, so there is **no guarantee** this makes it maintenance-free. Real long-term testing is required.

See [Mechanics and test protocol](docs/MECHANICS.md) for the precise source comparison and suggested verification.

## Sources

- [Minecraft 1.20.5 release (decompiled Mojmap reference)](https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/tree/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity)
- [Minecraft 26.3 ShulkerBullet](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/shulkerbullet/)
- [Minecraft 26.3 ProjectileUtil](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectileutil/)
- [Minecraft 26.3 Shulker](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/monster/shulker/)

MIT licensed; not affiliated with Mojang or Microsoft.

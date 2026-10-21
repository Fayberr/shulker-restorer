# Shulker Restorer

**Fabric mod for Minecraft Java 26.3**, aimed at restoring Shulker / Shulker Bullet behavior from the **Minecraft 1.20.5 release**.

## Direct download

After the GitHub build succeeds, download the compiled jar directly:
**[shulker-restorer-1.1.0.jar](https://github.com/Fayberr/shulker-restorer/releases/download/v1.1.0/shulker-restorer-1.1.0.jar)**

Requires Java 25 and Fabric Loader 0.19.5+. Install on a dedicated server, or in the singleplayer installation. Fabric API is not required. This is a server-side mechanics patch; clients joining a modded dedicated server do not need to install it.

## Implemented changes

| Behavior | 1.20.5 reference | 26.3 without mod | With this mod |
|---|---|---|---|
| Redirected projectiles hit by Shulker Bullets | Generic `AIM_DEFLECT` | Shulker-specific `MOMENTUM_DEFLECT` | Generic `AIM_DEFLECT` |
| Bullet collision timing | Before moving | After moving / block effects | Before moving |
| Entity margin for bullet raycast | Constant `0.3F` | Gradually grows to `0.3F` | Constant `0.3F` |
| Block raycast vs world border | `Level.clip` | `clipIncludingBorder` | `Level.clip` |
| Shulker teleport target blacklist | Not present | Additional `#shulker_does_not_teleport_to` restriction | Additional blacklist is ignored for Shulkers only |

Only Shulker / Shulker Bullet methods are patched. Unrelated projectiles, boats, block damage and generic entity rules retain 26.3 behavior.

## Verification limits

- JAR compiles with Minecraft 26.3, Java 25 and Fabric Loom.
- Version 1.1.0 passed a dedicated-server startup, Shulker and Shulker Bullet summon, 200-tick run, save and shutdown check on 21 October 2026.
- Sustained operation still needs testing in the target farm; the startup check does not measure farm rates or long-term reliability.
- **The 1.28k/h boat-based Shulker Farm was unreliable even before 26.3; this mod cannot guarantee maintenance-free operation.**
- The aim is to restore the verified differences relevant to these mechanics. It does not embed or replace the entire Minecraft 1.20.5 implementation, and some other engine behavior remains 26.3.
- The new 26.3 block-tag restriction intentionally is bypassed for Shulkers, including datapack additions to the tag.

See [technical comparison and test protocol](docs/MECHANICS.md).

## Build

`./gradlew clean build` (JDK 25). Only **one GitHub Actions workflow** is provided, which builds the jar and publishes it to the GitHub release for a directly downloadable single jar. No farm testing is run in GitHub Actions.

MIT license; not affiliated with Mojang or Microsoft.

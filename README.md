# Shulker Restorer

A focused **Fabric** mod for **Minecraft Java Edition 26.3** that restores the **pre-26.3 Shulker Bullet projectile-redirection behavior**.

## What it fixes

In 26.2, the general `Projectile.onHit` handler used `ProjectileDeflection.AIM_DEFLECT` when the projectile hit a redirectable projectile. In 26.3, Mojang added a Shulker Bullet-specific `onRedirectProjectile` override using `MOMENTUM_DEFLECT`.

Shulker Restorer makes `ShulkerBullet.onRedirectProjectile` delegate to the vanilla **base-class implementation** (`Projectile.onRedirectProjectile`) again. The base implementation uses `AIM_DEFLECT` and is equivalent to the 26.2 redirection path for Shulker Bullets.

This is **not** boat immunity. No boat classes or general entity damage rules are patched, and Shulker Bullets can still hurt boats when they hit them. The mod changes the behavior that causes bullets to be redirected differently in 26.3.

## Compatibility

- Minecraft **26.3** only (not 26.2 / 26.4).
- Fabric Loader **0.19.5+**.
- **Java 25**.
- No Fabric API dependency.
- **Install on the server** (or locally for a single-player world). It does not need to be installed on every connecting client; server-side game mechanics control this interaction.

## Build

Run `./gradlew build` on Linux/macOS, or `gradlew.bat build` on Windows, with JDK 25. The distributable jar will be under `build/libs/` (choose the jar without `-sources`).

Alternatively download the built jar from the **Build** GitHub Actions run artifacts on `main`.

## Scope and limitations

This mod is a precise rollback of the Shulker-specific projectile redirection introduced in 26.3. It does **not** roll back every projectile-physics change in Minecraft and does **not** guarantee the old 1.28k/h Shulker Farm will be indefinitely reliable. The original farm was already considered unreliable before 26.3. In particular, directional boat ejection, armor stand alignment, and other unrelated issues remain unchanged.

**Status: experimental.** Compilation is checked by CI; farm reliability must be established with a long-running, in-game stress test. Check `docs/MECHANICS.md` for a source-backed explanation and a reproducible test protocol.

## Sources

- 26.2-snapshot-2 decompiled reference (read-only research; do not copy Mojang sources into this repo): https://github.com/ohnodev/decompiled-minecraft-26-2-SNAPSHOT-2
- 26.3 ShulkerBullet: https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/shulkerbullet/
- 26.3 Projectile: https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectile/
- 26.3 ProjectileDeflection: https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectiledeflection/
- Mojang bug fix MC-260780: https://feedback.minecraft.net/hc/en-us/articles/48078031305101-Minecraft-Java-Edition-26-3-Snapshot-8

## License

MIT. Not affiliated with Mojang or Microsoft.

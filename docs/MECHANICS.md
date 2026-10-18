# Shulker Restorer: 1.20.5 vs. 26.3

## Scope and reference

Actual **Minecraft Java 1.20.5 release commit**, not a prerelease:
- Reference commit: `c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6`
- Bullet: https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/blob/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity/projectile/ShulkerBullet.java
- Projectile: https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/blob/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity/projectile/Projectile.java
- ProjectileUtil: https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/blob/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity/projectile/ProjectileUtil.java
- Shulker: https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/blob/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity/monster/Shulker.java

26.3 references:
- Bullet: https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/shulkerbullet/
- Projectile: https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectile/
- ProjectileUtil: https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectileutil/
- Shulker: https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/monster/shulker/

These are **reference links only**. Mojang's decompiled source is not copied into this mod.

## Verified differences implemented

### 1. Projectile redirection

Minecraft 1.20.5 `Projectile.onHit`: redirectable projectile targets receive `AIM_DEFLECT` using the hitting projectile's owner.

26.3 `ShulkerBullet.onRedirectProjectile`: `MOMENTUM_DEFLECT` with the Shulker Bullet itself as the redirecting entity. This changes the direction of reflected projectiles.

**Fix:** `ShulkerBulletMixin` delegates to the base `Projectile.onRedirectProjectile`, whose 26.3 `AIM_DEFLECT` path is the closest current equivalent to the 1.20.5 logic.

### 2. Collision-before-movement order

1.20.5 `ShulkerBullet.tick()`:
1. Run homing physics.
2. Compute hit result with `ProjectileUtil.getHitResultOnMoveVector`.
3. Immediately call `hitTargetOrDeflectSelf` on a hit.
4. Run block checks, move, and rotate.

26.3 `ShulkerBullet.tick()`:
1. Run homing physics.
2. Compute hit result.
3. Move, apply block effects / portal logic.
4. Process hit only afterward, and only if still alive.

**Fix:** Redirect Shulker Bullet's internal raycast invocation. Apply the hit immediately and return `null` so that the 26.3 deferred hit branch cannot process it twice. This restores the most relevant ordering; **block-checking and portal implementation remain 26.3**.

### 3. Collision margin

1.20.5 `ProjectileUtil.getHitResultOnMoveVector` passes a constant `0.3F` entity margin.

26.3 delegates to `computeMargin(source)`, which expands the margin over the early projectile ticks.

**Fix:** `ProjectileUtilMixin` returns `0.3F` specifically when `source instanceof ShulkerBullet`. No other projectile's margin changes.

## Differences reviewed but intentionally unchanged

- **Shulker duplication:** Both versions use an open shulker, successful teleport, and `(nearbyShulkers - 1) / 5.0F` failure chance. Core spawning logic is effectively the same.
- **Shulker teleportation:** In 26.3 there is a new `#shulker_does_not_teleport_to` block tag. Disabling it would also override intentional world/datapack teleport restrictions; it has **not** been globally disabled.
- **Boats and passengers:** 1.20.5 vs. 26.3 changes outside the bullet logic could affect directional ejection. No blanket patches.
- **World border and generic projectile collisions:** Current 26.3 behavior is retained.
- This change **does not** claim byte-for-byte 1.20.5 gameplay equivalence.

## Validation plan

1. Build using JDK 25 with `./gradlew clean build` and inspect Mixin compilation.
2. Launch Minecraft 26.3 with Fabric, confirm Mixin application without `InvalidInjectionException` and with `shulker-restorer` visible in loaded mods.
3. Test isolated Shulker Bullet projectile deflections with and without the mod.
4. Run the exact 1.28k/h Shulker Farm schematic in two controlled worlds: vanilla 26.3 vs. patched 26.3.
5. Use identical seeds, orientation, player position, spawn loading, and shulker alignment.
6. Run at least 20 real server hours and ideally 100+. Record each boat loss, ejection failure, spawning rate, entities, TPS/MSPT, and shell count.
7. If the farm breaks with restored bullet behavior, isolate other systems (boat passenger physics, teleport candidates, entity collisions) and add fixes **only when confirmed**.

CI success confirms buildability; it does not establish that mixins apply in a running game or that a farm stays reliable for hours.

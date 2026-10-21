# Mechanics verification: Minecraft 1.20.5 vs 26.3

## Primary source comparison

Actual 1.20.5 release commit: [Mojang-mapped decompilation](https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/tree/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity).

Relevant Minecraft 26.3 source:
- [ShulkerBullet.java](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/shulkerbullet/)
- [ProjectileUtil.java](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectileutil/)
- [Shulker.java](https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/monster/shulker/)

No decompiled Mojang source files are distributed in this repository.

## Explicit restores

1. **Redirectable projectile deflections**: In 1.20.5, `Projectile.onHit` uses the generic `AIM_DEFLECT` strategy. Minecraft 26.3 adds a ShulkerBullet-specific `onRedirectProjectile` override using `MOMENTUM_DEFLECT`. The `ShulkerBulletMixin` cancels that override and delegates to `super.onRedirectProjectile`, which keeps the old `AIM_DEFLECT` semantics for the affected call.

2. **Collision ordering**: In 1.20.5, ShulkerBullet resolves a raycast hit before bullet movement. In 26.3 it resolves the hit after moving and processing block effects. The `@Redirect` Mixin invokes a 1.20.5-style raycast and processes its hit immediately, returning `null` so the deferred 26.3 handler is skipped. Bullet movement and general ticking remain under current game APIs. Older `checkInsideBlocks` calls cannot be transplanted literally because 26.3 has a redesigned block/portal processing lifecycle.

3. **Raycast details**: In 1.20.5, `ProjectileUtil.getHitResultOnMoveVector` uses constant `0.3F` to inflate entity hitboxes, clips blocks with `Level.clip`, limits the raycast to the earliest block hit, then checks potential entities inside the expanded swept AABB. The new `LegacyBulletCollision.raycast` reconstructs this for Shulker Bullets, calling the current `ProjectileUtil.getEntityHitResult` overload that uses the same entity intersection criterion. This also avoids changing the collision behavior of all other projectiles.

4. **Teleport target blacklist**: Minecraft 26.3 additionally rejects attachable blocks tagged `SHULKER_DOES_NOT_TELEPORT_TO`, introduced since 1.20.5. The `ShulkerMixin` selectively ignores that extra check. The standard teleport location, world border, attachable face and collision checks remain intact.

## Reviewed / unchanged

- The Shulker Bullet constructor still spawns the projectile at the owner's bounding-box center, as it did in 1.20.5.
- Bullet homing uses the same basic acceleration, targetDelta changes and random 10-50-step direction planning.
- The Shulker duplication procedure still depends on a successful teleport and nearby Shulker population, with failure probability `(count - 1) / 5.0F`. No non-vanilla duplication bonus.
- Damage, levitation, sounds and projectile destruction remain current vanilla, where corresponding gameplay behavior matches.
- World / portal internals and `Projectile.hitTargetOrDeflectSelf` have evolved; this is a targeted compatibility patch, not byte-for-byte restoration of every superclass and physics implementation.
- Boat eject direction and related passenger mechanics are not patched.
- The restored `Level.clip` semantics mean some 26.3 world-border interactions differ when the mod is installed. This is intentional for Shulker Bullets only.

## Validation record

- Earlier version (commit `072aa12`) built successfully and passed a dedicated-server startup + Shulker / Shulker Bullet summon smoke test.
- Version 1.1.0 built and passed a dedicated-server startup, Shulker and Shulker Bullet summon, 200-tick run, save and shutdown check on 21 October 2026. This does not test sustained farm operation.
- The GitHub workflow is deliberately limited to a single build + release workflow at the user's request.

### Remaining definitive farm test

A 1.28k/h farm world or schematic is required. Paste/build into a fresh Minecraft 26.3 world, align the armor stand and boats as the original designer specified, and compare the mod against vanilla 26.3 under identical conditions. Run long enough to detect random failures (at least 20 hours, ideally 100+ hours), log boat destruction, rates and ejection. Any persistent failure may be due to unrelated boat ejection or other version changes, requiring follow-up targeted patches.

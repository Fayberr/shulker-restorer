# Mechanics and verification

## Why a boat-immunity workaround is intentionally not used

Making boats invulnerable to Shulker Bullets would hide one symptom by changing an unrelated Minecraft mechanic. This mod instead targets the regression identified in the 26.3 Shulker Bullet projectile-redirection logic.

## Source comparison

### Before 26.3 (26.2-snapshot-2)

- `ShulkerBullet` has no `onRedirectProjectile` override.
- The `Projectile.onHit` handler invokes `hitProjectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.owner, true)` when an entity hit is a redirectable projectile.
- `AIM_DEFLECT` sets redirected projectile movement according to the deflecting entity's look direction.

Reference: https://github.com/ohnodev/decompiled-minecraft-26-2-SNAPSHOT-2/tree/main/src/net/minecraft/world/entity/projectile

### Minecraft 26.3

- The generic `Projectile.onHit` calls `this.onRedirectProjectile(hitProjectile)`.
- The generic `Projectile.onRedirectProjectile` uses `AIM_DEFLECT` with a power of 1.0; its 26.3 implementation remains equivalent to the 26.2 redirection path.
- `ShulkerBullet` overrides the generic hook and uses `MOMENTUM_DEFLECT` with the Shulker Bullet as deflector instead.
- In 26.3, `MOMENTUM_DEFLECT` obtains the deflecting entity's known speed, normalizes it, and redirects the hit projectile along it. That can change subsequent collisions.

Reference (current source): https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/shulkerbullet/

### This mod

Mixin: `ShulkerBulletMixin` intercepts `ShulkerBullet.onRedirectProjectile`, delegates to `super.onRedirectProjectile(hitProjectile)`, then cancels the Shulker-specific body. This restores the old *Shulker Bullet redirection branch* rather than modifying boats, hitboxes, damage, velocities of unrelated projectiles, or general collision rules.

`ProjectileDeflection.AIM_DEFLECT` in 26.3 still uses the deflector's look vector; the added power argument is 1.0, so this path matches its earlier one-vector behavior.

## Suggested manual tests (not yet performed)

1. Start **two fresh 26.3 Fabric test worlds** with identical seeds, structures and farm conditions: one without the mod (control) and one with Shulker Restorer.
2. Verify mod load in server log and compare redirectable-projectile trajectories using a small reproducible setup.
3. Paste the exact 1.28k/h farm schematic on both worlds, align the armor stand as required by the designer, and measure boat lifetime and accumulated Shulker Shells.
4. Run at least **20 hours** of simulated game time with actual server ticks and preferably **100+ hours** for evidence of reliability. A 50-minute test is not a reliability proof.
5. Record `/tick query` (if available), TPS/MSPT, bullet count, boat disappearance, ejection state, rates/hour, and versions/mod list. Compare control and patched cases.
6. Test both playerless and playerful farm variants if relevant to the design. The 1.28k/h name may refer to a playerful condition.

If boats still break, investigate other causes independently. Avoid claiming all older farms are fully restored merely because redirect behavior is patched.

## Why no global 26.2 rollback?

26.3 includes unrelated projectile updates. This mod restricts itself to the confirmed new, Shulker-specific branch so the fix does not accidentally regress unrelated arrows, fireballs, fishing rods, players, or boats.

The old 1.28k/h farm was unreliable even before 26.3; this mod only removes the specific newer regression.

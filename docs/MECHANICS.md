# Shulker Restorer: mechanics provenance and testing

## Verified source versions

**1.20.5 release commit:** `c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6` in
https://github.com/Moxie-Stuff/MinecraftDeobfuscated-Mojang/tree/c7bfd400c4b15bfdac823d6872e83f87a5d7cdf6/minecraft/src/net/minecraft/world/entity

**Minecraft 26.3:**
- https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/shulkerbullet/
- https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectile/
- https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/projectile/projectileutil/
- https://mc-packet-reference.netlify.app/26.x/source/net/minecraft/world/entity/monster/shulker/

These are research references; no Mojang decompiled code is distributed in the repository.

## Changes implemented, scoped to relevant entities

1. **Projectile redirection:** 1.20.5 used `AIM_DEFLECT` from `Projectile.onHit`. In 26.3 Shulker Bullets uniquely override `onRedirectProjectile` and redirect with `MOMENTUM_DEFLECT`. The ShulkerBulletMixin delegates to 26.3's base implementation, equivalent to the old `AIM_DEFLECT` path.

2. **Hit processing order:** 1.20.5 ShulkerBullet.tick calls `hitTargetOrDeflectSelf` immediately after its raycast, before movement. In 26.3 the game calculates the raycast, moves the bullet, applies block and portal effects, and then processes the hit. ShulkerBulletMixin redirects the bullet's own raycast, processes any hit immediately, and returns null to prevent the late 26.3 branch running a second time. This deliberately retains 26.3 block and portal APIs.

3. **Raycast margin:** 1.20.5 uses 0.3F on every projectile movement raycast; 26.3 computes a progressively increasing margin based on tick age. ProjectileUtilMixin returns 0.3F for Shulker Bullets only.

4. **Shulker teleport candidates:** 26.3 adds `BlockTags.SHULKER_DOES_NOT_TELEPORT_TO` as an extra candidate filter in `Shulker.teleportSomewhere`. The tag did not exist in 1.20.5. ShulkerMixin skips that one filter for Shulkers while retaining all other vanilla teleport checks, including world border, attach face, and collision restrictions. This intentionally bypasses datapack restrictions attached to that specific new 26.3 tag.

## Unchanged mechanics / exclusions

- **Duplication:** Both reference versions require the Shulker to be open and teleport successfully; the nearby-Shulker-based failure chance remains `(count - 1) / 5.0F`. No duplication-rate modifications.
- **Projectile spawn location:** Both versions place a new bullet at the shooter bounding-box center; no separate fix required.
- **Generic projectile physics and boat durability:** Left at 26.3. No boat immunity.
- **General teleport movement, vehicle passenger ejection and entity raycasting changes:** Not backported without evidence they specifically break the farm.
- **World border raycast implementation:** Still uses 26.3 `clipIncludingBorder`.
- This mod is NOT an exact byte-for-byte 1.20.5 runtime emulation.

## Test matrix (not completed merely by building)

- **CI:** Compile with JDK 25 and try booting a dedicated 26.3 Fabric server. A passing boot only confirms the game's initial classloading, not every on-demand entity transformation.
- **Targeted tests:** Summon Shulkers and Shulker Bullets in a test world, check mixin runtime errors, compare projectile redirection trajectories, hit effects and teleport destinations.
- **Farm A/B:** Same orientation, identical playerful/playerless state, seeds, farm schematics, armor stands and boats in 26.3 without the mod (control) and with the mod.
- **Reliability:** At least 20 hours of real server ticks and preferably 100+ hours, tracking shell yield, failure time, boats broken, ejection direction, Shulker count, TPS/MSPT and lag.
- **If failure persists:** Investigate boat passenger mechanics, entity hitbox changes or unrelated version regressions, before any broad additional patch.

Build + server smoke success does not warrant claiming 1.28k/h all-day reliability.

package dev.fayber.shulkerrestorer;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A narrowly scoped adapter for the 1.20.5 movement raycast used by a Shulker
 * Bullet. No change is made to the global ProjectileUtil implementation.
 *
 * Differences from 26.3:
 * - a constant 0.3F entity margin, from the very first tick;
 * - Level.clip instead of Level.clipIncludingBorder.
 *
 * The entity intersection routine in 26.3 accepts the same inputs used in
 * 1.20.5 and is reused rather than copied wholesale.
 */
public final class LegacyBulletCollision {
    private static final float LEGACY_ENTITY_MARGIN = 0.3F;

    private LegacyBulletCollision() {
    }

    public static HitResult raycast(Entity bullet, Predicate<Entity> matching) {
        Vec3 from = bullet.position();
        Vec3 movement = bullet.getDeltaMovement();
        Vec3 to = from.add(movement);
        Level level = bullet.level();

        HitResult blockHit = level.clip(
            new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bullet)
        );
        if (blockHit.getType() != HitResult.Type.MISS) {
            to = blockHit.getLocation();
        }

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            level,
            bullet,
            from,
            to,
            bullet.getBoundingBox().expandTowards(movement).inflate(1.0),
            matching,
            LEGACY_ENTITY_MARGIN
        );
        return entityHit != null ? entityHit : blockHit;
    }
}

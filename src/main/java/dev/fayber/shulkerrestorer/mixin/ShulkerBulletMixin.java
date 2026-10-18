package dev.fayber.shulkerrestorer.mixin;

import dev.fayber.shulkerrestorer.LegacyBulletCollision;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restore the pre-26.3 redirect behavior and the 1.20.5 hit ordering for
 * Shulker Bullets without changing boats, other projectiles, or world rules.
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {
    protected ShulkerBulletMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Inject(
        method = "onRedirectProjectile(Lnet/minecraft/world/entity/projectile/Projectile;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 1
    )
    private void shulkerRestorer$restoreLegacyRedirect(Projectile hitProjectile, CallbackInfo ci) {
        // 1.20.5 used AIM_DEFLECT from the generic Projectile hit handler.
        // The 26.3 base hook retains this action with the default 1.0 power.
        super.onRedirectProjectile(hitProjectile);
        ci.cancel();
    }

    /**
     * In 1.20.5 the collision was resolved before moving the bullet.
     * By calculating and resolving the hit inside this invocation we recover
     * that order. Returning null skips the deferred 26.3 hit handler, so the
     * collision cannot be applied twice.
     *
     * The helper also reproduces the 1.20.5 raycast's fixed 0.3 entity
     * margin and block clipping without 26.3's additional border handling.
     */
    @Redirect(
        method = "tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;"
        ),
        require = 1
    )
    private HitResult shulkerRestorer$resolveLegacyCollision(Entity source, Predicate<Entity> matching) {
        HitResult hit = LegacyBulletCollision.raycast(source, matching);
        if (hit != null && hit.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hit);
        }
        return null;
    }
}

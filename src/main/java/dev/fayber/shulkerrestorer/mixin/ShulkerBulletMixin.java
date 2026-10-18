package dev.fayber.shulkerrestorer.mixin;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restore Shulker Bullet mechanics found in the Minecraft 1.20.5 release.
 * Only ShulkerBullet is affected; other projectile types retain 26.3 behavior.
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {
    protected ShulkerBulletMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    /**
     * Since 26.3, ShulkerBullets override redirect behavior with MOMENTUM_DEFLECT.
     * In 1.20.5 the ordinary Projectile hit path used AIM_DEFLECT instead.
     * Delegating to the 26.3 base-class implementation restores that behavior.
     */
    @Inject(
        method = "onRedirectProjectile(Lnet/minecraft/world/entity/projectile/Projectile;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void shulkerRestorer$restoreLegacyRedirect(Projectile hitProjectile, CallbackInfo ci) {
        super.onRedirectProjectile(hitProjectile);
        ci.cancel();
    }

    /**
     * In 1.20.5 the Shulker Bullet performed its raycast and applied the hit
     * BEFORE moving for the tick. 26.3 collects the hit, moves, processes block
     * effects, and only then applies the hit. Intercept the sole raycast in
     * ShulkerBullet.tick, handle it immediately, and give vanilla no deferred
     * result to process.
     *
     * This intentionally does not alter global ProjectileUtil logic and it
     * does not grant any entity (including boats) invulnerability.
     */
    @Redirect(
        method = "tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;"
        ),
        require = 1
    )
    private HitResult shulkerRestorer$applyHitBeforeMovement(Entity source, Predicate<Entity> matching) {
        HitResult result = ProjectileUtil.getHitResultOnMoveVector(source, matching);
        if (result != null && result.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(result);
        }
        // 26.3's late hit branch is guarded by result != null.
        return null;
    }
}

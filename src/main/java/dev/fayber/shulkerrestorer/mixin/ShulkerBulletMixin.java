package dev.fayber.shulkerrestorer.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In 26.2, ShulkerBullet inherited the normal Projectile redirection path:
 * AIM_DEFLECT from the bullet owner's look direction.
 *
 * 26.3 introduced a Shulker-specific MOMENTUM_DEFLECT override.
 * Delegate to the vanilla base-class implementation to restore the old
 * general redirection behavior. No boat-specific changes are made.
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {
    protected ShulkerBulletMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Inject(
        method = "onRedirectProjectile(Lnet/minecraft/world/entity/projectile/Projectile;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void shulkerRestorer$restoreLegacyRedirect(Projectile hitProjectile, CallbackInfo ci) {
        super.onRedirectProjectile(hitProjectile);
        ci.cancel();
    }
}

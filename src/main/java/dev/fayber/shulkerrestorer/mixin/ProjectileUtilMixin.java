package dev.fayber.shulkerrestorer.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.20.5 ProjectileUtil used a constant 0.3F margin for all projectile raycasts.
 * In 26.3 it scales from zero to 0.3F over the first ~8 ticks.
 * Roll back this difference only for ShulkerBullets.
 */
@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {
    @Inject(
        method = "computeMargin(Lnet/minecraft/world/entity/Entity;)F",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void shulkerRestorer$legacyShulkerBulletHitMargin(Entity source, CallbackInfoReturnable<Float> cir) {
        if (source instanceof ShulkerBullet) {
            cir.setReturnValue(0.3F);
        }
    }
}

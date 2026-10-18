package dev.fayber.shulkerrestorer.mixin;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Minecraft 26.3 introduced an additional SHULKER_DOES_NOT_TELEPORT_TO block
 * tag check. This check did not exist in Minecraft 1.20.5.
 *
 * Intercept only the tag check in Shulker.teleportSomewhere rather than
 * changing BlockState, block tags, or teleportation rules for other mobs.
 */
@Mixin(Shulker.class)
public abstract class ShulkerMixin {
    @Redirect(
        method = "teleportSomewhere()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"
        ),
        require = 1
    )
    private boolean shulkerRestorer$restoreLegacyTeleportTargets(BlockState state, TagKey<Block> tag) {
        if (tag == BlockTags.SHULKER_DOES_NOT_TELEPORT_TO) {
            return false;
        }
        return state.is(tag);
    }
}

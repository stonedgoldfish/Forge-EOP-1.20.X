package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.stonedgoldfish.eopmod.power.ability.SilentStepsAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(
            method = "spawnSprintParticle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eopmod$cancelSprintParticles(CallbackInfo ci) {
        if ((Object) this instanceof Player player &&
                SilentStepsAbility.hasSilentSteps(player)) {
            ci.cancel();
        }
    }
}
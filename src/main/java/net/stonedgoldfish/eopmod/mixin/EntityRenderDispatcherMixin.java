package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.stonedgoldfish.eopmod.power.ability.InvisibilityAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(
            method = "renderShadow",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void eop$cancelInvisibleEntityShadow(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer,
            Entity entity,
            float weight,
            float partialTicks,
            net.minecraft.world.level.LevelReader level,
            float size,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci
    ) {
        if (entity instanceof LivingEntity living
                && InvisibilityAbility.isInvisible(living)) {
            ci.cancel();
        }
    }
}
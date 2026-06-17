package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.stonedgoldfish.eopmod.client.render.DimensionalSkyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(
            method = "getSkyColor",
            at = @At("RETURN"),
            cancellable = true
    )
    private void eop$changeSkyColor(Vec3 cameraPos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Minecraft minecraft = Minecraft.getInstance();

        float influence = DimensionalSkyHandler.getSkyInfluence(minecraft, partialTick);

        if (influence <= 0.0F) {
            return;
        }

        cir.setReturnValue(DimensionalSkyHandler.lerpColor(
                cir.getReturnValue(),
                DimensionalSkyHandler.TARGET_SKY,
                influence
        ));

        Vec3 color = DimensionalSkyHandler.lerpColor(
                cir.getReturnValue(),
                DimensionalSkyHandler.TARGET_SKY,
                influence
        );

        float flash = DimensionalSkyHandler.getLightningFlash(partialTick);

        if (flash > 0.0F) {
            color = DimensionalSkyHandler.lerpColor(
                    color,
                    new Vec3(0.65D, 0.95D, 0.75D),
                    flash * 0.75F
            );
        }

        cir.setReturnValue(color);
    }

    @Inject(
            method = "getCloudColor",
            at = @At("RETURN"),
            cancellable = true
    )
    private void eop$changeCloudColor(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Minecraft minecraft = Minecraft.getInstance();

        float influence = DimensionalSkyHandler.getSkyInfluence(minecraft, partialTick);

        if (influence <= 0.0F) {
            return;
        }

        cir.setReturnValue(DimensionalSkyHandler.lerpColor(
                cir.getReturnValue(),
                DimensionalSkyHandler.TARGET_CLOUDS,
                influence
        ));

        Vec3 color = DimensionalSkyHandler.lerpColor(
                cir.getReturnValue(),
                DimensionalSkyHandler.TARGET_CLOUDS,
                influence
        );

        float flash = DimensionalSkyHandler.getLightningFlash(partialTick);

        if (flash > 0.0F) {
            color = DimensionalSkyHandler.lerpColor(
                    color,
                    new Vec3(0.8D, 1.0D, 0.85D),
                    flash * 0.85F
            );
        }

        cir.setReturnValue(color);
    }
}
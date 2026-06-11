package net.stonedgoldfish.eopmod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.effect.EOPEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    private static final ResourceLocation BLEED_HEARTS =
            ResourceLocation.fromNamespaceAndPath("eop", "textures/gui/sprites/icons.png");

    @Inject(
            method = "renderHeart",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eop$renderBleedHeart(
            GuiGraphics guiGraphics,
            @Coerce Object heartType,
            int x,
            int y,
            int yOffset,
            boolean renderHighlight,
            boolean halfHeart,
            CallbackInfo ci
    ) {
        if (Minecraft.getInstance().player == null
                || !Minecraft.getInstance().player.hasEffect(EOPEffects.BLEED.get())) {
            return;
        }

        String typeName = heartType.toString();

        if (typeName.equals("CONTAINER") || typeName.equals("ABSORBING")) {
            return;
        }

        boolean hardcore = yOffset == 45;

        int u;

        if (hardcore) {
            if (renderHighlight) {
                u = halfHeart ? 63 : 54;
            } else {
                u = halfHeart ? 45 : 36;
            }
        } else {
            if (renderHighlight) {
                u = halfHeart ? 27 : 18;
            } else {
                u = halfHeart ? 9 : 0;
            }
        }

        guiGraphics.blit(
                BLEED_HEARTS,
                x,
                y,
                u,
                0,
                9,
                9,
                256,
                256
        );

        ci.cancel();
    }
}
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

    private static final ResourceLocation BLEED_FULL =
            ResourceLocation.fromNamespaceAndPath("eop", "textures/gui/sprites/blood_full.png");

    private static final ResourceLocation BLEED_HALF =
            ResourceLocation.fromNamespaceAndPath("eop", "textures/gui/sprites/blood_half.png");

    private static final ResourceLocation BLEED_FULL_HARDCORE =
            ResourceLocation.fromNamespaceAndPath("eop", "textures/gui/sprites/bleed_full_hardcore.png");

    private static final ResourceLocation BLEED_HALF_HARDCORE =
            ResourceLocation.fromNamespaceAndPath("eop", "textures/gui/sprites/bleed_half_hardcore.png");

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

        ResourceLocation texture;

        if (hardcore) {
            texture = halfHeart ? BLEED_HALF_HARDCORE : BLEED_FULL_HARDCORE;
        } else {
            texture = halfHeart ? BLEED_HALF : BLEED_FULL;
        }

        guiGraphics.blit(
                texture,
                x,
                y,
                0,
                0,
                9,
                9,
                9,
                9
        );

        ci.cancel();
    }
}
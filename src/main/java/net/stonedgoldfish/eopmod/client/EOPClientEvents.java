package net.stonedgoldfish.eopmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.client.screen.power.PowersScreen;
import net.threetag.palladium.event.PalladiumClientEvents;
import net.minecraft.resources.ResourceLocation;
import net.stonedgoldfish.eopmod.EOPMod;
import net.stonedgoldfish.eopmod.client.animation.EOPFlightAnimation;
import net.threetag.palladium.event.PalladiumClientEvents;

public class EOPClientEvents {

    private static final ResourceLocation EOP_GUI =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_border.png"
            );

    public static void init() {
        PalladiumClientEvents.RENDER_POWER_SCREEN.register(EOPClientEvents::renderPowerScreen);

        PalladiumClientEvents.REGISTER_ANIMATIONS.register(registry -> {
            registry.accept(
                    ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "flight"),
                    new EOPFlightAnimation(1000)
            );
        });
    }

    private static void renderPowerScreen(
            PowersScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            ResourceLocation tab
    ) {
        if (tab == null) {
            return;
        }

        if (!tab.getNamespace().equals("eop")) {
            return;
        }

        int textureWidth = 500;
        int textureHeight = 500;

        int centerX = screen.width / 2;
        int centerY = screen.height / 2;

        int x = centerX - 258;
        int y = centerY - 222;

        guiGraphics.blit(
                EOP_GUI,
                x,
                y,
                0,
                0,
                textureWidth,
                textureHeight,
                textureWidth,
                textureHeight
        );
    }

}
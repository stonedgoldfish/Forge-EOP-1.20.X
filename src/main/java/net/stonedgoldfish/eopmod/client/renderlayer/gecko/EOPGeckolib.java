package net.stonedgoldfish.eopmod.client.renderlayer.gecko;

import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager;

public class EOPGeckolib {

    public static void initClient() {
        PackRenderLayerManager.registerParser(
                ResourceLocation.fromNamespaceAndPath("eop", "geckolib"),
                EOPGeckoRenderLayer::parse
        );
    }
}
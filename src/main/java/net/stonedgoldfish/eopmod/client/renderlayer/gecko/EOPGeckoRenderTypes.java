package net.stonedgoldfish.eopmod.client.renderlayer.gecko;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class EOPGeckoRenderTypes {

    public static RenderType get(String type, ResourceLocation texture) {
        if (type == null || type.isBlank()) {
            return RenderType.entityCutout(texture);
        }

        return switch (type.toLowerCase()) {
            case "solid" -> RenderType.entitySolid(texture);
            case "cutout" -> RenderType.entityCutout(texture);
            case "cutout_no_cull" -> RenderType.entityCutoutNoCull(texture);
            case "translucent" -> RenderType.entityTranslucent(texture);
            case "translucent_cull" -> RenderType.entityTranslucentCull(texture);
            case "emissive", "eyes", "glow" -> RenderType.eyes(texture);
            default -> RenderType.entityCutout(texture);
        };
    }
}
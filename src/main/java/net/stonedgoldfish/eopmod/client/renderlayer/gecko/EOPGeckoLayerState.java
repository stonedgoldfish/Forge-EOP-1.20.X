package net.stonedgoldfish.eopmod.client.renderlayer.gecko;

import net.minecraft.client.Minecraft;
import net.threetag.palladium.client.renderer.renderlayer.RenderLayerStates;
import net.threetag.palladium.compat.geckolib.playeranimator.ParsedAnimationController;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.Map;

public class EOPGeckoLayerState extends RenderLayerStates.State implements GeoAnimatable {

    public final EOPGeckoRenderLayer layer;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, false);
    private final Map<String, AnimationController<EOPGeckoLayerState>> animationController = new HashMap<>();

    public EOPGeckoLayerState(EOPGeckoRenderLayer layer) {
        this.layer = layer;
    }

    @Nullable
    public AnimationController<EOPGeckoLayerState> getController(String name) {
        return this.animationController.get(name);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        for (ParsedAnimationController<EOPGeckoLayerState> parsed : this.layer.animationControllers) {
            var controller = parsed.createController(this);
            controllerRegistrar.add(controller);
            this.animationController.put(controller.getName(), controller);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return this.ticks + Minecraft.getInstance().getFrameTime();
    }
}
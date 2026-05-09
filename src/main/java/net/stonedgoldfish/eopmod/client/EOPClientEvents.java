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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)

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
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        if (!CustomFlightAbility.hasCustomFlight(player) || !player.getAbilities().flying) {
            return;
        }

        CustomFlightAbility.FlightSettings settings = CustomFlightAbility.getSettings(player);

        if (settings == null) {
            return;
        }

        Vec3 motion = Vec3.ZERO;

        double speed = settings.speed();

        if (settings.allowSprint() && player.isSprinting()) {
            speed *= settings.sprintMultiplier();
        }

        // Forward
        if (minecraft.options.keyUp.isDown()) {
            motion = motion.add(player.getLookAngle().normalize().scale(speed));
        }

        // Backward
        if (minecraft.options.keyDown.isDown()) {
            motion = motion.add(player.getLookAngle().normalize().scale(-speed * 0.6D));
        }

        float yawRad = (float) Math.toRadians(player.getYRot());

        Vec3 right = new Vec3(
                Math.cos(yawRad),
                0,
                Math.sin(yawRad)
        ).normalize();

        // Right
        if (minecraft.options.keyRight.isDown()) {
            motion = motion.add(right.scale(-speed * 0.7D));
        }

        // Left
        if (minecraft.options.keyLeft.isDown()) {
            motion = motion.add(right.scale(speed * 0.7D));
        }

        // Idle hovering
        Vec3 currentMotion = player.getDeltaMovement();

        boolean moving = minecraft.options.keyUp.isDown()
                || minecraft.options.keyDown.isDown()
                || minecraft.options.keyLeft.isDown()
                || minecraft.options.keyRight.isDown();

        double acceleration = settings.allowSprint() && player.isSprinting()
                ? Math.min(settings.acceleration() * 1.5D, 1.0D)
                : settings.acceleration();

        if (moving) {
            player.setDeltaMovement(currentMotion.lerp(motion, acceleration));
        } else {
            player.setDeltaMovement(currentMotion.scale(settings.drag()));
        }
        player.fallDistance = 0.0F;
    }
}
package net.stonedgoldfish.eopmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.stonedgoldfish.eopmod.EOPMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.stonedgoldfish.eopmod.client.animation.EOPCameraTransition;
import net.stonedgoldfish.eopmod.client.sound.EOPFlightSound;
import net.stonedgoldfish.eopmod.client.sound.EOPFlightSoundHandler;
import net.stonedgoldfish.eopmod.effect.EOPEffects;
import net.minecraft.client.player.LocalPlayer;
import net.stonedgoldfish.eopmod.client.animation.EOPAnimationHandler;
import net.stonedgoldfish.eopmod.client.animation.EOPFlightAnimation;
import net.stonedgoldfish.eopmod.client.animation.EOPPlayerAnimation;
import net.stonedgoldfish.eopmod.network.EOPNetwork;
import net.stonedgoldfish.eopmod.network.ToggleCustomFlightPacket;
import net.stonedgoldfish.eopmod.power.ability.IntangibilityAbility;
import net.threetag.palladium.client.screen.power.PowersScreen;
import net.threetag.palladium.event.PalladiumClientEvents;
import net.stonedgoldfish.eopmod.power.EOPPalladiumProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;
import net.stonedgoldfish.eopmod.power.EOPPowerRegistry;
import net.stonedgoldfish.eopmod.power.EOPPowerConstants;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.chat.Component;
@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)

public class EOPClientEvents {

    private static final ResourceLocation EOP_GUI =
            ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/ability_bars/power_gui/eop_border.png"
            );

    private static final ResourceLocation ICON_BORDER =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/eop_icon_border.png");

    private static final ResourceLocation XP_BAR_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/xp_bar_background.png");

    private static final ResourceLocation XP_BAR_FILL =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/xp_bar_fill.png");

    private static final ResourceLocation CLASS_ICON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/icon_class.png");

    private static final ResourceLocation SUBCLASS_ICON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/icon_subclass.png");

    private static final ResourceLocation COMBAT_ICON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/icon_combat.png");

    private static final ResourceLocation DIFFICULTY_ICON =
            ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "textures/gui/ability_bars/power_gui/icon_difficulty.png");

    private static boolean wasSprintFlying = false;

    public static void init() {
        PalladiumClientEvents.RENDER_POWER_SCREEN.register(EOPClientEvents::renderPowerScreen);

        EOPScreenShakeEvents.init();

        PalladiumClientEvents.REGISTER_ANIMATIONS.register(registry -> {
            registry.accept(
                    ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "player_animation"),
                    new EOPPlayerAnimation(1400)
            );
            registry.accept(
                    ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "flight_animation"),
                    new EOPFlightAnimation(1300)
            );
        });
    }

    private static void drawIconText(
            GuiGraphics guiGraphics,
            ResourceLocation icon,
            String text,
            int x,
            int y,
            int iconSize,
            int textOffset,
            int color
    ) {
        guiGraphics.blit(
                icon,
                x,
                y,
                0,
                0,
                iconSize,
                iconSize,
                iconSize,
                iconSize
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                text,
                x + textOffset,
                y,
                color,
                true
        );
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

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        String powerKey = tab.getPath();

        EOPPowerRegistry.EOPPower powerData = EOPPowerRegistry.getByKey(powerKey);
        if (powerData == null) {
            return;
        }

        String powerName = powerData.display().replace("_", " ");

        ResourceLocation powerIcon = ResourceLocation.fromNamespaceAndPath(
                "eop",
                "textures/gui/" + powerKey + ".png"
        );

        int xp = EOPPalladiumProperties.getXp(player, powerKey);

        int level = EOPPalladiumProperties.getLevel(player, powerKey);
        int skillPoints = EOPPalladiumProperties.getSkillPoints(player, powerKey);
        int maxXp = EOPPowerConstants.getMaxXpForLevel(level);

        float progress = Math.min((float) xp / maxXp, 1.0F);

        int barX = x + 74;
        int barY = y + 209;
        int barWidth = 45;
        int barHeight = 2;

        int filledWidth = (int) (barWidth * progress);

        // Background texture
        guiGraphics.blit(
                XP_BAR_BACKGROUND,
                barX,
                barY,
                0,
                0,
                barWidth,
                barHeight,
                barWidth,
                barHeight
        );

// Filled texture, cropped by XP progress
        guiGraphics.blit(
                XP_BAR_FILL,
                barX,
                barY,
                0,
                0,
                filledWidth,
                barHeight,
                barWidth,
                barHeight
        );

        int iconSize = 33;

        int iconX = barX + (barWidth / 2) - (iconSize / 2);
        int iconY = barY - 61;

        guiGraphics.blit(
                powerIcon,
                iconX,
                iconY,
                0,
                0,
                iconSize,
                iconSize,
                iconSize,
                iconSize
        );

        int borderSize = 43;
        int borderX = iconX - 5;
        int borderY = iconY - 5;

        guiGraphics.blit(
                ICON_BORDER,
                borderX,
                borderY,
                0,
                0,
                borderSize,
                borderSize,
                borderSize,
                borderSize
        );

        float powerNameScale = 0.7F;

        if (powerName.length() > 16) {
            powerNameScale *= 0.85F;
        }

        int powerNameColor = powerData != null
                ? (0xFF000000 | powerData.titleColor())
                : 0xFFFFFFFF;

        int powerNameWidth = Minecraft.getInstance().font.width(powerName);

        int powerNameWidthScaled = (int) (powerNameWidth * powerNameScale);

        int powerNameCenterX = iconX + 357;

        int powerNameX = (int) (
                (powerNameCenterX - (powerNameWidthScaled / 2))
                        / powerNameScale
        );
        int powerNameY = (int) ((iconY - 4) / powerNameScale);

        guiGraphics.pose().pushPose();

        guiGraphics.pose().scale(powerNameScale, powerNameScale, 1.0F);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                powerName,
                powerNameX,
                powerNameY,
                powerNameColor,
                true
        );

        guiGraphics.pose().popPose();

        String skillPointText = "Skill Points: " + skillPoints;

        float skillPointScale = 0.75F;

        int skillPointWidth = Minecraft.getInstance().font.width(skillPointText);

        int skillPointX = (int) (
                (powerNameCenterX - ((skillPointWidth * skillPointScale) / 2))
                        / skillPointScale
        );

        int skillPointY = (int) ((powerNameY * powerNameScale + 15) / skillPointScale);

        guiGraphics.pose().pushPose();

        guiGraphics.pose().scale(skillPointScale, skillPointScale, 1.0F);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                skillPointText,
                skillPointX,
                skillPointY,
                0xFFFFFFFF,
                true
        );

        guiGraphics.pose().popPose();

        if (powerData != null) {
            String classificationTitle = "Classification";

            String classText = "Class: " + powerData.powerClass();
            String subclassText = "Subclass: " + powerData.subclass();
            String combatText = "Combat: " + powerData.combatStyle();
            String difficultyText = "Difficulty: " + powerData.difficulty();

            float classificationScale = 0.55F;

            int classificationX = (int) ((powerNameCenterX - 18) / classificationScale);
            int classificationY = (int) ((powerNameY * powerNameScale + 30) / classificationScale);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(classificationScale, classificationScale, 1.0F);

            int smallIconSize = 8;
            int textOffset = 12;

            guiGraphics.drawString(Minecraft.getInstance().font, classificationTitle, classificationX, classificationY, 0x7FDBFF, true);
            drawIconText(guiGraphics, CLASS_ICON, classText, classificationX - 45, classificationY + 12, smallIconSize, textOffset, 0xFFFFFFFF);
            drawIconText(guiGraphics, SUBCLASS_ICON, subclassText, classificationX - 45, classificationY + 23, smallIconSize, textOffset, 0xFFFFFFFF);
            drawIconText(guiGraphics, COMBAT_ICON, combatText, classificationX - 45, classificationY + 34, smallIconSize, textOffset, 0xFFFFFFFF);
            drawIconText(guiGraphics, DIFFICULTY_ICON, difficultyText, classificationX - 45, classificationY + 45, smallIconSize, textOffset, 0xFFFFFFFF);

            guiGraphics.pose().popPose();

            String descriptionTitle = "Description";

            String descriptionKey = "power.eop." + powerKey + ".description";
            String descriptionText = Component.translatable(descriptionKey).getString();

            if (descriptionText.equals(descriptionKey)) {
                descriptionText = "No description available.";
            }

            float descriptionScale = 0.55F;

            int descriptionX = (int) ((powerNameCenterX - 16) / descriptionScale);
            int descriptionY = (int) ((classificationY * classificationScale + 37) / descriptionScale);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(descriptionScale, descriptionScale, 1.0F);

            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    descriptionTitle,
                    descriptionX,
                    descriptionY,
                    0x7FDBFF,
                    true
            );

            float descriptionTextScale = 0.9F;

            guiGraphics.pose().pushPose();

            guiGraphics.pose().scale(descriptionTextScale, descriptionTextScale, 1.0F);

            drawWrappedText(
                    guiGraphics,
                    descriptionText,
                    (int) ((descriptionX - 47) / descriptionTextScale),
                    (int) ((descriptionY + 12) / descriptionTextScale),
                    (int) (160 / descriptionTextScale),
                    10,
                    0xFFFFFFFF
            );

            guiGraphics.pose().popPose();

            guiGraphics.pose().popPose();
        }

        String levelLabel = "Level";
        String levelValue = String.valueOf(level);

        float labelScale = 0.6F;
        float numberScale = 1.1F;

        int labelWidth = Minecraft.getInstance().font.width(levelLabel);
        int valueWidth = Minecraft.getInstance().font.width(levelValue);

        int labelY = barY - 21;
        int valueY = labelY + 9;

// CENTERED POSITIONS
        int labelX = (int) (((barX + (barWidth / 2) - ((labelWidth * labelScale) / 2)) + 0.5) / labelScale);

        int valueX = (int) (((barX + (barWidth / 2) - ((valueWidth * numberScale) / 2)) + 0.5) / numberScale);

// LEVEL LABEL
        guiGraphics.pose().pushPose();

        guiGraphics.pose().scale(labelScale, labelScale, 1.0F);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                levelLabel,
                labelX,
                (int) (labelY / labelScale),
                0x69889a,
                true
        );

        guiGraphics.pose().popPose();

// LEVEL NUMBER
        guiGraphics.pose().pushPose();

        guiGraphics.pose().scale(numberScale, numberScale, 1.0F);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                levelValue,
                valueX,
                (int) (valueY / numberScale),
                0xFFFFFFFF,
                true
        );

        guiGraphics.pose().popPose();

        String xpText = xp + "/" + maxXp;

        float xpTextScale = 0.6F;

        int xpTextWidth = Minecraft.getInstance().font.width(xpText);

        int xpTextX = (int) ((barX + (barWidth / 2) - ((xpTextWidth * xpTextScale) / 2)) / xpTextScale);

        int xpTextY = barY + 8;

        guiGraphics.pose().pushPose();

        guiGraphics.pose().scale(xpTextScale, xpTextScale, 1.0F);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                xpText,
                xpTextX,
                (int) (xpTextY / xpTextScale),
                0xFFFFFFFF,
                true
        );

        guiGraphics.pose().popPose();

        String attributesLabel = "Attributes";

        float attributesLabelScale = 0.6F;

        int attributesLabelWidth = Minecraft.getInstance().font.width(attributesLabel);

        int attributesLabelX = (int) (
                (barX + (barWidth / 2) - ((attributesLabelWidth * attributesLabelScale) / 2))
                        / attributesLabelScale
        );

        int attributesLabelY = (int) ((barY + 28) / attributesLabelScale);

        guiGraphics.pose().pushPose();

        guiGraphics.pose().scale(attributesLabelScale, attributesLabelScale, 1.0F);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                attributesLabel,
                attributesLabelX,
                attributesLabelY,
                0x69889a,
                true
        );

        guiGraphics.pose().popPose();

        double health = player.getAttributeValue(Attributes.MAX_HEALTH);
        double armor = player.getAttributeValue(Attributes.ARMOR);
        double armorToughness = player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double attackDamage = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);

        float attributeScale = 0.70F;

        int attrX = (int) (barX / attributeScale);
        int attrY = (int) ((barY + 38) / attributeScale);

        int lineHeight = 15;

        guiGraphics.pose().pushPose();

        guiGraphics.pose().scale(attributeScale, attributeScale, 1.0F);

        String valueColorExample;
        int valueColor = 0xFFFFFF;

        String healthLabel = "Health: ";
        String healthValue = formatAttribute(health);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                healthLabel,
                attrX,
                attrY,
                0xf57d84,
                true
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                healthValue,
                attrX + Minecraft.getInstance().font.width(healthLabel),
                attrY,
                valueColor,
                true
        );

        String attackLabel = "Attack: ";
        String attackValue = formatAttribute(attackDamage);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                attackLabel,
                attrX,
                attrY + lineHeight,
                0x8ad28a,
                true
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                attackValue,
                attrX + Minecraft.getInstance().font.width(attackLabel),
                attrY + lineHeight,
                valueColor,
                true
        );

        String armorLabel = "Armor: ";
        String armorValue = formatAttribute(armor);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                armorLabel,
                attrX,
                attrY + lineHeight * 2,
                0x77b8da,
                true
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                armorValue,
                attrX + Minecraft.getInstance().font.width(armorLabel),
                attrY + lineHeight * 2,
                valueColor,
                true
        );

        String toughnessLabel = "Toughness: ";
        String toughnessValue = formatAttribute(armorToughness);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                toughnessLabel,
                attrX,
                attrY + lineHeight * 3,
                0x3384ae,
                true
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                toughnessValue,
                attrX + Minecraft.getInstance().font.width(toughnessLabel),
                attrY + lineHeight * 3,
                valueColor,
                true
        );

        String speedLabel = "Speed: ";
        String speedValue = formatAttribute(speed);

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                speedLabel,
                attrX,
                attrY + lineHeight * 4,
                0xd6b578,
                true
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                speedValue,
                attrX + Minecraft.getInstance().font.width(speedLabel),
                attrY + lineHeight * 4,
                valueColor,
                true
        );

        guiGraphics.pose().popPose();
    }

    private static void drawWrappedText(
            GuiGraphics guiGraphics,
            String text,
            int x,
            int y,
            int maxWidth,
            int lineHeight,
            int color
    ) {
        var font = Minecraft.getInstance().font;

        for (var line : font.split(Component.literal(text), maxWidth)) {
            guiGraphics.drawString(
                    font,
                    line,
                    x,
                    y,
                    color,
                    true
            );

            y += lineHeight;
        }
    }

    private static String formatAttribute(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    private static float lunarCloakTintProgress = 0.0F;
    private static final ResourceLocation[] LUNAR_CLOAK_FRAMES = new ResourceLocation[31];
    static {
        for (int i = 0; i < LUNAR_CLOAK_FRAMES.length; i++) {
            LUNAR_CLOAK_FRAMES[i] = ResourceLocation.fromNamespaceAndPath(
                    EOPMod.MOD_ID,
                    "textures/gui/hud/lunar_cloak/lunar_cloak_overlay_" + i + ".png"
            );
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        boolean active = minecraft.player != null
                && minecraft.player.hasEffect(EOPEffects.LUNAR_CLOAK.get());

        float fadeInSpeed = 0.05F;
        float fadeOutSpeed = 0.08F;

        if (active) {
            lunarCloakTintProgress += (1.0F - lunarCloakTintProgress) * fadeInSpeed;
        } else {
            lunarCloakTintProgress += (0.0F - lunarCloakTintProgress) * fadeOutSpeed;
        }

        if (lunarCloakTintProgress <= 0.01F) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        float textureOpacity = 0.7F;

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                lunarCloakTintProgress * textureOpacity
        );

        long gameTime = minecraft.level != null ? minecraft.level.getGameTime() : 0L;

        int ticksPerFrame = 3;
        int frame = (int) ((gameTime / ticksPerFrame) % LUNAR_CLOAK_FRAMES.length);

        ResourceLocation currentFrame = LUNAR_CLOAK_FRAMES[frame];

        event.getGuiGraphics().blit(
                currentFrame,
                0,
                0,
                0,
                0,
                screenWidth,
                screenHeight,
                screenWidth,
                screenHeight
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || !minecraft.player.hasEffect(EOPEffects.LUNAR_CLOAK.get())) {
            return;
        }

        SoundInstance original = event.getSound();

        if (original == null) {
            return;
        }

        event.setSound(new MuffledSoundInstance(original, 0.15F, 0.85F));
    }

    private record MuffledSoundInstance(
            SoundInstance original,
            float volumeMultiplier,
            float pitchMultiplier
    ) implements SoundInstance {

        @Override
        public ResourceLocation getLocation() {
            return original.getLocation();
        }

        @Override
        public WeighedSoundEvents resolve(SoundManager soundManager) {
            return original.resolve(soundManager);
        }

        @Override
        public Sound getSound() {
            return original.getSound();
        }

        @Override
        public SoundSource getSource() {
            return original.getSource();
        }

        @Override
        public boolean isLooping() {
            return original.isLooping();
        }

        @Override
        public boolean isRelative() {
            return original.isRelative();
        }

        @Override
        public int getDelay() {
            return original.getDelay();
        }

        @Override
        public float getVolume() {
            return original.getVolume() * volumeMultiplier;
        }

        @Override
        public float getPitch() {
            return original.getPitch() * pitchMultiplier;
        }

        @Override
        public double getX() {
            return original.getX();
        }

        @Override
        public double getY() {
            return original.getY();
        }

        @Override
        public double getZ() {
            return original.getZ();
        }

        @Override
        public Attenuation getAttenuation() {
            return original.getAttenuation();
        }
    }

    private static boolean wasJumpDown = false;
    private static long lastJumpPressTime = 0L;
    private static EOPFlightSound flightSound = null;
    private static final java.util.Set<Integer> LOOPING_ARMOR_STAND_SOUNDS = new java.util.HashSet<>();

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        if (!CustomFlightAbility.hasCustomFlight(player) || !CustomFlightAbility.isFlying(player)) {
            return;
        }
        event.getInput().shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        float viewDistance =
                IntangibilityAbility.getInsideBlockViewDistance(minecraft.player);

        if (viewDistance <= 0.0F) {
            return;
        }

        if (!isInsideBlock(minecraft.player)) {
            return;
        }

        event.setNearPlaneDistance(0.0F);
        event.setFarPlaneDistance(viewDistance);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockOverlay(RenderBlockScreenEffectEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        float viewDistance =
                IntangibilityAbility.getInsideBlockViewDistance(minecraft.player);

        if (viewDistance <= 0.0F) {
            return;
        }

        if (isInsideBlock(minecraft.player)) {
            event.setCanceled(true);
        }
    }

    private static boolean isInsideBlock(LocalPlayer player) {
        BlockPos eyePos = BlockPos.containing(player.getEyePosition());

        var state = player.level().getBlockState(eyePos);

        return !state.isAir()
                && state.getCollisionShape(player.level(), eyePos).isEmpty() == false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        EOPAnimationHandler.tick();
        EOPCameraTransition.tick();

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        if (CustomFlightAbility.hasCustomFlight(player) && player.isFallFlying()) {
            player.stopFallFlying();
        }

        if (CustomFlightAbility.isFlying(player) && player.onGround()) {
            CustomFlightAbility.resetSprintFlyingHitbox(player);
            EOPNetwork.CHANNEL.sendToServer(new ToggleCustomFlightPacket());
            wasSprintFlying = false;
            return;
        }

        boolean jumpDown =
                minecraft.options.keyJump.isDown()
                        && !NoJumpHandler.isJumpBlocked(player);

        if (jumpDown && !wasJumpDown) {
            long now = System.currentTimeMillis();

            if (now - lastJumpPressTime <= 300L && CustomFlightAbility.hasCustomFlight(player)) {
                EOPNetwork.CHANNEL.sendToServer(new ToggleCustomFlightPacket());
                lastJumpPressTime = 0L;
            } else {
                lastJumpPressTime = now;
            }
        }

        wasJumpDown = jumpDown;

        if (!CustomFlightAbility.hasCustomFlight(player) || !CustomFlightAbility.isFlying(player)) {
            return;
        }

        boolean sprintFlying = CustomFlightAbility.isFlying(player)
                && player.isSprinting()
                && minecraft.options.keyUp.isDown();

        CustomFlightAbility.setSprintFlying(player, sprintFlying);

        if (sprintFlying && !wasSprintFlying) {
            EOPFlightSoundHandler.start(player);
        }

        wasSprintFlying = sprintFlying;
        player.input.shiftKeyDown = false;

        if (player.getPose() != net.minecraft.world.entity.Pose.STANDING) {
            player.setPose(net.minecraft.world.entity.Pose.STANDING);
        }
        if (!player.isCreative() && !player.isSpectator()) {
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
            }
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

        double verticalKeySpeed = player.isSprinting()
                ? settings.speed() * 0.15D
                : settings.speed() * 0.45D;

        double verticalInput = 0.0D;

        if (minecraft.options.keyJump.isDown()) {
            verticalInput += verticalKeySpeed;
        }

        if (minecraft.options.keyShift.isDown()) {
            verticalInput -= verticalKeySpeed;
        }

        if (minecraft.options.keyUp.isDown()) {
            motion = motion.add(player.getLookAngle().normalize().scale(speed));
        }

        if (minecraft.options.keyDown.isDown()) {
            motion = motion.add(player.getLookAngle().normalize().scale(-speed * 0.6D));
        }

        float yawRad = (float) Math.toRadians(player.getYRot());

        Vec3 right = new Vec3(
                Math.cos(yawRad),
                0,
                Math.sin(yawRad)
        ).normalize();

        if (minecraft.options.keyRight.isDown()) {
            motion = motion.add(right.scale(-speed * 0.7D));
        }

        if (minecraft.options.keyLeft.isDown()) {
            motion = motion.add(right.scale(speed * 0.7D));
        }

        // Idle hovering
        Vec3 currentMotion = player.getDeltaMovement();

        boolean moving = minecraft.options.keyUp.isDown()
                || minecraft.options.keyDown.isDown()
                || minecraft.options.keyLeft.isDown()
                || minecraft.options.keyRight.isDown()
                || minecraft.options.keyJump.isDown()
                || minecraft.options.keyShift.isDown();

        double acceleration = settings.allowSprint() && player.isSprinting()
                ? Math.min(settings.acceleration() * 1.5D, 1.0D)
                : settings.acceleration();

        if (moving) {
            Vec3 horizontalAndLookMotion = new Vec3(motion.x, motion.y, motion.z);

            if (horizontalAndLookMotion.lengthSqr() > 0.0D) {
                horizontalAndLookMotion = horizontalAndLookMotion.normalize().scale(speed);
            }

            player.setDeltaMovement(
                    horizontalAndLookMotion.x,
                    horizontalAndLookMotion.y + verticalInput,
                    horizontalAndLookMotion.z
            );
        } else {
            double currentSpeed = currentMotion.length();

            double speedDragPenalty = Math.min(currentSpeed * 0.08D, 0.25D);
            double baseDrag = Math.min(settings.drag(), 1.0D);
            double dynamicDrag = baseDrag - speedDragPenalty;

            dynamicDrag = Math.max(dynamicDrag, 0.65D);

// Horizontal keeps your normal glide
            double horizontalDrag = dynamicDrag;

// Vertical stops much faster
            double verticalDrag = 0.65D;

            player.setDeltaMovement(
                    currentMotion.x * horizontalDrag,
                    currentMotion.y * verticalDrag,
                    currentMotion.z * horizontalDrag
            );
        }
        for (var armorStand : player.level().getEntitiesOfClass(
                net.minecraft.world.entity.decoration.ArmorStand.class,
                player.getBoundingBox().inflate(128.0D)
        )) {
            if (!armorStand.getPersistentData().contains("EOPLoopingSound")) {
                continue;
            }

            if (LOOPING_ARMOR_STAND_SOUNDS.contains(armorStand.getId())) {
                continue;
            }

            String soundRaw = armorStand.getPersistentData().getString("EOPLoopingSound");

            if (soundRaw == null || soundRaw.isEmpty()) {
                continue;
            }

            net.minecraft.resources.ResourceLocation sound =
                    net.minecraft.resources.ResourceLocation.tryParse(soundRaw);

            if (sound == null) {
                continue;
            }

            float volume = armorStand.getPersistentData().getFloat("EOPLoopingSoundVolume");
            float pitch = armorStand.getPersistentData().getFloat("EOPLoopingSoundPitch");

            Minecraft.getInstance().getSoundManager().play(
                    new net.stonedgoldfish.eopmod.client.sound.EOPArmorStandLoopingSound(
                            armorStand,
                            sound,
                            volume,
                            pitch
                    )
            );

            LOOPING_ARMOR_STAND_SOUNDS.add(armorStand.getId());
        }
    }
}
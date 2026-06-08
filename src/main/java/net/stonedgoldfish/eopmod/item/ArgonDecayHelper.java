package net.stonedgoldfish.eopmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;

public class ArgonDecayHelper {
    public static final long DEFAULT_DECAY_TIME = 216000L;

    public static final String CREATED_TIME_TAG = "ArgonCreatedTime";
    public static final String DECAY_TIME_TAG = "ArgonDecayTime";

    public static void inventoryTick(ItemStack stack, Level level, Entity entity) {
        if (level.isClientSide) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(CREATED_TIME_TAG)) {
            tag.putLong(CREATED_TIME_TAG, level.getGameTime());
        }

        if (!tag.contains(DECAY_TIME_TAG)) {
            tag.putLong(DECAY_TIME_TAG, DEFAULT_DECAY_TIME);
        }

        long createdTime = tag.getLong(CREATED_TIME_TAG);
        long decayTime = tag.getLong(DECAY_TIME_TAG);

        if (decayTime <= 0L) {
            decayTime = DEFAULT_DECAY_TIME;
            tag.putLong(DECAY_TIME_TAG, decayTime);
        }

        long age = level.getGameTime() - createdTime;

        if (age >= decayTime) {
            stack.shrink(1);
        }
    }

    public static boolean isBarVisible(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        return tag != null
                && tag.contains(CREATED_TIME_TAG)
                && tag.contains(DECAY_TIME_TAG);
    }

    public static int getBarWidth(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(CREATED_TIME_TAG) || !tag.contains(DECAY_TIME_TAG)) {
            return 0;
        }

        long decayTime = tag.getLong(DECAY_TIME_TAG);

        if (decayTime <= 0L) {
            decayTime = DEFAULT_DECAY_TIME;
        }

        long currentGameTime = getClientGameTime();

        if (currentGameTime < 0L) {
            return 13;
        }

        long createdTime = tag.getLong(CREATED_TIME_TAG);
        long age = currentGameTime - createdTime;
        long remainingTicks = Math.max(0L, decayTime - age);

        return Math.round(13.0F * (remainingTicks / (float) decayTime));
    }

    public static int getBarColor() {
        return 0x66CCFF;
    }

    public static void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (level == null) {
            return;
        }

        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(CREATED_TIME_TAG) || !tag.contains(DECAY_TIME_TAG)) {
            return;
        }

        long decayTime = tag.getLong(DECAY_TIME_TAG);

        if (decayTime <= 0L) {
            decayTime = DEFAULT_DECAY_TIME;
        }

        long createdTime = tag.getLong(CREATED_TIME_TAG);
        long age = level.getGameTime() - createdTime;
        long remainingTicks = Math.max(0L, decayTime - age);

        long remainingSeconds = remainingTicks / 20L;
        long minutes = remainingSeconds / 60L;
        long seconds = remainingSeconds % 60L;

        tooltip.add(Component.literal("Decays in: " + minutes + "m " + seconds + "s")
                .withStyle(ChatFormatting.AQUA));
    }

    private static long getClientGameTime() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return -1L;
        }

        if (Minecraft.getInstance().level == null) {
            return -1L;
        }

        return Minecraft.getInstance().level.getGameTime();
    }
}
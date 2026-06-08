package net.stonedgoldfish.eopmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;

public class ArgonCrystalItem extends Item {
    private static final long DEFAULT_DECAY_TIME = 216000L;

    private static final String CREATED_TIME_TAG = "ArgonCreatedTime";
    private static final String DECAY_TIME_TAG = "ArgonDecayTime";

    public ArgonCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

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

    @Override
    public boolean isBarVisible(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(CREATED_TIME_TAG)
                && tag.contains(DECAY_TIME_TAG);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains(CREATED_TIME_TAG) || !tag.contains(DECAY_TIME_TAG)) {
            return 0;
        }

        long decayTime = tag.getLong(DECAY_TIME_TAG);

        if (decayTime <= 0L) {
            decayTime = DEFAULT_DECAY_TIME;
        }

        long clientGameTime = getClientGameTime();

        if (clientGameTime < 0L) {
            return 13;
        }

        long createdTime = tag.getLong(CREATED_TIME_TAG);
        long age = clientGameTime - createdTime;
        long remainingTicks = Math.max(0L, decayTime - age);

        return Math.round(13.0F * (remainingTicks / (float) decayTime));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x66CCFF;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

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
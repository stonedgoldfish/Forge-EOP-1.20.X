package net.stonedgoldfish.eopmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class ArgonPickaxeItem extends PickaxeItem {
    public ArgonPickaxeItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        ArgonDecayHelper.inventoryTick(stack, level, entity);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ArgonDecayHelper.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ArgonDecayHelper.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ArgonDecayHelper.getBarColor();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ArgonDecayHelper.appendHoverText(stack, level, tooltip, flag);
    }
}
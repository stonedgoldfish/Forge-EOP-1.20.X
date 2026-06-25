package net.stonedgoldfish.eopmod.menu.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChipsSlot extends Slot {

    private boolean visible = true;
    public static final TagKey<Item> CHIP_TAG =
            ItemTags.create(
                    ResourceLocation.fromNamespaceAndPath("eop", "chips")
            );

    public ChipsSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isActive() {
        return this.visible;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(CHIP_TAG);
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }
}
package net.stonedgoldfish.eopmod.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ToggleableSlot extends Slot {

    private boolean visible = true;

    public ToggleableSlot(Container container, int slot, int x, int y) {
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
    public boolean mayPickup(net.minecraft.world.entity.player.Player player) {
        return this.visible && super.mayPickup(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.visible && super.mayPlace(stack);
    }
}
package net.stonedgoldfish.eopmod.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stonedgoldfish.eopmod.item.EOPItems;

public class FusionCatalystSlot extends Slot {

    private boolean visible = false;

    public FusionCatalystSlot(Container container, int slot, int x, int y) {
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
        return stack.is(EOPItems.FUSION_CATALYST.get());
    }
}
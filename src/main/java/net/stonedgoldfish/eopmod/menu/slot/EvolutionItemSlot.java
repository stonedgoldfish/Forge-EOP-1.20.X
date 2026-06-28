package net.stonedgoldfish.eopmod.menu.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stonedgoldfish.eopmod.EOPMod;

public class EvolutionItemSlot extends Slot {

    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> EVOLUTION_ITEMS_TAG =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(EOPMod.MOD_ID, "evolution_items"));

    private boolean visible = false;

    public EvolutionItemSlot(Container container, int slot, int x, int y) {
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
        return stack.is(EVOLUTION_ITEMS_TAG);
    }
}
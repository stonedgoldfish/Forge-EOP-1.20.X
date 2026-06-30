package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.stonedgoldfish.eopmod.power.ability.EOPAbilities;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.util.PlayerSlot;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.PlayerSlotListProperty;
import net.threetag.palladium.util.property.StringArrayProperty;
import net.threetag.palladium.util.property.SyncType;

import java.util.Collections;
import java.util.List;

public class SavedRestrictSlotsAbility extends Ability {

    public static final PalladiumProperty<List<PlayerSlot>> SLOTS = new PlayerSlotListProperty("slots").sync(SyncType.NONE).configurable("Slots that will be saved, emptied, and restricted");
    public static final PalladiumProperty<String[]> IGNORED_ITEMS = new StringArrayProperty("ignored_items").configurable("Item IDs that will not be saved or restricted");

    private static final String ROOT_KEY = "eop.saved_restrict_slots";

    public SavedRestrictSlotsAbility() {
        this.withProperty(ICON, new ItemIcon(Items.BARRIER));
        this.withProperty(SLOTS, Collections.singletonList(PlayerSlot.get(EquipmentSlot.CHEST)));
        this.withProperty(IGNORED_ITEMS, new String[]{});
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        CompoundTag root = entity.getPersistentData().getCompound(ROOT_KEY);

        for (PlayerSlot slot : entry.getProperty(SLOTS)) {
            String key = getSlotKey(slot);

            if (root.contains(key)) {
                continue;
            }

            ItemStack stack = getStack(entity, slot);
            if (shouldIgnore(stack, entry)) {
                continue;
            }

            CompoundTag savedStack = new CompoundTag();
            stack.save(savedStack);

            root.put(key, savedStack);

            clearSlot(entity, slot);
        }

        entity.getPersistentData().put(ROOT_KEY, root);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        restoreSavedItems(entity);
    }

    private static boolean shouldIgnore(ItemStack stack, AbilityInstance entry) {
        if (stack.isEmpty()) {
            return true;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        for (String ignored : entry.getProperty(IGNORED_ITEMS)) {
            if (itemId.equals(ignored)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isRestricted(LivingEntity entity, String key) {
        for (AbilityInstance entry : AbilityUtil.getEnabledEntries(entity, EOPAbilities.SAVED_RESTRICT_SLOTS.get())) {
            for (PlayerSlot slot : entry.getProperty(SLOTS)) {
                if (getSlotKey(slot).equalsIgnoreCase(key)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String getSlotKey(PlayerSlot slot) {
        return slot.toString().toLowerCase();
    }

    private static ItemStack getStack(LivingEntity entity, PlayerSlot slot) {
        String key = getSlotKey(slot);

        return switch (key) {
            case "head" -> entity.getItemBySlot(EquipmentSlot.HEAD);
            case "chest" -> entity.getItemBySlot(EquipmentSlot.CHEST);
            case "legs" -> entity.getItemBySlot(EquipmentSlot.LEGS);
            case "feet" -> entity.getItemBySlot(EquipmentSlot.FEET);
            default -> ItemStack.EMPTY;
        };
    }

    private static void setStack(LivingEntity entity, PlayerSlot slot, ItemStack stack) {
        String key = getSlotKey(slot);

        switch (key) {
            case "head" -> entity.setItemSlot(EquipmentSlot.HEAD, stack);
            case "chest" -> entity.setItemSlot(EquipmentSlot.CHEST, stack);
            case "legs" -> entity.setItemSlot(EquipmentSlot.LEGS, stack);
            case "feet" -> entity.setItemSlot(EquipmentSlot.FEET, stack);
        }
    }

    private static void clearSlot(LivingEntity entity, PlayerSlot slot) {
        setStack(entity, slot, ItemStack.EMPTY);
    }

    public static boolean isEquipmentSlotRestricted(LivingEntity entity, EquipmentSlot equipmentSlot) {
        for (AbilityInstance entry : AbilityUtil.getEnabledEntries(entity, EOPAbilities.SAVED_RESTRICT_SLOTS.get())) {
            for (PlayerSlot slot : entry.getProperty(SLOTS)) {
                String key = getSlotKey(slot);
                ItemStack currentStack = getRawStackForRestrictionCheck(entity, equipmentSlot);

                if (shouldIgnore(currentStack, entry)) {
                    continue;
                }

                if (key.equals("head") && equipmentSlot == EquipmentSlot.HEAD) return true;
                if (key.equals("chest") && equipmentSlot == EquipmentSlot.CHEST) return true;
                if (key.equals("legs") && equipmentSlot == EquipmentSlot.LEGS) return true;
                if (key.equals("feet") && equipmentSlot == EquipmentSlot.FEET) return true;
                if (key.equals("mainhand") && equipmentSlot == EquipmentSlot.MAINHAND) return true;
                if (key.equals("offhand") && equipmentSlot == EquipmentSlot.OFFHAND) return true;
            }
        }

        return false;
    }

    private static ItemStack getRawStackForRestrictionCheck(LivingEntity entity, EquipmentSlot slot) {
        if (entity instanceof Player player) {
            if (slot == EquipmentSlot.MAINHAND) {
                return player.getInventory().items.get(player.getInventory().selected);
            }

            if (slot == EquipmentSlot.OFFHAND) {
                return player.getInventory().offhand.get(0);
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isInventorySlotRestricted(LivingEntity entity, int slotIndex) {
        if (slotIndex == 39 && isEquipmentSlotRestricted(entity, EquipmentSlot.HEAD)) return true;
        if (slotIndex == 38 && isEquipmentSlotRestricted(entity, EquipmentSlot.CHEST)) return true;
        if (slotIndex == 37 && isEquipmentSlotRestricted(entity, EquipmentSlot.LEGS)) return true;
        if (slotIndex == 36 && isEquipmentSlotRestricted(entity, EquipmentSlot.FEET)) return true;
        if (slotIndex == 40 && isEquipmentSlotRestricted(entity, EquipmentSlot.OFFHAND)) return true;

        return false;
    }

    public static void restoreSavedItems(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }

        CompoundTag root = entity.getPersistentData().getCompound(ROOT_KEY);

        if (root.isEmpty()) {
            return;
        }

        for (String key : root.getAllKeys()) {
            ItemStack savedStack = ItemStack.of(root.getCompound(key));

            clearSlotByKey(entity, key);
            setStackByKey(entity, key, savedStack);
        }

        entity.getPersistentData().remove(ROOT_KEY);
    }

    private static void setStackByKey(LivingEntity entity, String key, ItemStack stack) {
        switch (key.toLowerCase()) {
            case "head" -> entity.setItemSlot(EquipmentSlot.HEAD, stack);
            case "chest" -> entity.setItemSlot(EquipmentSlot.CHEST, stack);
            case "legs" -> entity.setItemSlot(EquipmentSlot.LEGS, stack);
            case "feet" -> entity.setItemSlot(EquipmentSlot.FEET, stack);
        }
    }

    private static void clearSlotByKey(LivingEntity entity, String key) {
        setStackByKey(entity, key, ItemStack.EMPTY);
    }

    private static final ThreadLocal<Boolean> BYPASS_HAND_RESTRICTION =
            ThreadLocal.withInitial(() -> false);

    public static boolean shouldBypassHandRestriction() {
        return BYPASS_HAND_RESTRICTION.get();
    }

    public static void runWithHandRestrictionBypass(Runnable runnable) {
        BYPASS_HAND_RESTRICTION.set(true);

        try {
            runnable.run();
        } finally {
            BYPASS_HAND_RESTRICTION.set(false);
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Saves items in selected slots, removes them, restricts the slots, and restores the items when disabled.";
    }
}
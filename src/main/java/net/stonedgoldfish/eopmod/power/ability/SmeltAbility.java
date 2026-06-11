package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import java.util.Optional;

public class SmeltAbility extends Ability {

    public static final PalladiumProperty<Integer> TICKS_PER_ITEM = new IntegerProperty("ticks_per_item").configurable("Ticks required to smelt one item");
    private static final String SMELT_TAG = "EOP.Can.Smelt";
    private static final String PROGRESS_KEY = "autosmelt_progress";
    private static final String ITEM_KEY = "autosmelt_item";

    public SmeltAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FURNACE));
        this.withProperty(TICKS_PER_ITEM, 20);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.isDeadOrDying()) {
            clear(entity);
            return;
        }

        if (entity.level().isClientSide) {
            return;
        }

        ItemStack item = entity.getMainHandItem();

        if (item.isEmpty()) {
            clear(entity);
            return;
        }

        if (item.isDamageableItem() || item.getMaxDamage() > 0) {
            clear(entity);
            return;
        }

        SimpleContainer container = new SimpleContainer(item);
        Optional<SmeltingRecipe> recipeOpt = entity.level()
                .getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, container, entity.level());

        if (recipeOpt.isEmpty()) {
            clear(entity);
            return;
        }

        SmeltingRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.assemble(container, entity.level().registryAccess());

        if (result.isEmpty()) {
            clear(entity);
            return;
        }

        entity.addTag(SMELT_TAG);

        String currentItemId = item.getItem().builtInRegistryHolder().key().location().toString();
        String lastItemId = entity.getPersistentData().getString(ITEM_KEY);

        if (!currentItemId.equals(lastItemId)) {
            entity.getPersistentData().putInt(PROGRESS_KEY, 0);
            entity.getPersistentData().putString(ITEM_KEY, currentItemId);
            return;
        }

        int ticksPerItem = Math.max(1, entry.getProperty(TICKS_PER_ITEM));
        int stackSize = item.getCount();
        int totalTicksNeeded = stackSize * ticksPerItem;

        int progress = entity.getPersistentData().getInt(PROGRESS_KEY);
        progress++;

        entity.getPersistentData().putInt(PROGRESS_KEY, progress);

        int percent = Math.min(100, (int) Math.floor((progress / (float) totalTicksNeeded) * 100F));

        if (entity instanceof Player player) {
            player.displayClientMessage(Component.literal("Smelting: §e" + percent + "%"), true);
        }

        if (progress < totalTicksNeeded) {
            return;
        }

        ItemStack output = result.copy();
        output.setCount(stackSize);

        entity.setItemSlot(EquipmentSlot.MAINHAND, output);

        clear(entity);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide) {
            clear(entity);
        }
    }

    private static void clear(LivingEntity entity) {
        entity.removeTag(SMELT_TAG);
        entity.getPersistentData().remove(PROGRESS_KEY);
        entity.getPersistentData().remove(ITEM_KEY);
    }

    @Override
    public String getDocumentationDescription() {
        return "Smelts the currently held item.";
    }
}
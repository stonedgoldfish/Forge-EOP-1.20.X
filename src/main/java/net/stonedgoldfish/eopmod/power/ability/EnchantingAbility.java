package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.Map;

public class EnchantingAbility extends Ability {

    public static final PalladiumProperty<Boolean> CONSUME_BOOK =
            new BooleanProperty("consume_book")
                    .configurable("If true, the enchanted book is consumed when enchanting succeeds.");

    public EnchantingAbility() {
        this.withProperty(ICON, new ItemIcon(Items.ENCHANTED_BOOK));
        this.withProperty(CONSUME_BOOK, true);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack tool = player.getMainHandItem();
        ItemStack book = player.getOffhandItem();

        if (tool.isEmpty() || book.isEmpty()) {
            return;
        }

        if (!book.is(Items.ENCHANTED_BOOK)) {
            return;
        }

        Map<Enchantment, Integer> bookEnchantments =
                EnchantmentHelper.getEnchantments(book);

        boolean appliedAny = false;

        if (bookEnchantments.isEmpty()) {
            ListTag storedEnchantments = EnchantedBookItem.getEnchantments(book);

            for (int i = 0; i < storedEnchantments.size(); i++) {
                var tag = storedEnchantments.getCompound(i);

                ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));

                if (id == null) {
                    continue;
                }

                Enchantment enchantment =
                        net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.get(id);

                int level = tag.getInt("lvl");

                if (enchantment == null || level <= 0) {
                    continue;
                }

                if (tryApplyEnchantment(tool, enchantment, level)) {
                    appliedAny = true;
                }
            }
        } else {
            for (Map.Entry<Enchantment, Integer> enchantmentEntry : bookEnchantments.entrySet()) {
                if (tryApplyEnchantment(
                        tool,
                        enchantmentEntry.getKey(),
                        enchantmentEntry.getValue()
                )) {
                    appliedAny = true;
                }
            }
        }

        if (!appliedAny) {
            return;
        }

        if (entry.getProperty(CONSUME_BOOK)) {
            book.shrink(1);
        }

        entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private static boolean tryApplyEnchantment(ItemStack tool, Enchantment enchantment, int level) {
        if (!enchantment.canEnchant(tool)) {
            return false;
        }

        Map<Enchantment, Integer> currentEnchantments =
                EnchantmentHelper.getEnchantments(tool);

        int currentLevel = currentEnchantments.getOrDefault(enchantment, 0);

        if (level <= currentLevel) {
            return false;
        }

        currentEnchantments.put(enchantment, level);
        EnchantmentHelper.setEnchantments(currentEnchantments, tool);

        return true;
    }

    @Override
    public String getDocumentationDescription() {
        return "Applies valid enchantments from an enchanted book in the offhand to the item in the main hand.";
    }
}
package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringArrayProperty;

import java.util.UUID;

public class MultiAttributeModifierAbility extends Ability {

    public static final PalladiumProperty<String[]> ATTRIBUTES = new StringArrayProperty("attributes").configurable("Attributes to modify");
    public static final PalladiumProperty<String[]> AMOUNTS = new StringArrayProperty("amounts").configurable("Modifier amounts");
    public static final PalladiumProperty<String[]> OPERATIONS = new StringArrayProperty("operations").configurable("Operations: addition, multiply_base, multiply_total");
    public static final PalladiumProperty<String[]> MODIFIER_UUIDS = new StringArrayProperty("modifier_uuids").configurable("Unique UUID for each modifier");

    public MultiAttributeModifierAbility() {
        this.withProperty(ICON, new ItemIcon(Items.EXPERIENCE_BOTTLE));

        this.withProperty(ATTRIBUTES, new String[]{});
        this.withProperty(AMOUNTS, new String[]{});
        this.withProperty(OPERATIONS, new String[]{});
        this.withProperty(MODIFIER_UUIDS, new String[]{});
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || !(entity instanceof Player player)) {
            return;
        }

        String[] attributes = entry.getProperty(ATTRIBUTES);
        String[] amounts = entry.getProperty(AMOUNTS);
        String[] operations = entry.getProperty(OPERATIONS);
        String[] uuids = entry.getProperty(MODIFIER_UUIDS);

        int length = Math.min(
                Math.min(attributes.length, amounts.length),
                Math.min(operations.length, uuids.length)
        );

        for (int i = 0; i < length; i++) {
            applyModifier(
                    player,
                    attributes[i],
                    amounts[i],
                    operations[i],
                    uuids[i]
            );
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        String[] attributes = entry.getProperty(ATTRIBUTES);
        String[] uuids = entry.getProperty(MODIFIER_UUIDS);

        int length = Math.min(attributes.length, uuids.length);

        for (int i = 0; i < length; i++) {
            removeModifier(player, attributes[i], uuids[i]);
        }
    }

    private static void applyModifier(
            Player player,
            String attributeId,
            String amountString,
            String operationString,
            String uuidString
    ) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);

        if (id == null || uuidString == null || uuidString.isBlank()) {
            return;
        }

        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);

        if (attribute == null) {
            return;
        }

        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            return;
        }

        UUID uuid;

        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return;
        }

        AttributeModifier.Operation operation = parseOperation(operationString);

        instance.removeModifier(uuid);

        instance.addTransientModifier(
                new AttributeModifier(
                        uuid,
                        "eop_multi_attribute_modifier",
                        amount,
                        operation
                )
        );
    }

    private static void removeModifier(Player player, String attributeId, String uuidString) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);

        if (id == null || uuidString == null || uuidString.isBlank()) {
            return;
        }

        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);

        if (attribute == null) {
            return;
        }

        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        try {
            UUID uuid = UUID.fromString(uuidString);
            instance.removeModifier(uuid);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static AttributeModifier.Operation parseOperation(String operation) {
        return switch (operation.toLowerCase()) {
            case "multiply_base", "multiplty_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> AttributeModifier.Operation.ADDITION;
        };
    }

    @Override
    public String getDocumentationDescription() {
        return "Applies multiple configurable attribute modifiers at once.";
    }
}
package net.stonedgoldfish.eopmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CellularEnhancementEffect extends MobEffect {

    public CellularEnhancementEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x5DFF7A);

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "d1f6d9f1-1a7b-4c4f-b7a4-4eec6e5c1001",
                0.50D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                Attributes.ARMOR,
                "d1f6d9f1-1a7b-4c4f-b7a4-4eec6e5c1002",
                30.0D,
                AttributeModifier.Operation.ADDITION
        );

        this.addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                "d1f6d9f1-1a7b-4c4f-b7a4-4eec6e5c1003",
                20.0D,
                AttributeModifier.Operation.ADDITION
        );

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "d1f6d9f1-1a7b-4c4f-b7a4-4eec6e5c1004",
                0.60D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}
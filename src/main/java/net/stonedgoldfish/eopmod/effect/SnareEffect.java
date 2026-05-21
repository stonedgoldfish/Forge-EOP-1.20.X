package net.stonedgoldfish.eopmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SnareEffect extends MobEffect {

    public SnareEffect() {
        super(MobEffectCategory.HARMFUL, 0x5A7A5A);

        this.addAttributeModifier(
                net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                "4ab3a3d4-1637-4fcb-91ff-85bcbb64e902",
                -1.0D,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setDeltaMovement(
                entity.getDeltaMovement().x,
                Math.min(entity.getDeltaMovement().y, 0.0D),
                entity.getDeltaMovement().z
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
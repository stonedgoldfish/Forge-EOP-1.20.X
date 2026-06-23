package net.stonedgoldfish.eopmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CellularImmortalityEffect extends MobEffect {

    public CellularImmortalityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55FF88);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        if (entity.getServer() == null) {
            return;
        }

        String selector = entity.getStringUUID();
        entity.getServer().getCommands().performPrefixedCommand(
                entity.createCommandSourceStack().withSuppressedOutput().withPermission(2),
                "superpower add eop:mob_effects/cellular_immortality " + selector
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(
            LivingEntity entity,
            net.minecraft.world.entity.ai.attributes.AttributeMap attributes,
            int amplifier
    ) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (entity.level().isClientSide || entity.getServer() == null) {
            return;
        }
        String selector = entity.getStringUUID();
        entity.getServer().getCommands().performPrefixedCommand(
                entity.createCommandSourceStack().withSuppressedOutput().withPermission(2),
                "superpower remove eop:mob_effects/cellular_immortality " + selector
        );
    }
}
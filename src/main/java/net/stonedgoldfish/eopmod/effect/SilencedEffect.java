package net.stonedgoldfish.eopmod.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SilencedEffect extends MobEffect {

    public SilencedEffect() {
        super(MobEffectCategory.HARMFUL, 0x444488);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            player.addTag("EOP.Silenced");
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(
            LivingEntity entity,
            net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap,
            int amplifier
    ) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        if (entity instanceof ServerPlayer player) {
            player.removeTag("EOP.Silenced");
        }
    }
}
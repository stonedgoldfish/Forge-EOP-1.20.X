package net.stonedgoldfish.eopmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.stonedgoldfish.eopmod.particle.EOPParticles;
import net.stonedgoldfish.eopmod.util.EOPDamageTypes;

public class BleedEffect extends MobEffect {

    public BleedEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        if (entity.tickCount % 40 == 0) {
            float damage = 1.0F + amplifier;

            entity.hurt(
                    EOPDamageTypes.bleed(entity.level()),
                    damage
            );
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    EOPParticles.FALLING_BLOOD.get(),
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.6D,
                    entity.getZ(),
                    5,
                    entity.getBbWidth() * 0.35D,
                    entity.getBbHeight() * 0.25D,
                    entity.getBbWidth() * 0.35D,
                    0.02D
            );
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }
}
package net.stonedgoldfish.eopmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.stonedgoldfish.eopmod.particle.EOPParticles;

public class FracturedEffect extends MobEffect {

    public FracturedEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF55FF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        entity.addTag("EOP.Premature.Explosion");
        entity.getServer().getCommands().performPrefixedCommand(
                entity.createCommandSourceStack().withSuppressedOutput(),
                "superpower add eop:mob_effects/fractured @s"
        );

        if (entity.getTags().contains("Plane.Explode")) {
            entity.removeTag("Plane.Explode");
            entity.removeEffect(EOPEffects.FRACTURED.get());

            entity.hurt(
                    entity.damageSources().explosion(null, null),
                    12.0F
            );

            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WEAKNESS,
                    200,
                    2,
                    false,
                    false,
                    true
            ));

            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN,
                    200,
                    2,
                    false,
                    false,
                    true
            ));

            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    200,
                    2,
                    false,
                    false,
                    true
            ));

            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() * 0.8D;
            double z = entity.getZ();

            serverLevel.sendParticles(
                    EOPParticles.VOID_ENERGY.get(),
                    x, y, z,
                    120,
                    0.0D, 0.0D, 0.0D,
                    0.45D
            );

            entity.level().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS,
                    1.5F,
                    1.2F
            );

            entity.level().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.GLASS_BREAK,
                    SoundSource.PLAYERS,
                    2.0F,
                    0.5F
            );

            return;
        }

        if (entity.tickCount % 3 != 0) {
            return;
        }

        int particleCount = 25;

        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.8D;
        double z = entity.getZ();

        serverLevel.sendParticles(
                EOPParticles.VOID_ENERGY.get(),
                x,
                y,
                z,
                particleCount,
                entity.getBbWidth() * 0.4D,
                entity.getBbHeight() * 0.4D,
                entity.getBbWidth() * 0.4D,
                0.3D
        );

        serverLevel.sendParticles(
                EOPParticles.GLITCH.get(),
                x,
                y,
                z,
                particleCount - 10,
                entity.getBbWidth() * 0.4D,
                entity.getBbHeight() * 0.4D,
                entity.getBbWidth() * 0.4D,
                0.02D
        );
    }

    @Override
    public void removeAttributeModifiers(
            LivingEntity entity,
            net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap,
            int amplifier
    ) {

        entity.removeTag("EOP.Premature.Explosion");
        entity.getServer().getCommands().performPrefixedCommand(
                entity.createCommandSourceStack().withSuppressedOutput(),
                "superpower remove eop:mob_effects/fractured @s"
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
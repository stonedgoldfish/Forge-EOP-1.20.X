package net.stonedgoldfish.eopmod.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.stonedgoldfish.eopmod.particle.EOPParticles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DistortedEffect extends MobEffect {

    private static final Map<UUID, Integer> INVERTED_TICKS = new HashMap<>();

    public DistortedEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A2BE2);
    }
    private static final UUID DISTORTED_SPEED_UUID =
            UUID.fromString("9d2c4f72-5c7d-4e0d-92d8-1b91f5eac111");

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        entity.addTag("EOP.Premature.Explosion");
        entity.getServer().getCommands().performPrefixedCommand(
                entity.createCommandSourceStack().withSuppressedOutput(),
                "superpower add eop:mob_effects/distorted @s"
        );

        if (amplifier >= 5) {
            entity.removeEffect(EOPEffects.DISTORTED.get());
            entity.addEffect(new MobEffectInstance(
                    EOPEffects.FRACTURED.get(),
                    200,
                    0,
                    false,
                    false,
                    true
            ));
            return;
        }

        if (entity.getTags().contains("Plane.Explode")) {
            entity.removeTag("Plane.Explode");
            entity.removeEffect(EOPEffects.DISTORTED.get());

            float damage = 2.0F + (amplifier + 2.0F);

            entity.hurt(
                    entity.damageSources().explosion(null, null),
                    damage
            );

            entity.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    200,
                    2,
                    false,
                    false,
                    true
            ));

            entity.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    200,
                    2,
                    false,
                    false,
                    true
            ));

            entity.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    200,
                    2,
                    false,
                    false,
                    true
            ));

            if (entity.level() instanceof ServerLevel serverLevel) {
                double x = entity.getX();
                double y = entity.getY() + entity.getBbHeight() * 0.8D;
                double z = entity.getZ();

                serverLevel.sendParticles(
                        EOPParticles.VOID_ENERGY.get(),
                        x, y, z,
                        60 + amplifier * 20,
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
            }

            return;
        }

        if (entity.tickCount % 3 == 0
                && entity.level() instanceof ServerLevel serverLevel) {
            int particleCount = 2 + (amplifier * 2);

            serverLevel.sendParticles(
                    EOPParticles.VOID_ENERGY.get(),
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5D,
                    entity.getZ(),
                    particleCount,
                    entity.getBbWidth() * 0.6D,
                    entity.getBbHeight() * 0.5D,
                    entity.getBbWidth() * 0.6D,
                    0.02D
            );
        }

        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            distortMobMovement(entity, amplifier);
        }

        UUID uuid = entity.getUUID();

        int currentTicks = INVERTED_TICKS.getOrDefault(uuid, 0);

        if (currentTicks > 0) {
            INVERTED_TICKS.put(uuid, currentTicks - 1);
            return;
        }

        float chance = 0.005F + (amplifier * 0.005F);

        if (entity.getRandom().nextFloat() < chance) {
            INVERTED_TICKS.put(uuid, 20 + amplifier * 5);
        }

        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute != null) {

            var existing = attribute.getModifier(DISTORTED_SPEED_UUID);

            if (existing != null) {
                attribute.removeModifier(existing);
            }

            double penalty = -0.05D * (amplifier + 1);

            attribute.addTransientModifier(
                    new AttributeModifier(
                            DISTORTED_SPEED_UUID,
                            "Distorted speed penalty",
                            penalty,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    )
            );
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
        INVERTED_TICKS.remove(entity.getUUID());
        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute != null) {
            var existing = attribute.getModifier(DISTORTED_SPEED_UUID);

            if (existing != null) {
                attribute.removeModifier(existing);
            }
        }

        INVERTED_TICKS.remove(entity.getUUID());

        entity.removeTag("EOP.Premature.Explosion");
        entity.getServer().getCommands().performPrefixedCommand(
                entity.createCommandSourceStack().withSuppressedOutput(),
                "superpower remove eop:mob_effects/distorted @s"
        );
    }

    private static void distortMobMovement(LivingEntity entity, int amplifier) {
        int interval = Math.max(5, 30 - amplifier * 4);

        if (entity.tickCount % interval != 0) {
            return;
        }

        var random = entity.getRandom();

        entity.setYRot(random.nextFloat() * 360.0F);
        entity.setYHeadRot(entity.getYRot());

        double strength = 0.75D + amplifier * 0.3D;

        double x = (random.nextDouble() - 0.5D) * strength;
        double z = (random.nextDouble() - 0.5D) * strength;

        entity.setDeltaMovement(
                entity.getDeltaMovement().add(x, 0.0D, z)
        );

        entity.hurtMarked = true;
    }

    public static boolean isInverted(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        return INVERTED_TICKS.getOrDefault(entity.getUUID(), 0) > 0;
    }
}
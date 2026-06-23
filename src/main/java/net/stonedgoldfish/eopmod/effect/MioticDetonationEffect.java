package net.stonedgoldfish.eopmod.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class MioticDetonationEffect extends MobEffect {

    private static final UUID MIOTIC_SPEED_UUID =
            UUID.fromString("aa593f26-5c56-40de-90a8-7d077e8c2a01");

    private static final ResourceLocation BLEED_DAMAGE =
            ResourceLocation.fromNamespaceAndPath("eop", "bleed");

    private static final ResourceLocation FLESH_SOUND =
            ResourceLocation.fromNamespaceAndPath("eop", "flesh");

    public MioticDetonationEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B1A1A);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.getPersistentData().remove("EOPMioticDetonationTriggered");
            entity.addTag("EOP.Miotic.Explode");
        }

        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute != null) {
            var existing = attribute.getModifier(MIOTIC_SPEED_UUID);

            if (existing != null) {
                attribute.removeModifier(existing);
            }

            double penalty = -Math.min(0.95D, 0.30D + 0.10D * amplifier);

            attribute.addTransientModifier(new AttributeModifier(
                    MIOTIC_SPEED_UUID,
                    "Miotic detonation speed penalty",
                    penalty,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
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
        entity.removeTag("EOP.Miotic.Explode");
        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute != null) {
            var existing = attribute.getModifier(MIOTIC_SPEED_UUID);

            if (existing != null) {
                attribute.removeModifier(existing);
            }
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        if (!entity.level().isClientSide && entity.getServer() != null) {
            String key = "EOPMioticDetonationTriggered";

            if (!entity.getPersistentData().getBoolean(key)) {
                entity.getPersistentData().putBoolean(key, true);

                entity.getServer().execute(() -> {
                    if (entity.isAlive()) {
                        detonate(entity);
                    }

                    entity.getPersistentData().remove(key);
                });
            }
        }
    }

    private static void detonate(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        entity.hurt(createBleedDamageSource(entity), 3.0F);

        if (entity.getRandom().nextFloat() < 0.5F) {
            entity.addEffect(
                    new MobEffectInstance(
                            EOPEffects.BLEED.get(),
                            200,
                            1,
                            false,
                            false,
                            true
                    )
            );
        }

        serverLevel.sendParticles(
                new net.minecraft.core.particles.BlockParticleOption(
                        ParticleTypes.BLOCK,
                        net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState()
                ),
                entity.getX(),
                entity.getY() + entity.getBbHeight() * 0.5D,
                entity.getZ(),
                24,
                entity.getBbWidth() * 0.5D,
                entity.getBbHeight() * 0.4D,
                entity.getBbWidth() * 0.5D,
                0.08D
        );

        serverLevel.sendParticles(
                new net.minecraft.core.particles.BlockParticleOption(
                        ParticleTypes.BLOCK,
                        net.minecraft.world.level.block.Blocks.TERRACOTTA.defaultBlockState()
                ),
                entity.getX(),
                entity.getY() + entity.getBbHeight() * 0.5D,
                entity.getZ(),
                24,
                entity.getBbWidth() * 0.5D,
                entity.getBbHeight() * 0.4D,
                entity.getBbWidth() * 0.5D,
                0.08D
        );

        serverLevel.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                SoundEvent.createVariableRangeEvent(FLESH_SOUND),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private static DamageSource createBleedDamageSource(LivingEntity entity) {
        ResourceKey<DamageType> key = ResourceKey.create(
                Registries.DAMAGE_TYPE,
                BLEED_DAMAGE
        );

        return new DamageSource(
                entity.level()
                        .registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(key),
                entity
        );
    }
}
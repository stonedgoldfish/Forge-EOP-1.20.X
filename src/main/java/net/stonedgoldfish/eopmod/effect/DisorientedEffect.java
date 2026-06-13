package net.stonedgoldfish.eopmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class DisorientedEffect extends MobEffect {

    private static final UUID DISORIENTED_SPEED_UUID =
            UUID.fromString("3a9f81d2-0a53-46df-9f0f-5d24d72a6c91");

    public DisorientedEffect() {
        super(MobEffectCategory.HARMFUL, 0x9B5DE5);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute == null) {
            return;
        }

        var existing = attribute.getModifier(DISORIENTED_SPEED_UUID);

        if (existing != null) {
            attribute.removeModifier(existing);
        }

        double penalty = -0.05D * (amplifier + 1);

        attribute.addTransientModifier(
                new AttributeModifier(
                        DISORIENTED_SPEED_UUID,
                        "Disoriented speed penalty",
                        penalty,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );

        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            distortMobMovement(entity, amplifier);
        }
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
        var attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (attribute != null) {
            var existing = attribute.getModifier(DISORIENTED_SPEED_UUID);

            if (existing != null) {
                attribute.removeModifier(existing);
            }
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }
}
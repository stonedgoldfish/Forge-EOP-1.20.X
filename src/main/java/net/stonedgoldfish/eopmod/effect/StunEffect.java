package net.stonedgoldfish.eopmod.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StunEffect extends MobEffect {

    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0x777777);

        this.addAttributeModifier(
                net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                "9f44f29e-3d7e-4e0f-8b53-d5d7d1d8a901",
                -1.0D,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
    private static final Set<UUID> STUNNED_MOBS = new HashSet<>();

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            player.addTag("EOP.Silenced");
        }

        entity.setDeltaMovement(
                entity.getDeltaMovement().x,
                Math.min(entity.getDeltaMovement().y, 0.0D),
                entity.getDeltaMovement().z
        );

        if (entity instanceof Mob mob && !mob.isNoAi()) {
            mob.setNoAi(true);
            STUNNED_MOBS.add(mob.getUUID());
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

        if (entity instanceof Mob mob
                && STUNNED_MOBS.remove(mob.getUUID())) {
            mob.setNoAi(false);
        }
    }
}
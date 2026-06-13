package net.stonedgoldfish.eopmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

public class PlaneShiftEffect extends MobEffect {

    public PlaneShiftEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xCDE6C3);

        this.addAttributeModifier(
                net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                "22cde7f7-0b53-46a4-878e-f5f1f486f0e2",
                0.10D,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        player.addTag("EOP.Phasing");

        player.getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withSuppressedOutput(),
                "superpower add eop:mob_effects/plane_shift @s"
        );
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
            player.removeTag("EOP.Phasing");
            player.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput(),
                    "superpower remove eop:mob_effects/plane_shift @s"
            );
        }
    }
}
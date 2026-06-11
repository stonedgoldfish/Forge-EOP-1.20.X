package net.stonedgoldfish.eopmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

public class LunarCloakEffect extends MobEffect {

    public LunarCloakEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8A7CFF);

        this.addAttributeModifier(
                net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                "4d6f8f3a-0d24-4de3-9c88-3df8d5c9a001",
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
        player.addTag("EOP.Ability.Disabled");

        player.getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withSuppressedOutput(),
                "superpower add eop:mob_effects/lunar_cloak @s"
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
            player.removeTag("EOP.Ability.Disabled");
            player.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput(),
                    "superpower remove eop:mob_effects/lunar_cloak @s"
            );
        }
    }
}
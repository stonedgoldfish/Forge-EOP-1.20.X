package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;

public class SayHiAbility extends Ability {

    public SayHiAbility() {
        this.withProperty(ICON, new ItemIcon(Items.PAPER));
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entity.level().isClientSide && enabled && entity instanceof Player player) {
            player.sendSystemMessage(Component.literal("Hi!"));
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Makes the player say hi in chat when the ability is enabled.";
    }
}
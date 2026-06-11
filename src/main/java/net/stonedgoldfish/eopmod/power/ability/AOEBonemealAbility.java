package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

public class AOEBonemealAbility extends Ability {

    public static final PalladiumProperty<Float> RADIUS = new FloatProperty("radius").configurable("Radius to apply bonemeal");

    public AOEBonemealAbility() {
        this.withProperty(ICON, new ItemIcon(Items.BONE_MEAL));
        this.withProperty(RADIUS, 4.0F);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        float radius = Math.max(0.0F, entry.getProperty(RADIUS));
        int r = (int) Math.ceil(radius);

        BlockPos center = entity.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -r, -r),
                center.offset(r, r, r)
        )) {
            if (!pos.closerToCenterThan(entity.position(), radius)) {
                continue;
            }

            BoneMealItem.growCrop(
                    net.minecraft.world.item.ItemStack.EMPTY,
                    level,
                    pos
            );

            BoneMealItem.growWaterPlant(
                    net.minecraft.world.item.ItemStack.EMPTY,
                    level,
                    pos,
                    null
            );
        }

        level.levelEvent(
                1505,
                center,
                0
        );
    }

    @Override
    public String getDocumentationDescription() {
        return "Applies bonemeal in a radius.";
    }
}
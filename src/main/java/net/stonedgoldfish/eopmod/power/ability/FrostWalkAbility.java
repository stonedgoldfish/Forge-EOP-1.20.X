package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.stonedgoldfish.eopmod.block.EOPBlocks;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

public class FrostWalkAbility extends Ability {

    public static final PalladiumProperty<Integer> LEVEL =
            new IntegerProperty("level")
                    .configurable("Frost walker level. Higher values create a larger ice radius.");
    public static final PalladiumProperty<Boolean> LAVA_MODE =
            new BooleanProperty("lava_mode")
                    .configurable("If true, creates obsidian on lava instead of frosted ice on water.");

    public FrostWalkAbility() {
        this.withProperty(ICON, new ItemIcon(Items.BLUE_ICE));
        this.withProperty(LEVEL, 2);
        this.withProperty(LAVA_MODE, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        int level = Math.max(1, entry.getProperty(LEVEL));

        if (entry.getProperty(LAVA_MODE)) {
            freezeNearbyLava(entity, entity.level(), entity.blockPosition(), level);
        } else {
            freezeNearbyWater(entity, entity.level(), entity.blockPosition(), level);
        }
    }

    private static void freezeNearbyWater(
            LivingEntity entity,
            Level level,
            BlockPos center,
            int frostLevel
    ) {
        if (!entity.onGround()) {
            return;
        }

        BlockState frostedIce =
                Blocks.FROSTED_ICE.defaultBlockState()
                        .setValue(FrostedIceBlock.AGE, 0);

        float radius = Math.min(16, 2 + frostLevel);

        BlockPos.betweenClosedStream(
                center.offset((int) -radius, -1, (int) -radius),
                center.offset((int) radius, -1, (int) radius)
        ).forEach(pos -> {
            if (!pos.closerToCenterThan(entity.position(), radius)) {
                return;
            }

            BlockState state = level.getBlockState(pos);

            if (!state.is(Blocks.WATER)) {
                return;
            }

            if (!level.getBlockState(pos.above()).isAir()) {
                return;
            }

            if (frostedIce.canSurvive(level, pos)
                    && level.isUnobstructed(frostedIce, pos, net.minecraft.world.phys.shapes.CollisionContext.empty())) {
                level.setBlockAndUpdate(pos, frostedIce);
                level.scheduleTick(pos, Blocks.FROSTED_ICE, net.minecraft.util.Mth.nextInt(entity.getRandom(), 60, 120));
            }
        });
    }
    private static void freezeNearbyLava(
            LivingEntity entity,
            Level level,
            BlockPos center,
            int frostLevel
    ) {
        if (!entity.onGround()) {
            return;
        }

        float radius = Math.min(16, 2 + frostLevel);

        BlockPos.betweenClosedStream(
                center.offset((int) -radius, -1, (int) -radius),
                center.offset((int) radius, -1, (int) radius)
        ).forEach(pos -> {
            if (!pos.closerToCenterThan(entity.position(), radius)) {
                return;
            }

            BlockState state = level.getBlockState(pos);

            if (!state.is(Blocks.LAVA)) {
                return;
            }

            if (!level.getBlockState(pos.above()).isAir()) {
                return;
            }

            level.setBlockAndUpdate(
                    pos,
                    EOPBlocks.TEMPORARY_OBSIDIAN.get().defaultBlockState()
            );

            level.scheduleTick(
                    pos,
                    EOPBlocks.TEMPORARY_OBSIDIAN.get(),
                    net.minecraft.util.Mth.nextInt(entity.getRandom(), 60, 120)
            );
        });
    }

    @Override
    public String getDocumentationDescription() {
        return "Allows the entity to walk on water by creating temporary frosted ice like the vanilla Frost Walker enchantment.";
    }
}
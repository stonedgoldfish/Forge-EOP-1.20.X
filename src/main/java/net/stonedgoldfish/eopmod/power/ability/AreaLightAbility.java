package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.*;

public class AreaLightAbility extends Ability {

    public static final PalladiumProperty<Integer> LIGHT_LEVEL =
            new IntegerProperty("light_level")
                    .configurable("Light level of the temporary light blocks. 0-15.");

    private static final Map<UUID, Set<BlockPos>> PLACED_LIGHTS = new HashMap<>();

    public AreaLightAbility() {
        this.withProperty(ICON, new ItemIcon(Items.GLOWSTONE_DUST));
        this.withProperty(LIGHT_LEVEL, 15);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        UUID uuid = entity.getUUID();

        if (!enabled) {
            removeLights(level, uuid);
            return;
        }

        int lightLevel = Math.max(0, Math.min(15, entry.getProperty(LIGHT_LEVEL)));

        Set<BlockPos> oldLights = PLACED_LIGHTS.getOrDefault(uuid, new HashSet<>());
        Set<BlockPos> newLights = new HashSet<>();

        BlockPos center = entity.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-1, -1, -1),
                center.offset(1, 1, 1)
        )) {
            BlockPos immutablePos = pos.immutable();
            BlockState state = level.getBlockState(immutablePos);

            if (isAir(state) || state.is(Blocks.LIGHT)) {
                level.setBlock(
                        immutablePos,
                        Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel),
                        3
                );

                newLights.add(immutablePos);
            }
        }

        for (BlockPos oldPos : oldLights) {
            if (!newLights.contains(oldPos) && level.getBlockState(oldPos).is(Blocks.LIGHT)) {
                level.setBlock(oldPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        PLACED_LIGHTS.put(uuid, newLights);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level() instanceof ServerLevel level) {
            removeLights(level, entity.getUUID());
        }
    }

    private static boolean isAir(BlockState state) {
        return state.is(Blocks.AIR)
                || state.is(Blocks.CAVE_AIR)
                || state.is(Blocks.VOID_AIR);
    }

    private static void removeLights(ServerLevel level, UUID uuid) {
        Set<BlockPos> lights = PLACED_LIGHTS.remove(uuid);

        if (lights == null) {
            return;
        }

        for (BlockPos pos : lights) {
            if (level.getBlockState(pos).is(Blocks.LIGHT)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Temporarily places light blocks around the entity while active.";
    }
}
package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AreaLightAbility extends Ability {

    public static final PalladiumProperty<Integer> LIGHT_LEVEL =
            new IntegerProperty("light_level")
                    .configurable("Light level of the temporary light blocks. 0-15.");

    private static final Map<String, Set<BlockPos>> PLACED_LIGHTS = new HashMap<>();

    public AreaLightAbility() {
        this.withProperty(ICON, new ItemIcon(Items.GLOWSTONE_DUST));
        this.withProperty(LIGHT_LEVEL, 15);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        String lightKey = entity.getUUID() + ":" + entry.getReference();

        if (!enabled || entity.isRemoved() || !entity.isAlive()) {
            removeLights(level, lightKey);
            return;
        }

        int lightLevel = Math.max(0, Math.min(15, entry.getProperty(LIGHT_LEVEL)));

        Set<BlockPos> oldLights = PLACED_LIGHTS.getOrDefault(lightKey, new HashSet<>());
        Set<BlockPos> newLights = new HashSet<>();

        BlockPos center = entity.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-1, -1, -1),
                center.offset(1, 1, 1)
        )) {
            BlockPos lightPos = pos.immutable();
            BlockState state = level.getBlockState(lightPos);

            if (isAir(state) || state.is(Blocks.LIGHT)) {
                level.setBlock(
                        lightPos,
                        Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel),
                        3
                );

                newLights.add(lightPos);
            }
        }

        for (BlockPos oldPos : oldLights) {
            if (!newLights.contains(oldPos) && level.getBlockState(oldPos).is(Blocks.LIGHT)) {
                level.setBlock(oldPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        PLACED_LIGHTS.put(lightKey, newLights);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level() instanceof ServerLevel level) {
            removeLights(level, entity.getUUID() + ":" + entry.getReference());
        }
    }

    public static void removeLights(ServerLevel level, String lightKey) {
        Set<BlockPos> lights = PLACED_LIGHTS.remove(lightKey);

        if (lights == null) {
            return;
        }

        for (BlockPos pos : lights) {
            if (level.getBlockState(pos).is(Blocks.LIGHT)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    public static void removeLights(ServerLevel level, UUID uuid) {
        String prefix = uuid.toString() + ":";

        Set<String> keysToRemove = new HashSet<>();

        for (String key : PLACED_LIGHTS.keySet()) {
            if (key.startsWith(prefix)) {
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            removeLights(level, key);
        }
    }

    public static void removeAllLights(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Set<BlockPos> lights : PLACED_LIGHTS.values()) {
                for (BlockPos pos : lights) {
                    if (level.getBlockState(pos).is(Blocks.LIGHT)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        PLACED_LIGHTS.clear();
    }

    private static boolean isAir(BlockState state) {
        return state.is(Blocks.AIR)
                || state.is(Blocks.CAVE_AIR)
                || state.is(Blocks.VOID_AIR);
    }

    @Override
    public String getDocumentationDescription() {
        return "Temporarily places light blocks around the entity while active.";
    }
}
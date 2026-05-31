package net.stonedgoldfish.eopmod.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EOPArmorStandSpawner {

    public static ArmorStand spawnBasic(
            LivingEntity caster,
            Level level,
            Vec3 position,
            float yRot
    ) {
        ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);

        armorStand.setPos(position.x, position.y, position.z);
        armorStand.setYRot(yRot);

        armorStand.setInvulnerable(true);
        armorStand.setNoGravity(true);
        armorStand.setInvisible(true);

        armorStand.getPersistentData().putUUID("EOPCaster", caster.getUUID());

        level.addFreshEntity(armorStand);

        return armorStand;
    }

    public static void applyCommonData(
            ArmorStand armorStand,
            int lifetime,
            float aoeDamage,
            float aoeRadius,
            String aoeDamageType,
            boolean enableDamage,
            boolean damageOnLastTick,
            float knockbackOnLastTick,
            float targetCommandRadius,
            float pullStrength,
            boolean invertPull,
            String standPower,
            String[] standFirstTickCommands,
            String[] standCommands,
            String[] standLastTickCommands,
            String[] targetFirstTickCommands,
            String[] targetCommands,
            String[] targetLastTickCommands,
            String loopingSound,
            float loopingSoundVolume,
            float loopingSoundPitch,
            boolean destroyBlocks,
            float destroyBlockRadius
    ) {
        if (lifetime > 0) {
            armorStand.getPersistentData().putInt("EOPLifetime", lifetime);
        }

        armorStand.getPersistentData().putFloat("EOPAOEDamage", aoeDamage);
        armorStand.getPersistentData().putFloat("EOPAOERadius", aoeRadius);
        armorStand.getPersistentData().putString("EOPAOEDamageType", aoeDamageType);
        armorStand.getPersistentData().putBoolean("EOPEnableDamage", enableDamage);
        armorStand.getPersistentData().putBoolean("EOPDamageOnLastTick", damageOnLastTick);
        armorStand.getPersistentData().putFloat("EOPKnockbackOnLastTick", knockbackOnLastTick);

        armorStand.getPersistentData().putFloat("EOPTargetCommandRadius", targetCommandRadius);

        armorStand.getPersistentData().putFloat("EOPPullStrength", pullStrength);
        armorStand.getPersistentData().putBoolean("EOPInvertPull", invertPull);

        armorStand.getPersistentData().putString("EOPStandFirstTickCommands", String.join("||", standFirstTickCommands));
        armorStand.getPersistentData().putString("EOPStandCommands", String.join("||", standCommands));
        armorStand.getPersistentData().putString("EOPStandLastTickCommands", String.join("||", standLastTickCommands));

        armorStand.getPersistentData().putString("EOPTargetFirstTickCommands", String.join("||", targetFirstTickCommands));
        armorStand.getPersistentData().putString("EOPTargetCommands", String.join("||", targetCommands));
        armorStand.getPersistentData().putString("EOPTargetLastTickCommands", String.join("||", targetLastTickCommands));
        armorStand.getPersistentData().putString("EOPLoopingSound", loopingSound);
        armorStand.getPersistentData().putFloat("EOPLoopingSoundVolume", loopingSoundVolume);
        armorStand.getPersistentData().putFloat("EOPLoopingSoundPitch", loopingSoundPitch);
        armorStand.getPersistentData().putInt("EOPLoopingSoundTick", 0);
        armorStand.getPersistentData().putBoolean("EOPDestroyBlocks", destroyBlocks);
        armorStand.getPersistentData().putFloat("EOPDestroyBlockRadius", destroyBlockRadius);

        if (standPower != null && !standPower.isEmpty() && armorStand.getServer() != null) {
            armorStand.getServer().getCommands().performPrefixedCommand(
                    armorStand.createCommandSourceStack()
                            .withSuppressedOutput()
                            .withPermission(2),
                    "superpower add " + standPower + " @s"
            );
        }
    }
}
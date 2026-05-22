package net.stonedgoldfish.eopmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonedgoldfish.eopmod.EOPMod;

public class EOPEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EOPMod.MOD_ID);

    public static final RegistryObject<EntityType<EOPProjectileEntity>> BASIC_PROJECTILE =
            ENTITIES.register("basic_projectile", () ->
                    EntityType.Builder.<EOPProjectileEntity>of(
                                    EOPProjectileEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("basic_projectile")
            );
}
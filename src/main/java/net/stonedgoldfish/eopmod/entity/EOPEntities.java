package net.stonedgoldfish.eopmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonedgoldfish.eopmod.entity.DimensionalSlashProjectile;
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
    public static final RegistryObject<EntityType<DimensionalSlashProjectile>> DIMENSIONAL_SLASH =
            ENTITIES.register("dimensional_slash", () ->
                    EntityType.Builder.<DimensionalSlashProjectile>of(
                                    DimensionalSlashProjectile::new,
                                    MobCategory.MISC
                            )
                            .sized(2.5F, 2.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("dimensional_slash")
            );
    public static final RegistryObject<EntityType<DimensionalSlash2Projectile>> DIMENSIONAL_SLASH_2 =
            ENTITIES.register("dimensional_slash_2", () ->
                    EntityType.Builder.<DimensionalSlash2Projectile>of(
                                    DimensionalSlash2Projectile::new,
                                    MobCategory.MISC
                            )
                            .sized(2.5F, 2.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("dimensional_slash_2")
            );
    public static final RegistryObject<EntityType<RedProjectile>> RED =
            ENTITIES.register("red", () ->
                    EntityType.Builder.<RedProjectile>of(
                                    RedProjectile::new,
                                    MobCategory.MISC
                            )
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("red")
            );

    public static final RegistryObject<EntityType<PurpleProjectile>> PURPLE =
            ENTITIES.register("purple", () ->
                    EntityType.Builder.<PurpleProjectile>of(
                                    PurpleProjectile::new,
                                    MobCategory.MISC
                            )
                            .sized(3.5F, 3.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("purple")
            );
}